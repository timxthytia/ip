package tim.task;

public class Todo extends Task {
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toStorageString() {
        return "T | " + (completed ? "1" : "0") + " | " + description;
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
