package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a deadline.
 * Stores the task description along with a LocalDateTime representing
 * the deadline. Provides methods to format the task for storage and for
 * user-friendly display.
 *
 */
public class Deadline extends Task {
    private final LocalDateTime due; // store deadline as LocalDateTime
    /**
     * Creates a Deadline task with the given description and due date/time.
     *
     * @param description the description of the task.
     * @param due the due date and time of the task.
     */
    public Deadline(String description, LocalDateTime due) {
        super(description);
        this.due = due;
    }

    @Override
    /**
     * Returns a string representation of this Deadline task suitable for storage.
     * The format used is:
     * D | {1 or 0 for completion} | {description} | {due date/time}
     *
     * @return the storage string representation of this task.
     */
    public String toStorageString() {
        return "D | " + (completed ? "1" : "0") + " | " + description + " | " + due.toString();
    }

    @Override
    /**
     * Returns a string representation of this Deadline task for user display.
     * The output includes the task type, completion status, description,
     * and the due date/time formatted as "MMM dd yyyy HH:mm".
     *
     * @return the string representation of this task.
     */
    public String toString() {
        // Create format for toString method
        DateTimeFormatter DISP = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        return "[D]" + super.toString() + " (by: " + due.format(DISP) + ")";
    }
}
