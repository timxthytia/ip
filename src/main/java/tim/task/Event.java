package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event extends Task {
    private final LocalDateTime start;
    private final LocalDateTime end;
    public Event(String description, LocalDateTime start, LocalDateTime end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toStorageString() {
        return "E | " + (completed ? "1" : "0") + " | " + description +
                " | " + start.toString() + " to " + end.toString();
    }

    @Override
    public String toString() {
        DateTimeFormatter DISP = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        if (start.equals(end)) {
            return "[E]" + super.toString() + " (on: " + start.format(DISP) + ")";
        } else {
            return "[E]" + super.toString() + " (from: " + start.format(DISP) + " to: " + end.format(DISP) + ")";
        }
    }
}
