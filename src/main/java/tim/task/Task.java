package tim.task;

public class Task {
    protected String description;
    protected boolean completed;

    Task(String description) {
        this.description = description;
        this.completed = false;
    }

    public void markAsDone() {
        this.completed = true;
    }

    public void markAsUndone() {
        this.completed = false;
    }

    /* Returns line format that is saved to storage, to be overridden in subclasses */
    public String toStorageString() {
        return "? | " + (completed ? "1" : "0") + " | " + description;
    }

    protected String getStatusIcon() {
        return (completed ? "X" : " ");
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
