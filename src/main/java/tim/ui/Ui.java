
package tim.ui;

import java.util.Scanner;

import tim.task.Task;
import tim.task.TaskList;

/**
 * Handles all user interactions with the chatbot.
 * Provides methods to display messages, errors, task lists, and other
 * user-facing outputs, as well as to read commands entered by the user.
 */
public class Ui {
    private final Scanner sc;
    /**
     * Creates a Ui object to handle user input and output.
     *
     * @param sc Scanner used to read user commands from input.
     */
    public Ui(Scanner sc) {
        this.sc = sc;
    }

    /**
     * Displays the welcome message shown at the start of the program.
     */
    public void showWelcome() {
        System.out.println("Hello I'm Tim");
        System.out.println("What can I do for you?");
    }

    /**
     * Reads the next line of user input.
     *
     * @return the full user command entered.
     */
    public String readCommand() {
        return sc.nextLine();
    }

    /**
     * Displays a horizontal divider line to separate output blocks.
     */
    public void showLine() {
        System.out.println("____________________________________________________________");
    }
    /**
     * Displays an error message.
     *
     * @param msg the error message to display.
     */
    public void showError(String msg) {
        System.out.println(" " + msg);
    }
    /**
     * Displays an error message when tasks cannot be loaded from storage.
     */
    public void showLoadingError() {
        System.out.println(" OOPS!!! Couldn't load previous tasks, starting fresh.");
    }
    /**
     * Displays the list of tasks currently stored.
     *
     * @param tasks the list of tasks to display.
     */
    public void showList(TaskList tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }
    /**
     * Displays a confirmation message after a task has been added.
     *
     * @param t the task that was added.
     * @param tasks the updated task list.
     */
    public void showAdded(Task t, TaskList tasks) {
        System.out.println("Got it. I've added this task:");
        System.out.println(" " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
    /**
     * Displays a confirmation message after a task has been removed.
     *
     * @param t the task that was removed.
     * @param tasks the updated task list.
     */
    public void showRemoved(Task t, TaskList tasks) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
    /**
     * Displays the farewell message when the program exits.
     */
    public void showBye() {
        System.out.println("Bye! Hope to see you again soon!");
    }
}
