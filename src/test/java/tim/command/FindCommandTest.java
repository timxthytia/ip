package tim.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Unit tests for the {@link FindCommand} class.
 * <p>
 * These tests verify that the command correctly performs case-insensitive search,
 * trims and normalizes the keyword, handles no-match cases, and enforces keyword validity.
 * </p>
 */
public class FindCommandTest {

    @TempDir
    Path tmpDir;

    private TaskList tasks;
    private Ui ui;
    private Storage storage;

    /**
     * Initializes a new {@link TaskList} with sample tasks before each test run.
     * Tasks include "Read Book" and "buy milk".
     *
     * @throws Exception if task setup or storage initialization fails
     */
    @BeforeEach
    void setup() throws Exception {
        tasks = new TaskList();
        ui = new Ui();
        storage = new Storage(tmpDir.resolve("find-data.txt").toString());

        new AddTodoCommand("Read Book").execute(tasks, ui, storage);
        new AddTodoCommand("buy milk").execute(tasks, ui, storage);
    }

    /**
     * Tests that {@link FindCommand} finds tasks in a case-insensitive manner.
     *
     * @throws Exception if command execution fails
     */
    @Test
    void findCaseInsensitiveMatches() throws Exception {
        String msg = new FindCommand("book").execute(tasks, ui, storage);
        assertTrue(msg.toLowerCase().contains("read book"), "Expected to find 'Read Book'");
    }

    /**
     * Tests that {@link FindCommand} trims whitespace and lowercases the keyword
     * before performing a search.
     *
     * @throws Exception if command execution fails
     */
    @Test
    void findTrimsAndLowercasesKeyword() throws Exception {
        String msg = new FindCommand("  BOOK  ").execute(tasks, ui, storage);
        assertTrue(msg.toLowerCase().contains("read book"),
                "Expected to find 'Read Book' with trimmed/lowercased keyword");
    }

    /**
     * Tests that {@link FindCommand} returns an exact no-match message when no tasks
     * match the given keyword.
     *
     * @throws Exception if command execution fails
     */
    @Test
    void findNoMatchesReturnsExactNoMatchMessageWithNormalizedKeyword() throws Exception {
        String msg = new FindCommand(" ZzZ ").execute(tasks, ui, storage);
        assertEquals("No matching tasks found for keyword: zzz", msg);
    }

    /**
     * Tests that constructing a {@link FindCommand} with a blank or null keyword
     * throws an {@link IllegalArgumentException}.
     */
    @Test
    void constructorBlankKeywordThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FindCommand("   "),
                "Blank keyword should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new FindCommand(null),
                "Null keyword should throw IllegalArgumentException");
    }
}
