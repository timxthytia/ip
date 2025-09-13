package tim.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tim.exception.TimException;
import tim.task.Deadline;
import tim.task.Event;
import tim.task.TaskList;
import tim.task.Todo;

/**
 * Unit tests for the {@link Storage} class.
 * Verifies that saving and loading tasks to and from disk works correctly,
 */
public class StorageTest {
    /**
     * Tests that saving a task list to disk and then reloading it produces
     * equivalent tasks, including loading of Todo, Deadline, and Event tasks.
     *
     * @param tempDir a temporary directory provided by JUnit for file I/O
     * @throws IOException if an I/O error occurs during save or load
     * @throws TimException if parsing or storage operations fail
     */
    @Test
    @DisplayName("save() then load() round-trips tasks (Todo, Deadline, Event)")
    void saveThenLoad_roundTrip(@TempDir Path tempDir) throws IOException, TimException {
        Path dataFile = tempDir.resolve("tim.text");
        Storage storage = new Storage(dataFile.toString());

        TaskList original = new TaskList();
        original.add(new Todo("todo1"));

        Deadline dl = new Deadline("deadline1", LocalDateTime.of(2020, 2, 2, 0, 0));
        original.add(dl);

        Event ev = new Event(
                "event1",
                LocalDateTime.of(2020, 2, 2, 12, 0),
                LocalDateTime.of(2020, 2, 3, 0, 0)
        );
        original.add(ev);

        // persist to DATA_FILE
        storage.save(original);
        assertTrue(Files.exists(dataFile), "Data file should be created");

        // reload from DATA_FILE
        TaskList reloaded = storage.load();
        assertEquals(3, reloaded.size());
        assertEquals("[T][ ] todo1", reloaded.get(0).toString());
        assertTrue(reloaded.get(1) instanceof Deadline);
        assertTrue(reloaded.get(2) instanceof Event);

        // check display formatting
        assertTrue(reloaded.get(1).toString().contains("Feb 02 2020 00:00"));
        assertTrue(reloaded.get(2).toString().contains("from: Feb 02 2020 12:00 to: Feb 03 2020 00:00"));
    }

    /**
     * Tests that loading from a non-existent file returns an empty task list
     * instead of throwing an exception.
     *
     * @param tempDir a temporary directory provided by JUnit for file I/O
     * @throws TimException if the load operation fails unexpectedly
     */
    @Test
    @DisplayName("load() handles missing file: returns empty list")
    void load_missingFile(@TempDir Path tempDir) throws TimException {
        Path dataFile = tempDir.resolve("tim.text");
        Storage storage = new Storage(dataFile.toString());
        TaskList list = storage.load();
        assertEquals(0, list.size());
    }
}
