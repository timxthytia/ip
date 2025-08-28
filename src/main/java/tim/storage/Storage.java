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

public class Storage {
    private final Path dataDir;
    private final Path dataFile;

    public Storage(String filePath) {
        Path p = Paths.get(filePath);
        this.dataDir = p.getParent() == null ? Paths.get(".") : p.getParent();
        this.dataFile = p;
    }

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
