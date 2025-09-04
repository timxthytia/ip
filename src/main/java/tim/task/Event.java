package tim.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a scheduled event task.
 * Stores the task description, a start date/time, and an end date/time.
 * Provides methods to format the event for storage and for user-friendly display.
 *
 */
public class Event extends Task {
    private final LocalDateTime start;
    private final LocalDateTime end;
    /**
     * Creates an Event task with the given description, start time, and end time.
     *
     * @param description the description of the event.
     * @param start the start date and time of the event.
     * @param end the end date and time of the event.
     */
    public Event(String description, LocalDateTime start, LocalDateTime end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    /**
     * Returns a string representation of this Event suitable for storage.
     * The format used is:
     * E | {1 or 0 for completion} | {description} | {start date/time} to {end date/time}
     *
     * @return the storage string representation of this event.
     */
    @Override
    public String toStorageString() {
        return "E | " + (completed ? "1" : "0") + " | " + description
                + " | " + start.toString() + " to " + end.toString();
    }

    /**
     * Returns a string representation of this Event for user display.
     * The output includes the task type, completion status, description,
     * and the formatted start and end date/times.
     * If the start and end times are equal, shows a single "on" date/time.
     *
     * @return the string representation of this event.
     */
    @Override
    public String toString() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        if (start.equals(end)) {
            return "[E]" + super.toString() + " (on: " + start.format(displayFormatter) + ")";
        } else {
            return "[E]" + super.toString() + " (from: " + start.format(displayFormatter)
                    + " to: " + end.format(displayFormatter) + ")";
        }
    }
}
