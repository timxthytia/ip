package tim.reminder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
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
 * display a popup, play a sound, etc. This service is self-contained and does not
 * create any new threads beyond an internal scheduler; call {@link #start()} to
 * begin scanning and {@link #stop()} to terminate.</p>
 */
public class ReminderService {

    /** Input formatter helper for logs or debug displays. */
    public static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** How frequently to scan the task list for triggers. */
    public static final Duration SCAN_PERIOD = Duration.ofSeconds(20); // ~15–30s is typical

    /** Default snooze interval applied when user presses "Snooze". */
    public static final Duration SNOOZE_DURATION = Duration.ofMinutes(10);

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

    /**
     * Keys of reminders that are snoozed until a future {@link Instant}.
     */
    private final Map<String, Instant> snoozedUntil = new ConcurrentHashMap<>();

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
        // First run immediately, then on a fixed rate.
        scheduler.scheduleAtFixedRate(this::safeScanOnce,
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
        } catch (Throwable t) { // keep scanning even if one pass fails
            // In production you might log this to a logger; keeping silent here to avoid console noise.
        }
    }

    /**
     * Scans the {@link TaskList} and delivers reminders for due/starting tasks
     * that have not yet been fired (or are not snoozed).
     */
    public void scanOnce() {
        final Instant now = Instant.now();
        final ZoneId zone = ZoneId.systemDefault();

        for (int i = 0; i < tasks.size(); i++) {
            Task task;
            try {
                task = tasks.get(i);
            } catch (Exception e) {
                // If TaskList throws (e.g., index changed during scan), skip this index.
                continue;
            }

            final int idx = i;
            final Task current = task;

            // DEADLINE: trigger at due time
            if (task instanceof Deadline) {
                Optional<LocalDateTime> maybeBy = ((Deadline) task).getPrimaryTriggerTime();
                maybeBy.ifPresent(by -> {
                    Instant trigger = by.atZone(zone).toInstant();
                    maybeFire(idx, current, trigger, ReminderType.DEADLINE_DUE, now);
                });
            }

            // EVENT: trigger at start time
            if (task instanceof Event) {
                Optional<LocalDateTime> maybeStart = ((Event) task).getPrimaryTriggerTime();
                maybeStart.ifPresent(start -> {
                    Instant trigger = start.atZone(zone).toInstant();
                    maybeFire(idx, current, trigger, ReminderType.EVENT_START, now);
                });
            }
        }

        lastScan = now;
    }

    /**
     * Decides whether to fire a reminder for the given task and trigger time.
     */
    private void maybeFire(int index, Task task, Instant trigger, ReminderType type, Instant now) {
        String key = key(type, index, trigger);

        // Respect snooze: if snoozedUntil is in the future, skip for now.
        Instant snoozeUntil = snoozedUntil.get(key);
        if (snoozeUntil != null && snoozeUntil.isAfter(now)) {
            return;
        }

        // Fire if trigger has passed and we haven't already fired this key,
        // and the trigger is not too far in the past (beyond grace window).
        boolean inCatchupWindow = trigger.isAfter(lastScan.minus(STARTUP_GRACE));
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
     * Snoozes a previously delivered reminder by the default {@link #SNOOZE_DURATION}.
     */
    public void snooze(ReminderEvent event) {
        snooze(event, SNOOZE_DURATION);
    }

    /**
     * Snoozes a previously delivered reminder by a custom duration.
     */
    public void snooze(ReminderEvent event, Duration duration) {
        if (event == null || duration == null || duration.isNegative() || duration.isZero()) {
            return;
        }
        Instant until = Instant.now().plus(duration);
        snoozedUntil.put(event.getKey(), until);
        // Allow firing again after snooze expires
        firedKeys.remove(event.getKey());
    }

    /**
     * Dismisses a previously delivered reminder; it will not fire again for the same key.
     */
    public void dismiss(ReminderEvent event) {
        if (event == null) {
            return;
        }
        snoozedUntil.remove(event.getKey());
        firedKeys.add(event.getKey());
    }

    /** Returns an immutable view of snoozed keys (useful for debugging/UX). */
    public Map<String, Instant> getSnoozedUntil() {
        return Collections.unmodifiableMap(snoozedUntil);
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

        /** Internal identifier tying this reminder to a specific task/time. */
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
