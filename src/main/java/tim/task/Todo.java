package tim.task;

/**
 * Represents a simple to-do task.
 * A to-do task has only a description and completion status,
 * without any associated date or time.
 */
public class Todo extends Task {

    /** Storage type tag for Todo tasks. */
    private static final String TYPE_TAG = "T";
    /** Delimiter used in storage serialization. */
    private static final String DELIM = " | ";

    /**
     * Creates a new {@code Todo} task with the given description.
     *
     * @param description the description of the to-do task
     * @throws IllegalArgumentException if {@code description} is blank
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns a string representation of this {@code Todo} suitable for storage.
     * Format: {@code T | 1|0 | description}
     *
     * @return the storage string representation of this task
     */
    @Override
    public String toStorageString() {
        return TYPE_TAG + DELIM + (isCompleted() ? "1" : "0") + DELIM + getDescription();
    }

    /**
     * Returns a user-facing string representation of this {@code Todo}.
     * Example: {@code [T][X] read book}
     *
     * @return the display string representation of this task
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
