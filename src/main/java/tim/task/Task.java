package tim.task;

/**
 * Represents a generic task in the task list.
 * A task has a description and a completion status. This class serves as the base
 * class for more specific task subclasses to inherit.
 *
 */
public class Task {
    protected String description;
    protected boolean isCompleted;

    /**
     * Creates a new Task with the specified description.
     * The task is marked as not completed by default.
     *
     * @param description the description of the task.
     */
    Task(String description) {
        this.description = description;
        this.isCompleted = false;
    }

    /**
     * Marks this task as completed.
     */
    public void markAsDone() {
        this.isCompleted = true;
    }

    /**
     * Marks this task as not completed.
     */
    public void markAsUndone() {
        this.isCompleted = false;
    }

    /**
     * Returns a string representation of this task suitable for storage in the save file.
     * This method should be overridden by subclasses to include additional details.
     *
     * @return the storage string representation of the task.
     */
    public String toStorageString() {
        return "? | " + (isCompleted ? "1" : "0") + " | " + description;
    }

    /**
     * Returns the status icon for this task.
     *
     * @return "X" if the task is completed, otherwise a space character.
     */
    protected String getStatusIcon() {
        return (isCompleted ? "X" : " ");
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
