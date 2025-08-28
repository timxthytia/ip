package tim.task;

/**
 * Represents a simple to-do task.
 * A to-do task has only a description and completion status,
 * without any associated date or time.
 *
 */

public class Todo extends Task {
    /**
     * Creates a new Todo task with the given description.
     *
     * @param description the description of the to-do task.
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns a string representation of this Todo task suitable for storage.
     * The format used is:
     * T | {1 or 0 for completion} | {description}
     *
     * @return the storage string representation of this task.
     */
    @Override
    public String toStorageString() {
        return "T | " + (completed ? "1" : "0") + " | " + description;
    }

    /**
     * Returns a string representation of this Todo task for user display.
     * The output includes the task type, completion status, and description.
     *
     * @return the string representation of this task.
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
