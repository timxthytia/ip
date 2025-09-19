package tim.reminder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tim.task.Deadline;
import tim.task.Event;
import tim.task.Task;
import tim.task.TaskList;

/**
 * Periodically scans the existing {@link TaskList} and fires one-time reminders for
 * <ul>
 *   <li>{@link Deadline} — at the due date/time</li>
 *   <li>{@link Event} — at the start date/time</li>
 * </ul>
 *
 * <p>Reminders are delivered to a {@link ReminderListener} on the GUI, which may
 * display a popup. Dismissed reminders are persisted to prevent re-firing across sessions.</p>
 */
public class ReminderService {

    // Constants for timing configuration
    public static final Duration SCAN_PERIOD = Duration.ofSeconds(20);
    public static final Duration STARTUP_GRACE = Duration.ofHours(24);

    // File system constants
    private static final String DISMISSED_REMINDERS_FILE = "data/dismissed_reminders.txt";
    private static final String THREAD_NAME = "ReminderService-Scanner";
    private static final String LOG_PREFIX = "[ReminderService]";

    // Key formatting constants
    private static final String KEY_SEPARATOR = "#";
    private static final int EXPECTED_SPLIT_LENGTH = 2;

    private final TaskList tasks;
    private final ReminderListener listener;
    private final ScheduledExecutorService scheduler;
    private final Set<String> firedKeys = ConcurrentHashMap.newKeySet();

    private Instant lastScan = Instant.now().minus(STARTUP_GRACE);
    private boolean running = false;

    /**
     * Constructs a reminder service.
     *
     * @param tasks    the task list to scan
     * @param listener callback for delivering reminder events
     */
    public ReminderService(TaskList tasks, ReminderListener listener) {
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.scheduler = createScheduler();
        loadDismissedKeys();
    }

    /**
     * Creates the scheduled executor service for scanning tasks.
     */
    private ScheduledExecutorService createScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts periodic scanning. Safe to call multiple times.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        scheduler.scheduleWithFixedDelay(this::safeScanOnce,
                0,
                SCAN_PERIOD.toSeconds(),
                TimeUnit.SECONDS);
    }

    /**
     * Stops periodic scanning and saves dismissed keys.
     */
    public void stop() {
        running = false;
        scheduler.shutdownNow();
        saveDismissedKeys();
    }

    /**
     * Loads previously dismissed reminder keys from persistent storage.
     */
    private void loadDismissedKeys() {
        Path path = Paths.get(DISMISSED_REMINDERS_FILE);
        if (!Files.exists(path)) {
            return;
        }

        try {
            Set<String> loaded = new HashSet<>(Files.readAllLines(path));
            firedKeys.addAll(loaded);
            logInfo("Loaded " + loaded.size() + " dismissed reminders");
        } catch (IOException e) {
            logError("Failed to load dismissed reminders: " + e.getMessage());
        }
    }

    /**
     * Saves currently dismissed reminder keys to persistent storage.
     */
    private void saveDismissedKeys() {
        Path path = Paths.get(DISMISSED_REMINDERS_FILE);

        try {
            ensureDirectoryExists(path);
            Files.write(path, firedKeys);
            logInfo("Saved " + firedKeys.size() + " dismissed reminders");
        } catch (IOException e) {
            logError("Failed to save dismissed reminders: " + e.getMessage());
        }
    }

    /**
     * Ensures the parent directory exists for the given file path.
     */
    private void ensureDirectoryExists(Path filePath) throws IOException {
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Performs a single scan with exception safety to keep the scheduler alive.
     */
    private void safeScanOnce() {
        try {
            scanOnce();
        } catch (Exception e) {
            logError("Scan failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Scans the task list and delivers reminders for due/starting tasks
     * that have not yet been fired.
     */
    public void scanOnce() {
        final Instant previousScan = lastScan;
        final Instant now = Instant.now();

        for (int i = 0; i < tasks.size(); i++) {
            Task task = getTaskSafely(i);
            if (task != null) {
                checkDeadlineReminder(task, i, previousScan, now);
                checkEventReminder(task, i, previousScan, now);
            }
        }

        lastScan = now;
    }

    /**
     * Safely retrieves a task from the task list, handling concurrent modifications.
     */
    private Task getTaskSafely(int index) {
        try {
            return tasks.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Checks if a deadline task needs a reminder.
     */
    private void checkDeadlineReminder(Task task, int index, Instant previousScan, Instant now) {
        if (!(task instanceof Deadline)) {
            return;
        }

        Optional<LocalDateTime> maybeDue = task.getPrimaryTriggerTime();
        maybeDue.ifPresent(dueTime -> {
            Instant trigger = convertToInstant(dueTime);
            maybeFire(index, task, trigger, ReminderType.DEADLINE_DUE, previousScan, now);
        });
    }

    /**
     * Checks if an event task needs a reminder.
     */
    private void checkEventReminder(Task task, int index, Instant previousScan, Instant now) {
        if (!(task instanceof Event)) {
            return;
        }

        Optional<LocalDateTime> maybeStart = task.getPrimaryTriggerTime();
        maybeStart.ifPresent(startTime -> {
            Instant trigger = convertToInstant(startTime);
            maybeFire(index, task, trigger, ReminderType.EVENT_START, previousScan, now);
        });
    }

    /**
     * Converts LocalDateTime to Instant using system default zone.
     */
    private Instant convertToInstant(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Determines whether to fire a reminder for a specific task based on its trigger time
     * and current system state. A reminder will only be fired if all conditions are met:
     * the trigger time has passed, the reminder hasn't been fired before, and the trigger
     * falls within the startup grace window.
     *
     * @param index the 0-based index of the task in the TaskList
     * @param task the Task object containing the reminder details
     * @param trigger the exact Instant when this reminder should fire
     * @param type the type of reminder (DEADLINE_DUE or EVENT_START)
     * @param previousScan the Instant of the previous scan, used for grace window calculation
     * @param now the current Instant, used to determine if trigger has passed
     */
    private void maybeFire(int index, Task task, Instant trigger, ReminderType type,
                           Instant previousScan, Instant now) {
        String key = createReminderKey(type, index, trigger);

        boolean isOverdue = trigger.isBefore(now);
        boolean isWithinGraceWindow = trigger.isAfter(previousScan.minus(STARTUP_GRACE));
        boolean isNewReminder = firedKeys.add(key);

        if (isOverdue && isWithinGraceWindow && isNewReminder) {
            ReminderEvent event = createReminderEvent(index, task, trigger, type, key);
            listener.onReminder(event);
            saveDismissedKeys();
        }
    }

    /**
     * Creates a reminder event for the given parameters.
     */
    private ReminderEvent createReminderEvent(int index, Task task, Instant trigger,
                                              ReminderType type, String key) {
        LocalDateTime triggerTime = trigger.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return new ReminderEvent(index, task.toString(), triggerTime, type, key);
    }

    /**
     * Creates a unique key for the reminder based on type, index, and trigger time.
     */
    private static String createReminderKey(ReminderType type, int index, Instant trigger) {
        return type.name() + KEY_SEPARATOR + index + KEY_SEPARATOR + trigger.toEpochMilli();
    }

    /**
     * Dismisses a previously delivered reminder so it will not fire again.
     */
    public void dismiss(ReminderEvent event) {
        if (event == null) {
            return;
        }
        firedKeys.add(event.getKey());
        saveDismissedKeys();
    }

    /**
     * Logs an informational message.
     */
    private void logInfo(String message) {
        System.out.println(LOG_PREFIX + " " + message);
    }

    /**
     * Logs an error message.
     */
    private void logError(String message) {
        System.err.println(LOG_PREFIX + " " + message);
    }

    /** Listener interface for receiving reminder events (e.g., GUI popup). */
    public interface ReminderListener {
        void onReminder(ReminderEvent event);
    }

    /** Type of reminder. */
    public enum ReminderType {
        DEADLINE_DUE,
        EVENT_START
    }

    /**
     * Immutable value class describing a reminder to show to the user.
     */
    public static final class ReminderEvent {
        private static final DateTimeFormatter DISPLAY_FORMATTER =
                DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

        private final int taskIndex;
        private final String taskLabel;
        private final LocalDateTime triggerTime;
        private final ReminderType type;
        private final String key;

        /**
         * Constructs a new ReminderEvent describing a reminder to be shown to the user.
         */
        public ReminderEvent(int taskIndex, String taskLabel, LocalDateTime triggerTime,
                             ReminderType type, String key) {
            this.taskIndex = taskIndex;
            this.taskLabel = taskLabel;
            this.triggerTime = triggerTime;
            this.type = type;
            this.key = key;
        }

        public int getTaskIndex() {
            return taskIndex;
        }

        /**
         * Returns the human-readable string representation of the task that triggered this reminder.
         */
        public String getTaskLabel() {
            return taskLabel;
        }

        public LocalDateTime getTriggerTime() {
            return triggerTime;
        }

        public ReminderType getType() {
            return type;
        }

        /**
         * Returns the unique internal identifier used for reminder deduplication and persistence.
         */
        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return type + ": " + taskLabel + " @ " + triggerTime.format(DISPLAY_FORMATTER);
        }
    }
}
