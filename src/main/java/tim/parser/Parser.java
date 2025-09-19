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
 */
public class Parser {
    // Date/time formatters
    private static final DateTimeFormatter INPUT_DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    // Command keywords
    private static final String LIST_COMMAND = "list";
    private static final String MARK_COMMAND = "mark ";
    private static final String UNMARK_COMMAND = "unmark ";
    private static final String BYE_COMMAND = "bye";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String DELETE_COMMAND = "delete";
    private static final String FIND_COMMAND = "find ";

    // Format separators
    private static final String BY_SEPARATOR = "/by";
    private static final String FROM_SEPARATOR = "/from";
    private static final String TO_SEPARATOR = "/to";

    // Command substring lengths
    private static final int TODO_PREFIX_LENGTH = 4;
    private static final int MARK_PREFIX_LENGTH = 5;
    private static final int UNMARK_PREFIX_LENGTH = 7;
    private static final int DELETE_PREFIX_LENGTH = 7;
    private static final int FIND_PREFIX_LENGTH = 5;

    // Error messages
    private static final String ERROR_PROVIDE_TASK_NUMBER = "OOPS!!! Provide a task number.";
    private static final String ERROR_TASK_NUMBER_INTEGER = "OOPS!!! Task number must be an integer.";
    private static final String ERROR_UNKNOWN_COMMAND = "OOPS!!! I'm sorry, but I don't know what that means :-(";
    private static final String ERROR_DEADLINE_FORMAT = "Deadline format: deadline <desc> /by <due date>.";
    private static final String ERROR_EVENT_FORMAT = "Event format: event <desc> /from <start> /to <end>.";
    private static final String ERROR_DATE_FORMAT = "Use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 1800).";
    private static final String ERROR_DATE_FORMAT_EVENT = "Use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 0900).";

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

        throw new IllegalArgumentException("Unrecognized date/time (use yyyy-MM-dd or yyyy-MM-dd HHmm): " + s);
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

        if (input.equals(LIST_COMMAND)) {
            return new ListCommand();
        } else if (input.equals(BYE_COMMAND)) {
            return new ExitCommand();
        } else if (input.startsWith(MARK_COMMAND)) {
            return createMarkCommand(input);
        } else if (input.startsWith(UNMARK_COMMAND)) {
            return createUnmarkCommand(input);
        } else if (input.startsWith(TODO_COMMAND)) {
            return createTodoCommand(input);
        } else if (input.startsWith(DEADLINE_COMMAND)) {
            return parseDeadline(input);
        } else if (input.startsWith(EVENT_COMMAND)) {
            return parseEvent(input);
        } else if (input.startsWith(DELETE_COMMAND)) {
            return createDeleteCommand(input);
        } else if (input.startsWith(FIND_COMMAND)) {
            return createFindCommand(input);
        } else {
            throw new TimException(ERROR_UNKNOWN_COMMAND);
        }
    }

    /**
     * Creates a MarkCommand from user input.
     */
    private static Command createMarkCommand(String input) throws TimException {
        return new MarkCommand(parseIndex(input.substring(MARK_PREFIX_LENGTH)));
    }

    /**
     * Creates an UnmarkCommand from user input.
     */
    private static Command createUnmarkCommand(String input) throws TimException {
        return new UnmarkCommand(parseIndex(input.substring(UNMARK_PREFIX_LENGTH)));
    }

    /**
     * Creates an AddTodoCommand from user input.
     */
    private static Command createTodoCommand(String input) throws TimException {
        return new AddTodoCommand(input.substring(TODO_PREFIX_LENGTH).trim());
    }

    /**
     * Creates a DeleteCommand from user input.
     */
    private static Command createDeleteCommand(String input) throws TimException {
        if (input.equals(DELETE_COMMAND)) {
            throw new TimException(ERROR_PROVIDE_TASK_NUMBER);
        }
        return new DeleteCommand(parseIndex(input.substring(DELETE_PREFIX_LENGTH)));
    }

    /**
     * Creates a FindCommand from user input.
     */
    private static Command createFindCommand(String input) throws TimException {
        String keyword = input.substring(FIND_PREFIX_LENGTH).trim();
        return new FindCommand(keyword);
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
        String trimmed = s.trim();

        if (trimmed.isBlank()) {
            throw new TimException(ERROR_PROVIDE_TASK_NUMBER);
        }

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new TimException(ERROR_TASK_NUMBER_INTEGER);
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

        String body = input.substring(DEADLINE_COMMAND.length()).trim();
        validateDeadlineFormat(body);

        String[] parts = body.split(BY_SEPARATOR, 2);
        validateDeadlineParts(parts);

        String desc = parts[0].trim();
        String date = parts[1].trim();
        LocalDateTime dueDateTime = parseDeadlineDateTime(date);

        assert dueDateTime != null : "Parser.parseDeadline: dueDateTime is null";
        return new AddDeadlineCommand(desc, dueDateTime);
    }

    /**
     * Validates the format of a deadline command body.
     */
    private static void validateDeadlineFormat(String body) throws TimException {
        if (!body.contains(BY_SEPARATOR)) {
            throw new TimException(ERROR_DEADLINE_FORMAT);
        }
    }

    /**
     * Validates the parts of a deadline command after splitting.
     */
    private static void validateDeadlineParts(String[] parts) throws TimException {
        if (parts.length < 2 || parts[0].trim().isBlank() || parts[1].trim().isBlank()) {
            throw new TimException(ERROR_DEADLINE_FORMAT);
        }
    }

    /**
     * Parses the date/time portion of a deadline command.
     */
    private static LocalDateTime parseDeadlineDateTime(String date) throws TimException {
        try {
            return parseStrictDateOrDateTime(date);
        } catch (IllegalArgumentException ex) {
            throw new TimException(ERROR_DATE_FORMAT);
        }
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

        String body = input.substring(EVENT_COMMAND.length()).trim();
        validateEventFormat(body);

        String[] dateSplit = body.split(FROM_SEPARATOR, 2);
        validateFromSplit(dateSplit); // Extract /from date

        String desc = dateSplit[0].trim();
        String[] toSplit = dateSplit[1].split(TO_SEPARATOR, 2);
        validateToSplit(toSplit); // Extract /to date

        String start = toSplit[0].trim();
        String end = toSplit[1].trim();
        validateEventComponents(desc, start, end);

        LocalDateTime startDT = parseEventDateTime(start);
        LocalDateTime endDT = parseEventDateTime(end);

        assert startDT != null : "Parser.parseEvent: startDT is null";
        assert endDT != null : "Parser.parseEvent: endDT is null";
        assert !endDT.isBefore(startDT) : "Parser.parseEvent: end before start";

        return new AddEventCommand(desc, startDT, endDT);
    }

    /**
     * Validates the format of an event command body.
     */
    private static void validateEventFormat(String body) throws TimException {
        if (!body.contains(FROM_SEPARATOR) || !body.contains(TO_SEPARATOR)) {
            throw new TimException(ERROR_EVENT_FORMAT);
        }
    }

    /**
     * Validates the first split of an event command (before /from).
     */
    private static void validateFromSplit(String[] dateSplit) throws TimException {
        if (dateSplit.length < 2) {
            throw new TimException(ERROR_EVENT_FORMAT);
        }
    }

    /**
     * Validates the second split of an event command (after /to).
     */
    private static void validateToSplit(String[] toSplit) throws TimException {
        if (toSplit.length < 2) {
            throw new TimException(ERROR_EVENT_FORMAT);
        }
    }

    /**
     * Validates that all event components are non-blank.
     */
    private static void validateEventComponents(String desc, String start, String end) throws TimException {
        if (desc.isBlank() || start.isBlank() || end.isBlank()) {
            throw new TimException(ERROR_EVENT_FORMAT);
        }
    }

    /**
     * Parses the date/time portion of an event command.
     */
    private static LocalDateTime parseEventDateTime(String dateTime) throws TimException {
        try {
            return parseStrictDateOrDateTime(dateTime);
        } catch (IllegalArgumentException ex) {
            throw new TimException(ERROR_DATE_FORMAT_EVENT);
        }
    }
}
