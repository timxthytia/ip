package tim.reminder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
 * <p>Reminders are delivered to a {@link ReminderListener} (e.g., GUI) which may
 * display a popup.</p>
 */
public class ReminderService {

    /** How frequently to scan the task list for triggers. */
    public static final Duration SCAN_PERIOD = Duration.ofSeconds(20); // ~15–30s is typical

    /**
     * On startup, also fire any reminders that were missed within this grace window
     * (e.g., app was closed at the trigger moment).
     */
    public static final Duration STARTUP_GRACE = Duration.ofHours(24);

    private final TaskList tasks;
    private final ReminderListener listener;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ReminderService-Scanner");
        t.setDaemon(true);
        return t;
    });

    /**
     * Keys of reminders already delivered (to ensure one-time firing).
     */
    private final Set<String> firedKeys = ConcurrentHashMap.newKeySet();

    /** Last time we completed a scan. Used for catching up missed triggers. */
    private volatile Instant lastScan = Instant.now().minus(STARTUP_GRACE);

    private volatile boolean running = false;

    /**
     * Constructs a reminder service.
     *
     * @param tasks    the task list to scan
     * @param listener callback for delivering reminder events
     */
    public ReminderService(TaskList tasks, ReminderListener listener) {
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    /** Starts periodic scanning. Safe to call once. */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        // First run immediately, then with a fixed delay (avoids drift if scans take time).
        scheduler.scheduleWithFixedDelay(this::safeScanOnce,
                0,
                SCAN_PERIOD.toSeconds(),
                TimeUnit.SECONDS);
    }

    /** Stops periodic scanning and releases resources. */
    public synchronized void stop() {
        running = false;
        scheduler.shutdownNow();
    }

    /** Performs a single scan with exception safety (keeps scheduler alive). */
    private void safeScanOnce() {
        try {
            scanOnce();
        } catch (Exception e) { // keep scanning even if one pass fails
            // Log clearly; do not fail silently.
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
                // If TaskList throws (e.g., index changed during scan), skip this index.
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
     * Decides whether to fire a reminder for the given task and trigger time.
     */
    private void maybeFire(int index, Task task, Instant trigger, ReminderType type,
                           Instant previousScan, Instant now) {
        String key = key(type, index, trigger);

        // Fire if trigger has passed, and we haven't already fired this key,
        // and the trigger is not too far in the past (beyond grace window).
        boolean inCatchupWindow = trigger.isAfter(previousScan.minus(STARTUP_GRACE));
        if (trigger.isBefore(now) && inCatchupWindow && firedKeys.add(key)) {
            ReminderEvent evt = new ReminderEvent(index, task.toString(),
                    trigger.atZone(ZoneId.systemDefault()).toLocalDateTime(), type, key);
            listener.onReminder(evt);
        }
    }

    private static String key(ReminderType type, int index, Instant trigger) {
        return type.name() + "#" + index + "#" + trigger.toEpochMilli();
    }

    /**
     * Dismisses a previously delivered reminder; it will not fire again for the same key.
     */
    public void dismiss(ReminderEvent event) {
        if (event == null) {
            return;
        }
        firedKeys.add(event.getKey());
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
     * Immutable value that describes a reminder to show to the user.
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

        public String getTaskLabel() {
            return taskLabel;
        }

        public LocalDateTime getTriggerTime() {
            return triggerTime;
        }

        public ReminderType getType() {
            return type;
        }

        /** Internal identifier linking this reminder to a specific task/time. */
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
