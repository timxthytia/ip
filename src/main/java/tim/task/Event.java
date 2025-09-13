package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a scheduled event task.
 * Stores the task description, a start date/time, and an end date/time.
 * Provides methods to format the event for storage and user-friendly display.
 */
public class Event extends Task {

    /** Code used in storage to denote an Event. */
    private static final String TYPE_CODE = "E";
    /** Separator used between storage fields. */
    private static final String STORAGE_SEP = " | ";
    /** Separator used between start and end in storage. */
    private static final String RANGE_SEP = " to ";

    /** Display formatter for user-facing date/time. */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
    /** Storage formatter (explicit ISO) for start/end. */
    private static final DateTimeFormatter STORAGE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LocalDateTime start;
    private final LocalDateTime end;

    /**
     * Creates an Event task with the given description, start time, and end time.
     *
     * @param description the description of the event
     * @param start       the start date and time of the event (inclusive)
     * @param end         the end date and time of the event (inclusive)
     * @throws IllegalArgumentException if {@code start} or {@code end} is {@code null},
     *                                  or if {@code end} is before {@code start}
     */
    public Event(String description, LocalDateTime start, LocalDateTime end) {
        super(description);
        if (start == null) {
            throw new IllegalArgumentException("Event start must not be null.");
        }
        if (end == null) {
            throw new IllegalArgumentException("Event end must not be null.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Event end must not be before start.");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Returns a string representation of this Event suitable for storage.
     * <p>Format:</p>
     * <pre>
     * E | {1 or 0 for completion} | {description} | {start} to {end}
     * </pre>
     *
     * @return the storage string representation of this event
     */
    @Override
    public String toStorageString() {
        final String status = isCompleted() ? "1" : "0";
        final String range = start.format(STORAGE_FORMATTER) + RANGE_SEP + end.format(STORAGE_FORMATTER);
        return String.join(STORAGE_SEP, TYPE_CODE, status, getDescription(), range);
    }

    /**
     * Returns a string representation of this Event for user display.
     * The output includes the task type, completion status, description,
     * and the formatted start and end date/times.
     * If the start and end times are equal, shows a single "on" date/time.
     *
     * @return the string representation of this event
     */
    @Override
    public String toString() {
        final String base = "[E]" + super.toString();
        if (start.equals(end)) {
            return String.format("%s (on: %s)", base, start.format(DISPLAY_FORMATTER));
        }
        return String.format("%s (from: %s to: %s)",
                base, start.format(DISPLAY_FORMATTER), end.format(DISPLAY_FORMATTER));
    }
}
