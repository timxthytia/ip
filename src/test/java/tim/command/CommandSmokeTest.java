package tim.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tim.storage.Storage;
import tim.task.Deadline;
import tim.task.Event;
import tim.task.TaskList;
import tim.task.Todo;
import tim.ui.Ui;

/**
 * Smoke tests for core command classes.
 * <p>
 * These tests exercise adding, marking, finding, deleting tasks, and
 * adding deadlines/events to ensure that command execution updates
 * the {@link TaskList} correctly and returns non-empty user messages.
 * </p>
 */
public class CommandSmokeTest {

    @TempDir
    Path tmpDir;

    private TaskList tasks;
    private Ui ui;
    private Storage storage;

    /**
     * Initializes a fresh {@link TaskList}, {@link Ui}, and
     * {@link Storage} backed by a temporary file before each test.
     */
    @BeforeEach
    void setUp() {
        tasks = new TaskList();
        ui = new Ui();
        storage = new Storage(tmpDir.resolve("data.txt").toString());
    }

    /**
     * Verifies the full flow of adding a todo, marking it done,
     * finding it with case-insensitive search, and then deleting it.
     *
     * @throws Exception if any of the command executions throw
     *     a runtime or {@link tim.exception.TimException}
     */
    @Test
    void addTodoMarkFindDeleteFlow_ok() throws Exception {
        // add a todo
        String addMsg = new AddTodoCommand("read book").execute(tasks, ui, storage);
        assertEquals(1, tasks.size(), "TaskList size should increase after add");
        assertTrue(tasks.get(0) instanceof Todo, "First task should be a Todo");
        assertNotNull(addMsg);
        assertFalse(addMsg.isBlank());

        // mark it
        String markMsg = new MarkCommand(1).execute(tasks, ui, storage);
        assertTrue(tasks.get(0).isCompleted(), "Task should be marked as done");
        assertNotNull(markMsg);
        assertFalse(markMsg.isBlank());

        // find (case-insensitive)
        String findMsg = new FindCommand("BOOK").execute(tasks, ui, storage);
        assertTrue(findMsg.toLowerCase().contains("read book"), "Find should match case-insensitively");

        // delete it
        String delMsg = new DeleteCommand(1).execute(tasks, ui, storage);
        assertEquals(0, tasks.size(), "Delete should remove the task");
        assertNotNull(delMsg);
        assertFalse(delMsg.isBlank());
    }

    /**
     * Verifies that adding a deadline and an event results in
     * tasks of the correct type being appended to the {@link TaskList}.
     *
     * @throws Exception if command execution fails
     */
    @Test
    void addDeadlineAddEvent_ok() throws Exception {
        LocalDateTime due = LocalDateTime.parse("2025-09-20T23:30");
        String dMsg = new AddDeadlineCommand("submit report", due).execute(tasks, ui, storage);
        assertEquals(1, tasks.size(), "Adding deadline should add 1 task");
        assertTrue(tasks.get(0) instanceof Deadline);
        assertNotNull(dMsg);
        assertFalse(dMsg.isBlank());

        LocalDateTime s = LocalDateTime.parse("2025-09-21T09:00");
        LocalDateTime e = LocalDateTime.parse("2025-09-21T11:00");
        String eMsg = new AddEventCommand("team sync", s, e).execute(tasks, ui, storage);
        assertEquals(2, tasks.size(), "Adding event should add another task");
        assertTrue(tasks.get(1) instanceof Event);
        assertNotNull(eMsg);
        assertFalse(eMsg.isBlank());
    }

    /**
     * Verifies that attempting to delete a task at an invalid index
     * results in a {@link tim.exception.TimException} being thrown.
     */
    @Test
    void deleteOutOfBounds_throwsTimException() {
        assertThrows(
                tim.exception.TimException.class, () -> new DeleteCommand(1).execute(tasks, ui, storage),
                "Deleting from an empty list should throw TimException"
        );
    }
}
