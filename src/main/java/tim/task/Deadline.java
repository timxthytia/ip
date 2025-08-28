package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Deadline extends Task {
    private final LocalDateTime due; // store deaedline as LocalDateTime
    public Deadline(String description, LocalDateTime due) {
        super(description);
        this.due = due;
    }

    @Override
    public String toStorageString() {
        return "D | " + (completed ? "1" : "0") + " | " + description + " | " + due.toString();
    }

    @Override
    public String toString() {
        // Create format for toString method
        DateTimeFormatter DISP = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        return "[D]" + super.toString() + " (by: " + due.format(DISP) + ")";
    }
}
