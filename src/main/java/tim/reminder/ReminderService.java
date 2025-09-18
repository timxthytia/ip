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

    // Scan frequency to scan task list for triggers
    public static final Duration SCAN_PERIOD = Duration.ofSeconds(20);

    // On startup, fire any reminders that were missed within grace window
    public static final Duration STARTUP_GRACE = Duration.ofHours(24);

    // File to store dismissed reminder keys
    private static final String DISMISSED_REMINDERS_FILE = "data/dismissed_reminders.txt";

    private final TaskList tasks;
    private final ReminderListener listener;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ReminderService-Scanner");
        t.setDaemon(true);
        return t;
    });

    // Keys of reminders that have already been fired.
    // Use ConcurrentHashMap for thread safety without synchronization.
    private final Set<String> firedKeys = ConcurrentHashMap.newKeySet();

    // Last time we completed a scan, used for catching up missed triggers.
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
        loadDismissedKeys();
    }

    /**
     * Starts periodic scanning. Safe to call once.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        // First run immediately, then subsequent runs with a fixed delay.
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
     * Loads previously dismissed reminder keys from file.
     */
    private void loadDismissedKeys() {
        Path path = Paths.get(DISMISSED_REMINDERS_FILE);
        if (!Files.exists(path)) {
            return; // No previously dismissed reminders
        }

        try {
            Set<String> loaded = new HashSet<>(Files.readAllLines(path));
            firedKeys.addAll(loaded);
            System.out.println("[ReminderService] Loaded " + loaded.size() + " dismissed reminders");
        } catch (IOException e) {
            System.err.println("[ReminderService] Failed to load dismissed reminders: " + e.getMessage());
            // Continue with empty set
        }
    }

    /**
     * Saves currently dismissed reminder keys to file.
     */
    private void saveDismissedKeys() {
        Path path = Paths.get(DISMISSED_REMINDERS_FILE);

        try {
            Files.createDirectories(path.getParent()); // Ensure data directory exists
            Files.write(path, firedKeys); // Write all dismissed keys, one per line
            System.out.println("[ReminderService] Saved " + firedKeys.size() + " dismissed reminders");
        } catch (IOException e) {
            System.err.println("[ReminderService] Failed to save dismissed reminders: " + e.getMessage());
        }
    }

    /**
     * Performs a single scan with exception safety (keeps scheduler alive).
     */
    private void safeScanOnce() {
        try {
            scanOnce();
        } catch (Exception e) { // keep scanning even if one pass fails
            System.err.println("[ReminderService] Scan failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Scans the {@link TaskList} and delivers reminders for due/starting tasks
     * that have not yet been fired.
     */
    public void scanOnce() {
        final Instant previousScan = lastScan;
        final Instant now = Instant.now();
        final ZoneId zone = ZoneId.systemDefault();

        for (int i = 0; i < tasks.size(); i++) {
            Task task;
            try {
                task = tasks.get(i);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            final int idx = i;
            final Task current = task;

            // DEADLINE: trigger at due time
            if (task instanceof Deadline) {
                Optional<LocalDateTime> maybeBy = (task).getPrimaryTriggerTime();
                maybeBy.ifPresent(by -> {
                    Instant trigger = by.atZone(zone).toInstant();
                    maybeFire(idx, current, trigger, ReminderType.DEADLINE_DUE, previousScan, now);
                });
            }

            // EVENT: trigger at start time
            if (task instanceof Event) {
                Optional<LocalDateTime> maybeStart = (task).getPrimaryTriggerTime();
                maybeStart.ifPresent(start -> {
                    Instant trigger = start.atZone(zone).toInstant();
                    maybeFire(idx, current, trigger, ReminderType.EVENT_START, previousScan, now);
                });
            }
        }

        lastScan = now;
    }

    /**
     * Determines whether to fire a reminder for a specific task based on its trigger time
     * and current system state. A reminder will only be fired if all conditions are met:
     * the trigger time has passed, the reminder hasn't been fired before, and the trigger
     * falls within the startup grace window.
     *
     * <p>This method implements the core reminder firing logic with deduplication to ensure
     * one-time delivery. When a reminder is fired, it is immediately persisted to prevent
     * re-firing across application sessions.</p>
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
        String key = key(type, index, trigger);

        // Fire if trigger has passed, and we haven't already fired this key,
        // and the trigger is within the grace window.
        boolean inCatchupWindow = trigger.isAfter(previousScan.minus(STARTUP_GRACE));
        if (trigger.isBefore(now) && inCatchupWindow && firedKeys.add(key)) {
            ReminderEvent evt = new ReminderEvent(index, task.toString(),
                    trigger.atZone(ZoneId.systemDefault()).toLocalDateTime(), type, key);
            listener.onReminder(evt);

            // Save immediately when a new reminder is fired
            saveDismissedKeys();
        }
    }

    private static String key(ReminderType type, int index, Instant trigger) {
        return type.name() + "#" + index + "#" + trigger.toEpochMilli();
    }

    /**
     * Dismisses a previously delivered reminder, so that it will not fire again for the same key.
     */
    public void dismiss(ReminderEvent event) {
        if (event == null) {
            return;
        }
        firedKeys.add(event.getKey());
        saveDismissedKeys(); // Persist immediately on dismissal
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
     * Immutable, nested ReminderEvent class that describes a reminder to show to the user.
     */
    public static final class ReminderEvent {
        private final int taskIndex;
        private final String taskLabel;
        private final LocalDateTime triggerTime;
        private final ReminderType type;
        private final String key;

        /**
         * Constructs a new {@code ReminderEvent} describing a reminder to be shown to the user.
         *
         * @param taskIndex   the index of the task in the {@link TaskList}
         * @param taskLabel   the label or string representation of the task
         * @param triggerTime the date and time when the reminder should be triggered
         * @param type        the type of reminder (e.g., deadline due or event start)
         * @param key         the unique key identifying this reminder event
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
         * This label includes the task type, completion status, description, and any
         * associated dates (e.g., "[D][ ] submit assignment (by: Oct 15 2024 14:00)").
         *
         * @return the formatted task string as it appears in the GUI.
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
         * The key format is "{@code TYPE#INDEX#TIMESTAMP}" (e.g., "DEADLINE_DUE#2#1726621680000")
         * and combines the reminder type, task index, and trigger time in milliseconds.
         *
         * <p>This key serves multiple purposes:</p>
         * <ul>
         *   <li>Prevents duplicate reminders from firing multiple times</li>
         *   <li>Enables persistence of dismissed reminders across application sessions</li>
         *   <li>Links dismissal actions back to the specific reminder instance</li>
         * </ul>
         *
         * @return the unique reminder key used for deduplication and persistence
         */
        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return type + ": " + taskLabel + " @ "
                    + triggerTime.format(DateTimeFormatter.ofPattern("MMM d yyyy HH:mm"));
        }
    }
}
