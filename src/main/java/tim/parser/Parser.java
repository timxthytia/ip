package tim.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import tim.command.AddDeadlineCommand;
import tim.command.AddEventCommand;
import tim.command.AddTodoCommand;
import tim.command.Command;
import tim.command.DeleteCommand;
import tim.command.ExitCommand;
import tim.command.FindCommand;
import tim.command.ListCommand;
import tim.command.MarkCommand;
import tim.command.UnmarkCommand;
import tim.exception.TimException;

/**
 * Responsible for interpreting user input and converting it into commands.
 * Provides methods to parse full user commands into specific Command
 * objects, as well as utility methods to parse dates and times in strict formats.
 *
 */
public class Parser {
    private static final DateTimeFormatter INPUT_DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * Parses a string into a LocalDateTime, using strict date and date-time formats.
     * Accepts inputs in "yyyy-MM-dd" (default to 0000) or
     * "yyyy-MM-dd HHmm" (with time). Falls back to default parsing if possible.
     *
     * @param s the string to parse.
     * @return the parsed LocalDateTime.
     * @throws IllegalArgumentException if the input cannot be parsed.
     */
    public static LocalDateTime parseStrictDateOrDateTime(String s) {
        assert s != null : "Parser.parseStrictDateOrDateTime: input is null";
        String trimmed = s.trim();
        assert !trimmed.isEmpty() : "Parser.parseStrictDateOrDateTime: input is blank";
        try {
            return LocalDateTime.parse(trimmed, INPUT_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            // ignore and try next format
        }
        try {
            LocalDate d = LocalDate.parse(trimmed, INPUT_DATE_ONLY);
            return d.atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // ignore and try ISO default parsing
        }
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {
            // ignore and throw a clearer message below
        }
        throw new IllegalArgumentException(
                "Unrecognized date/time (use yyyy-MM-dd or yyyy-MM-dd HHmm): " + s);
    }

    /**
     * Parses a user input string into a Command object.
     *
     * @param input the full command entered by the user.
     * @return the corresponding Command object.
     * @throws TimException if the command is invalid or cannot be understood.
     */
    public static Command parse(String input) throws TimException {
        assert input != null : "Parser.parse: input is null";
        if (input.equals("list")) {
            return new ListCommand();
        } else if (input.startsWith("mark ")) {
            return new MarkCommand(parseIndex(input.substring(5)));
        } else if (input.startsWith("unmark ")) {
            return new UnmarkCommand(parseIndex(input.substring(7)));
        } else if (input.equals("bye")) {
            return new ExitCommand();
        } else if (input.startsWith("todo")) {
            return new AddTodoCommand(input.substring(4).trim());
        } else if (input.startsWith("deadline")) {
            return parseDeadline(input);
        } else if (input.startsWith("event")) {
            return parseEvent(input);
        } else if (input.equals("delete")) {
            throw new TimException("OOPS!!! Provide a task number.");
        } else if (input.startsWith("delete ")) {
            return new DeleteCommand(parseIndex(input.substring(7)));
        } else if (input.startsWith("find ")) {
            String keyword = input.substring(5).trim();
            return new FindCommand(keyword);
        }
        throw new TimException("OOPS!!! I'm sorry, but I don't know what that means :-(");
    }

    /**
     * Parses a string into a task index integer.
     *
     * @param s the string containing the index.
     * @return the parsed integer index.
     * @throws TimException if the input is blank or not a valid integer.
     */
    private static int parseIndex(String s) throws TimException {
        assert s != null : "Parser.parseIndex: input is null";
        String t = s.trim();
        if (t.isBlank()) {
            throw new TimException("OOPS!!! Provide a task number.");
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new TimException("OOPS!!! Task number must be an integer.");
        }
    }

    /**
     * Parses a deadline command string into an AddDeadlineCommand.
     *
     * @param input the full deadline command entered by the user.
     * @return the AddDeadlineCommand created from the input.
     * @throws TimException if the input format is invalid or the date cannot be parsed.
     */
    private static Command parseDeadline(String input) throws TimException {
        assert input != null : "Parser.parseDeadline: input is null";
        String body = input.substring("deadline".length()).trim();
        if (!body.contains("/by")) {
            throw new TimException("Deadline format: deadline <desc> /by <due date>.");
        }
        String[] parts = body.split("/by", 2);
        if (parts.length < 2 || parts[0].trim().isBlank() || parts[1].trim().isBlank()) {
            throw new TimException("Deadline format: deadline <desc> /by <due date>.");
        }
        String desc = parts[0].trim();
        String date = parts[1].trim();
        LocalDateTime dueDateTime;
        try {
            dueDateTime = parseStrictDateOrDateTime(date);
        } catch (IllegalArgumentException ex) {
            throw new TimException("Use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 1800).");
        }
        assert dueDateTime != null : "Parser.parseDeadline: dueDateTime is null";
        return new AddDeadlineCommand(desc, dueDateTime);
    }

    /**
     * Parses an event command string into an AddEventCommand.
     *
     * @param input the full event command entered by the user.
     * @return the AddEventCommand created from the input.
     * @throws TimException if the input format is invalid or the dates cannot be parsed.
     */
    private static Command parseEvent(String input) throws TimException {
        assert input != null : "Parser.parseEvent: input is null";
        String body = input.substring("event".length()).trim();
        if (!body.contains("/from") || !body.contains("/to")) {
            throw new TimException("Event format: event <desc> /from <start> /to <end>.");
        }
        String[] dateSplit = body.split("/from", 2);
        if (dateSplit.length < 2) {
            throw new TimException("Event format: event <desc> /from <start> /to <end>.");
        }
        String desc = dateSplit[0].trim();
        String[] toSplit = dateSplit[1].split("/to", 2);
        if (toSplit.length < 2) {
            throw new TimException("Event format: event <desc> /from <start> /to <end>.");
        }
        String start = toSplit[0].trim();
        String end = toSplit[1].trim();
        if (desc.isBlank() || start.isBlank() || end.isBlank()) {
            throw new TimException("Event format: event <desc> /from <start> /to <end>.");
        }

        LocalDateTime startDT;
        LocalDateTime endDT;
        try {
            startDT = parseStrictDateOrDateTime(start);
            endDT = parseStrictDateOrDateTime(end);
        } catch (IllegalArgumentException ex) {
            throw new TimException("Use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 0900).");
        }
        assert startDT != null : "Parser.parseEvent: startDT is null";
        assert endDT != null : "Parser.parseEvent: endDT is null";
        assert !endDT.isBefore(startDT) : "Parser.parseEvent: end before start";
        return new AddEventCommand(desc, startDT, endDT);
    }
}
