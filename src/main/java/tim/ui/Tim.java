package tim.ui;

import java.util.Scanner;

import tim.command.Command;
import tim.exception.DukeException;
import tim.parser.Parser;
import tim.storage.Storage;
import tim.task.TaskList;

public class Tim {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Ui ui = new Ui(sc);
        Storage storage = new Storage("data/tim.text"); // relative path
        TaskList tasks;
        try {
            tasks = storage.load();
        } catch (DukeException e) {
            ui.showLoadingError();
            tasks = new TaskList();
        }
        ui.showWelcome();
        boolean isExit = false;
        while (!isExit) {
            try {
                String fullCommand = ui.readCommand();
                ui.showLine();
                Command c = Parser.parse(fullCommand);
                c.execute(tasks, ui, storage);
                isExit = c.isExit();
            } catch (DukeException e) {
                ui.showError(e.getMessage());
            } finally {
                // ui.showLine();
            }
        }
        ui.showLine();
        sc.close();
    }
}