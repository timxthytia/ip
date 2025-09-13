package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a deadline.
 * Stores the task description along with a {@link LocalDateTime} representing
 * the deadline. Provides methods to format the task for storage and for
 * user-friendly display.
 */
public class Deadline extends Task {
    /** Type token used in storage to denote a deadline task. */
    private static final String TYPE_TOKEN = "D";
    /** Delimiter used in storage between fields. */
    private static final String STORAGE_DELIM = " | ";
    /** Display pattern for rendering the due date/time to users. */
    private static final String DISPLAY_PATTERN = "MMM dd yyyy HH:mm";
    /** Formatter for display purposes. */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern(DISPLAY_PATTERN);
    /** Formatter for storage; ISO-8601 is explicit and stable. */
    private static final DateTimeFormatter STORAGE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Due date/time of this deadline. */
    private final LocalDateTime due;

    /**
     * Creates a {@code Deadline} task with the given description and due date/time.
     *
     * @param description the description of the task; must be non-null/non-blank
     * @param due the due date and time of the task; must be non-null
     * @throws IllegalArgumentException if {@code due} is null
     */
    public Deadline(String description, LocalDateTime due) {
        super(description);
        if (due == null) {
            throw new IllegalArgumentException("Due date/time must not be null");
        }
        this.due = due;
    }

    /**
     * Returns a string representation of this {@code Deadline} suitable for storage.
     * The format is:
     * <pre>
     * D | {1 or 0 for completion} | {description} | {due date/time in ISO-8601}
     * </pre>
     *
     * @return the storage string representation of this task
     */
    @Override
    public String toStorageString() {
        // Use isCompleted() accessor to respect encapsulation of Task
        String completedFlag = isCompleted() ? "1" : "0";
        return TYPE_TOKEN + STORAGE_DELIM
                + completedFlag + STORAGE_DELIM
                + getDescription() + STORAGE_DELIM
                + due.format(STORAGE_FORMATTER);
    }

    /**
     * Returns a user-friendly string for this {@code Deadline}.
     * The output includes the task type, completion status, description,
     * and the due date/time formatted as {@value #DISPLAY_PATTERN}.
     *
     * @return the string representation of this task
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + due.format(DISPLAY_FORMATTER) + ")";
    }
}
