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
 * The {@code Storage} class is responsible for persisting the user's task list across program runs.
 * It loads tasks from the given file when the application starts, and saves the current tasks
 * back to the file whenever changes are made.
 */
public class Storage {
    // Constants
    private static final String DELIM_REGEX = "\\s*\\|\\s*"; // e.g., T | 1 | desc | 2020-01-01 1800
    private static final String RANGE_TO_REGEX = "\\s*to\\s*"; // e.g., "Aug 6th 2 to 4pm"
    private static final String TYPE_TODO = "T";
    private static final String TYPE_DEADLINE = "D";
    private static final String TYPE_EVENT = "E";

    private final Path dataDir;
    private final Path dataFile;

    /**
     * Creates a new {@code Storage} object with the given file path.
     *
     * @param filePath the path of the file to save/load tasks from.
     */
    public Storage(String filePath) {
        Path p = Paths.get(filePath);
        this.dataDir = p.getParent() == null ? Paths.get(".") : p.getParent();
        this.dataFile = p;
    }

    /**
     * Loads tasks from the data file into a {@link TaskList}.
     * If the file or its parent directory does not exist, this method creates the directory
     * and returns an empty list.
     *
     * @return a {@code TaskList} containing the tasks loaded from file.
     * @throws TimException if an I/O error occurs when preparing the data location or reading the file.
     */
    public TaskList load() throws TimException {
        TaskList list = new TaskList();
        try {
            // Ensure data directory exists; if there's no file yet, return empty list.
            ensureDataDirExists();
            if (!Files.exists(dataFile)) {
                return list;
            }

            try (BufferedReader br = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    Task parsed = parseLineToTask(line);
                    if (parsed != null) {
                        list.add(parsed);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new TimException("Could not load tasks: " + ioe.getMessage(), ioe);
        }
        return list;
    }

    /**
     * Saves the given {@link TaskList} to the data file. Creates the data directory if needed.
     *
     * @param tasks the tasks to save.
     */
    public void save(TaskList tasks) {
        try {
            ensureDataDirExists();
            try (BufferedWriter bw = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                for (Task t : tasks.asList()) {
                    bw.write(t.toStorageString());
                    bw.newLine();
                }
            }
        } catch (IOException ioe) {
            System.out.println("Could not save tasks: " + ioe.getMessage());
        }
    }

    // Helper methods (SLAP)

    /** Ensures the data directory exists, creating it if necessary. */
    private void ensureDataDirExists() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
    }

    /**
     * Parses a single storage line into a {@link Task}. Returns {@code null} for malformed lines.
     *
     * <p>Expected forms (whitespace around '|' is ignored):
     * <ul>
     *   <li>{@code T | done | description}</li>
     *   <li>{@code D | done | description | dueDateOrDateTime}</li>
     *   <li>{@code E | done | description | start to end}</li>
     * </ul>
     * where {@code done} is {@code 0} or {@code 1}.</p>
     */
    private Task parseLineToTask(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null; // skip blank lines
        }

        String[] parts = line.split(DELIM_REGEX);
        if (parts.length < 3) {
            logSkip(line, "Too few fields");
            return null;
        }

        String type = parts[0].trim();
        String doneFlag = parts[1].trim();
        String description = parts[2].trim();

        Task task;
        try {
            switch (type) {
            case TYPE_TODO:
                task = new Todo(description);
                break;
            case TYPE_DEADLINE:
                if (parts.length < 4) {
                    logSkip(line, "Missing deadline datetime");
                    return null;
                }
                LocalDateTime due = Parser.parseStrictDateOrDateTime(parts[3].trim());
                task = new Deadline(description, due);
                break;
            case TYPE_EVENT:
                if (parts.length < 4) {
                    logSkip(line, "Missing event time range");
                    return null;
                }
                String[] se = parts[3].split(RANGE_TO_REGEX);
                if (se.length < 2) {
                    logSkip(line, "Invalid event time range");
                    return null;
                }
                LocalDateTime start = Parser.parseStrictDateOrDateTime(se[0].trim());
                LocalDateTime end = Parser.parseStrictDateOrDateTime(se[1].trim());
                task = new Event(description, start, end);
                break;
            default:
                logSkip(line, "Unknown task type '" + type + "'");
                return null;
            }
        } catch (Exception ex) {
            // Date parsing (or similar) failed. Skip this malformed line.
            logSkip(line, ex.getMessage());
            return null;
        }

        if ("1".equals(doneFlag)) {
            task.markAsDone();
        }
        return task;
    }

    /** Simple stderr logger for skipped lines (keeps behavior but explains why). */
    private static void logSkip(String line, String reason) {
        System.err.println("[Storage] Skipping malformed line: '" + line + "' — " + reason);
    }
}
