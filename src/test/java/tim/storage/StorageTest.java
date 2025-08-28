package tim.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tim.task.*;
import tim.exception.DukeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class StorageTest {
    @Test
    @DisplayName("save() then load() round-trips tasks (Todo, Deadline, Event)")
    void saveThenLoad_roundTrip(@TempDir Path tempDir) throws IOException, DukeException {
        Path dataFile = tempDir.resolve("tim.text");
        Storage storage = new Storage(dataFile.toString());

        TaskList original = new TaskList();
        original.add(new Todo("todo1"));

        Deadline dl = new Deadline("deadline1", LocalDateTime.of(2020,2,2,0,0));
        original.add(dl);

        Event ev = new Event(
                "event1",
                LocalDateTime.of(2020,2,2,12,0),
                LocalDateTime.of(2020,2,3,0,0)
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

    @Test
    @DisplayName("load() handles missing file: returns empty list")
    void load_missingFile(@TempDir Path tempDir) throws DukeException {
        Path dataFile = tempDir.resolve("tim.text");
        Storage storage = new Storage(dataFile.toString());
        TaskList list = storage.load();
        assertEquals(0, list.size());
    }
}
