package tim.ui;

import java.util.Scanner;

import tim.task.Task;
import tim.task.TaskList;

public class Ui {
    private final Scanner sc;
    public Ui(Scanner sc) {
        this.sc = sc;
    }

    public void showWelcome() {
        System.out.println("Hello I'm Tim");
        System.out.println("What can I do for you?");
    }

    public String readCommand() {
        return sc.nextLine();
    }

    public void showLine() {
        System.out.println("____________________________________________________________");
    }
    public void showError(String msg) {
        System.out.println(" " + msg);
    }
    public void showLoadingError() {
        System.out.println(" OOPS!!! Couldn't load previous tasks, starting fresh.");
    }
    public void showList(TaskList tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }
    public void showAdded(Task t, TaskList tasks) {
        System.out.println("Got it. I've added this task:");
        System.out.println(" " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
    public void showRemoved(Task t, TaskList tasks) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
    public void showBye() {
        System.out.println("Bye! Hope to see you again soon!");
    }
}
