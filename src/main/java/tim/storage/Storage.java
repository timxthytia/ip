package tim.storage;

import tim.exception.DukeException;
import tim.task.*;
import tim.parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Handles the reading and writing of tasks to and from a save file on disk.
 * The Storage class is responsible for persisting the user's task list across
 * program runs.
 * when the application starts, and saves the current tasks back to the file
 * whenever changes are made.
 *
 */
public class Storage {
    private final Path dataDir;
    private final Path dataFile;

    /**
     * Creates a new Storage object with the given file path.
     *
     * @param filePath the path of the file to save/load tasks from.
     */
    public Storage(String filePath) {
        Path p = Paths.get(filePath);
        this.dataDir = p.getParent() == null ? Paths.get(".") : p.getParent();
        this.dataFile = p;
    }

    /**
     * Loads tasks from the data file into a TaskList.
     * If the file or its parent directory does not exist, this method will
     * create the directory and return an empty TaskList.
     *
     * @return a TaskList containing the tasks loaded from file.
     * @throws DukeException if an I/O error occurs or if
     * the data file cannot be created.
     */
    public TaskList load() throws DukeException {
        TaskList list = new TaskList(new ArrayList<>());
        if (!Files.exists(dataFile)) {
            try {
                if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            } catch (IOException ioe) {
                throw new DukeException("Could not create data directory: " + ioe.getMessage());
            }
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s*\\|\\s*");
                if (parts.length < 3) continue;
                String type = parts[0].trim();
                boolean isDone = "1".equals(parts[1].trim());
                String description = parts[2].trim();

                Task t;
                switch (type) {
                    case "T":
                        t = new Todo(description); break;
                    case "D":
                        if (parts.length < 4) continue;
                        LocalDateTime due = Parser.parseStrictDateOrDateTime(parts[3]);
                        t = new Deadline(description, due); break;
                    case "E":
                        if (parts.length < 4) continue;
                        String[] se = parts[3].split("\\s*to\\s*");
                        if (se.length < 2) continue;
                        LocalDateTime s = Parser.parseStrictDateOrDateTime(se[0]);
                        LocalDateTime e = Parser.parseStrictDateOrDateTime(se[1]);
                        t = new Event(description, s, e); break;
                    default:
                        continue;
                }
                if (isDone) t.markAsDone();
                list.add(t);
            }
        } catch (IOException ioe) {
            throw new DukeException("Could not load tasks: " + ioe.getMessage());
        }
        return list;
    }

    /**
     * Saves the given TaskList to the data file.
     * If the data directory does not exist, it will be created.
     *
     * @param tasks the TaskList to save.
     */
    public void save(TaskList tasks) {
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            try (BufferedWriter bw = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                for (Task t : tasks.asList()) {
                    bw.write(t.toStorageString());
                    bw.newLine();
                }
            }
        } catch (IOException ioe) {
            System.out.println(" Could not save tasks: " + ioe.getMessage());
        }
    }
}
