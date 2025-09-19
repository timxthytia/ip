package tim.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import tim.exception.TimException;
import tim.parser.Parser;
import tim.task.Deadline;
import tim.task.Event;
import tim.task.Task;
import tim.task.TaskList;
import tim.task.Todo;

/**
 * Handles reading and writing tasks to a save file on disk.
 * The Storage class is responsible for persisting the user's task list across program runs.
 * It loads tasks from the given file when the application starts, and saves the current tasks
 * back to the file whenever changes are made.
 */
public class Storage {
    // File format constants
    private static final String FIELD_DELIMITER_REGEX = "\\s*\\|\\s*";
    private static final String TIME_RANGE_DELIMITER_REGEX = "\\s*to\\s*";

    // Task type identifiers
    private static final String TODO_TYPE_CODE = "T";
    private static final String DEADLINE_TYPE_CODE = "D";
    private static final String EVENT_TYPE_CODE = "E";

    // Task completion flags
    private static final String COMPLETED_FLAG = "1";
    private static final String INCOMPLETE_FLAG = "0";

    // Minimum required fields for different task types
    private static final int MIN_FIELDS_BASE = 3; // type, done, description
    private static final int MIN_FIELDS_DEADLINE = 4; // + due date
    private static final int MIN_FIELDS_EVENT = 4; // + time range
    private static final int EXPECTED_TIME_RANGE_PARTS = 2; // start to end

    // Logging prefix
    private static final String LOG_PREFIX = "[Storage]";

    private final Path dataDirectory;
    private final Path dataFile;

    /**
     * Creates a new Storage object with the given file path.
     *
     * @param filePath the path of the file to save/load tasks from
     */
    public Storage(String filePath) {
        Path path = Paths.get(filePath);
        this.dataDirectory = determineDataDirectory(path);
        this.dataFile = path;
    }

    /**
     * Determines the data directory from the given file path.
     * @param filePath the file path to extract the directory from
     * @return the parent directory of the file, or current directory if no parent exists
     */
    private Path determineDataDirectory(Path filePath) {
        Path parent = filePath.getParent();
        return parent != null ? parent : Paths.get(".");
    }

    /**
     * Loads tasks from the data file into a TaskList.
     * If the file or its parent directory does not exist, this method creates the directory
     * and returns an empty list.
     *
     * @return a TaskList containing the tasks loaded from file
     * @throws TimException if an I/O error occurs when preparing the data location or reading the file
     */
    public TaskList load() throws TimException {
        TaskList taskList = new TaskList();

        try {
            ensureDataDirectoryExists();

            if (!Files.exists(dataFile)) {
                return taskList;
            }

            loadTasksFromFile(taskList);
        } catch (IOException e) {
            throw new TimException("Could not load tasks: " + e.getMessage(), e);
        }

        return taskList;
    }

    /**
     * Loads tasks from the file into the given task list.
     * @param taskList the TaskList to populate with loaded tasks
     * @throws IOException if an I/O error occurs while reading the file
     */
    private void loadTasksFromFile(TaskList taskList) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = parseLineToTask(line);
                if (task != null) {
                    taskList.add(task);
                }
            }
        }
    }

    /**
     * Saves the given TaskList to the data file. Creates the data directory if needed.
     *
     * @param tasks the tasks to save
     */
    public void save(TaskList tasks) {
        try {
            ensureDataDirectoryExists();
            saveTasksToFile(tasks);
        } catch (IOException e) {
            logError("Could not save tasks: " + e.getMessage());
        }
    }

    /**
     * Writes all tasks to the data file.
     *
     * @param tasks the tasks to save to file.
     */
    private void saveTasksToFile(TaskList tasks) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
            for (Task task : tasks.asList()) {
                writer.write(task.toStorageString());
                writer.newLine();
            }
        }
    }

    /**
     * Ensures the data directory exists, creates it if necessary.
     */
    private void ensureDataDirectoryExists() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
    }

    /**
     * Parses a single storage line into a Task. Returns null for malformed lines.
     *
     * <p>Expected formats:
     * <ul>
     *   <li>T | done | description</li>
     *   <li>D | done | description | dueDateOrDateTime</li>
     *   <li>E | done | description | start to end</li>
     * </ul>
     * where done is 0 or 1.</p>
     *
     * @param line the storage line to parse
     * @return the parsed Task, or null if the line is malformed
     */
    private Task parseLineToTask(String line) {
        if (isBlankLine(line)) {
            return null;
        }

        String[] fields = line.split(FIELD_DELIMITER_REGEX);
        if (hasInsufficientFields(fields, line)) {
            return null;
        }

        String typeCode = fields[0].trim();
        String completionFlag = fields[1].trim();
        String description = fields[2].trim();

        Task task = createTaskByType(typeCode, description, fields, line);
        if (task != null) {
            setTaskCompletion(task, completionFlag);
        }

        return task;
    }

    /**
     * Checks if a line is blank or empty.
     *
     * @param line the line being parsed
     */
    private boolean isBlankLine(String line) {
        return line == null || line.trim().isEmpty();
    }

    /**
     * Checks if the parsed fields are insufficient for a basic task.
     *
     * @param fields The parsed fields from the storage line.
     * @param line The original storage line, used for error logging.
     * @return {@code true} if there are fewer than the minimum required fields, {@code false} otherwise.
     */
    private boolean hasInsufficientFields(String[] fields, String line) {
        if (fields.length < MIN_FIELDS_BASE) {
            logSkippedLine(line, "Too few fields");
            return true;
        }
        return false;
    }

    /**
     * Creates a task based on its type code.
     *
     * @param typeCode The type code of the task (T, D, or E).
     * @param description The description of the task.
     * @param fields The parsed fields of the line split by the delimiter.
     * @param line The original line from the storage file, used for error logging.
     * @return A {@link Task} if parsing is successful, or {@code null} if the line is malformed.
     */
    private Task createTaskByType(String typeCode, String description, String[] fields, String line) {
        try {
            switch (typeCode) {
            case TODO_TYPE_CODE:
                return createTodoTask(description);
            case DEADLINE_TYPE_CODE:
                return createDeadlineTask(description, fields, line);
            case EVENT_TYPE_CODE:
                return createEventTask(description, fields, line);
            default:
                logSkippedLine(line, "Unknown task type '" + typeCode + "'");
                return null;
            }
        } catch (Exception e) {
            logSkippedLine(line, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a {@link Todo} task with the given description.
     *
     * @param description The description of the todo task.
     * @return A new {@link Todo} task instance containing the provided description.
     */
    private Task createTodoTask(String description) {
        return new Todo(description);
    }

    /**
     * Creates a Deadline task from parsed fields.
     *
     * @param description The description of the deadline task.
     * @param fields The parsed fields of the line split by the delimiter.
     * @param line The original line from the storage file, used for error logging.
     * @return A {@link Deadline} task if parsing is successful, or {@code null} if malformed.
     */
    private Task createDeadlineTask(String description, String[] fields, String line) {
        if (fields.length < MIN_FIELDS_DEADLINE) {
            logSkippedLine(line, "Missing deadline datetime");
            return null;
        }

        LocalDateTime dueDateTime = Parser.parseStrictDateOrDateTime(fields[3].trim());
        return new Deadline(description, dueDateTime);
    }

    /**
     * Creates an Event task from parsed fields.
     *
     * @param description The description of the event task.
     * @param fields The parsed fields of the line split by the delimiter.
     * @param line The original line from the storage file, used for error logging.
     * @return An {@link Event} task if parsing is successful, or {@code null} if malformed.
     */
    private Task createEventTask(String description, String[] fields, String line) {
        if (fields.length < MIN_FIELDS_EVENT) {
            logSkippedLine(line, "Missing event time range");
            return null;
        }

        String[] timeRange = fields[3].split(TIME_RANGE_DELIMITER_REGEX);
        if (timeRange.length < EXPECTED_TIME_RANGE_PARTS) {
            logSkippedLine(line, "Invalid event time range");
            return null;
        }

        LocalDateTime startTime = Parser.parseStrictDateOrDateTime(timeRange[0].trim());
        LocalDateTime endTime = Parser.parseStrictDateOrDateTime(timeRange[1].trim());

        return new Event(description, startTime, endTime);
    }

    /**
     * Sets the completion status of a task based on the flag.
     *
     * @param task The task whose completion status is to be updated.
     * @param completionFlag The flag indicating whether the task is completed.
     */
    private void setTaskCompletion(Task task, String completionFlag) {
        if (COMPLETED_FLAG.equals(completionFlag)) {
            task.markAsDone();
        }
    }

    /**
     * Logs a skipped line with the reason for skipping.
     *
     * @param line The line that was skipped.
     * @param reason The reason why the line was skipped.
     */
    private void logSkippedLine(String line, String reason) {
        System.err.println(LOG_PREFIX + " Skipping malformed line: '" + line + "' — " + reason);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message to log.
     */
    private void logError(String message) {
        System.out.println(LOG_PREFIX + " " + message);
    }
}
