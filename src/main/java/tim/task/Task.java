package tim.task;

/**
 * Represents a generic task in the task list.
 * A task has a description and a completion status. This class serves as the base
 * class for more specific task subclasses to inherit.
 */
public class Task {

    /** Storage delimiter used when serializing tasks. */
    protected static final String STORAGE_DELIM = " | ";

    /** Storage token used when the concrete task type is not specified here. */
    protected static final String STORAGE_UNKNOWN_TYPE = "?";

    /** UI symbol for a completed task. */
    private static final String STATUS_DONE = "X";
    /** UI symbol for an incomplete task. */
    private static final String STATUS_NOT_DONE = " ";

    /** Storage flag for completed status. */
    private static final String COMPLETED_FLAG_TRUE = "1";
    /** Storage flag for incomplete status. */
    private static final String COMPLETED_FLAG_FALSE = "0";

    /** Human-readable description of the task. */
    private final String description;

    /** Whether the task has been completed. */
    private boolean completed;

    /**
     * Creates a new Task with the specified description.
     * The task is marked as not completed by default.
     *
     * @param description the description of the task.
     * @throws IllegalArgumentException if {@code description} is null or blank.
     */
    Task(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be empty or blank.");
        }
        this.description = description;
        this.completed = false;
    }

    /**
     * Marks this task as completed.
     */
    public void markAsDone() {
        this.completed = true;
    }

    /**
     * Marks this task as not completed.
     */
    public void markAsUndone() {
        this.completed = false;
    }

    /**
     * Returns the plain-text description of this task.
     *
     * @return the description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the task is completed.
     *
     * @return true if completed; false otherwise.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns a string representation of this task suitable for storage in the save file.
     * Subclasses are expected to override this to provide the appropriate leading type token,
     * but this base implementation uses a generic token to avoid magic strings.
     *
     * @return the storage string representation of the task.
     */
    public String toStorageString() {
        return STORAGE_UNKNOWN_TYPE + STORAGE_DELIM + getCompletedFlag() + STORAGE_DELIM + description;
    }

    /**
     * Returns the status icon for this task.
     *
     * @return "X" if the task is completed, otherwise a space character.
     */
    protected String getStatusIcon() {
        return completed ? STATUS_DONE : STATUS_NOT_DONE;
    }

    /**
     * Returns the flag used to represent completion status in storage.
     */
    private String getCompletedFlag() {
        return completed ? COMPLETED_FLAG_TRUE : COMPLETED_FLAG_FALSE;
    }

    /**
     * Returns a string representation of this task for user display.
     * The output includes the completion status and description.
     *
     * @return the string representation of this task.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
