import java.util.Scanner;
import java.util.ArrayList;

public class Tim {
    // Nested Task classs
    static class Task {
        protected String description;
        protected boolean completed;

        Task(String description) {
            this.description = description;
            this.completed = false;
        }

        void markAsDone() {
            this.completed = true;
        }

        void markAsUndone() {
            this.completed = false;
        }

        String getStatusIcon() {
            return (completed ? "X" : " ");
        }

        @Override
        public String toString() {
            return "[" + getStatusIcon() + "] " + description;
        }
    }

    static class Deadline extends Task {
        private final String date;
        Deadline(String description, String date) {
            super(description);
            this.date = date;
        }

        @Override
        public String toString() {
            return "[D]" + super.toString() + " (by: " + date + ")";
        }
    }

    static class Event extends Task {
        private final String start;
        private final String end;
        Event(String description, String start, String end) {
            super(description);
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
        }
    }

    static class Todo extends Task {
        Todo(String description) {
            super(description);
        }

        @Override
        public String toString() {
            return "[T]" + super.toString();
        }
    }

    static class DukeException extends Exception {
        DukeException(String message) {
            super(message);
        }
    }

    static void printError(String message) {
        System.out.println(" " + message);
    }

    static ArrayList<Task> tasks = new ArrayList<>();
    public static void main(String[] args) {
        /*
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
         */
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello I'm Tim");
        System.out.println("What can I do for you?");
        while (true) {
            String input = sc.nextLine();
            try {
                if (input.equals("list")) {
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println(" " + (i + 1) + "." + tasks.get(i));
                    }
                } else if (input.startsWith("mark ")) {
                    // if no task to mark
                    if (input.length() < 6 || input.substring(5).trim().isBlank()) {
                        throw new DukeException("OOPS!!! You cannot mark an empty task. Please provide a task number");
                    }
                    String index = input.substring(5).trim();
                    int idx;
                    // Task number is not integer
                    try {
                        idx = Integer.parseInt(index);
                    } catch (NumberFormatException e) {
                        throw new DukeException("OOPS!!! Task number must be an integer.");
                    }
                    // Task number not exist in list
                    if (idx < 1 || idx > tasks.size()) {
                        throw new DukeException("OOPS!!! Task number out of range.");
                    }

                    Task done = tasks.get(idx - 1);
                    done.markAsDone();
                    System.out.println("Nice! I've marked this task as done:");
                    System.out.println(" " + done);
                } else if (input.startsWith("unmark ")) {
                    // if no task to unmark
                    if (input.length() < 8 || input.substring(7).trim().isBlank()) {
                        throw new DukeException("OOPS!!! You cannot unmark an empty task. Please provide a task number");
                    }

                    String index = input.substring(7).trim();
                    int idx;
                    // Task number is not integer
                    try {
                        idx = Integer.parseInt(index);
                    } catch (NumberFormatException e) {
                        throw new DukeException("OOPS!!! Task number must be an integer.");
                    }
                    // Task number not exist in list
                    if (idx < 1 || idx > tasks.size()) {
                        throw new DukeException("OOPS!!! Task number out of range.");
                    }

                    Task undone = tasks.get(idx - 1);
                    undone.markAsUndone();
                    System.out.println("OK, I've marked this task as not done yet:");
                    System.out.println(" " + undone);
                } else if (input.equals("bye")){
                    System.out.println("Bye! Hope to see you again soon!");
                    break;
                } else if (input.startsWith("todo")) {
                    String desc = input.substring(4).trim();
                    // if no todo description
                    if (desc.isBlank()) {
                        throw new DukeException("OOPS!!! The description of a todo cannot be empty.");
                    }

                    Task newTask = new Todo(desc);
                    tasks.add(newTask);
                    System.out.println("Got it. I've added this task:");
                    System.out.println(" " + newTask);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } else if (input.startsWith("deadline")) {
                    String body = input.substring("deadline".length()).trim();
                    // if desc missing "/by"
                    if (!body.contains("/by")) {
                        throw new DukeException("OOPS!!! Deadline format: deadline <desc> /by <due date>.");
                    }

                    String[] parts = body.split("/by", 2);
                    // if desc / due date missing
                    if (parts.length < 2 || parts[0].trim().isBlank() || parts[1].trim().isBlank()) {
                        throw new DukeException("OOPS!!! Deadline format: deadline <desc> /by <due date>.");
                    }
                    String desc = parts[0].trim();
                    String date = parts[1].trim();

                    Task newTask = new Deadline(desc, date);
                    tasks.add(newTask);
                    System.out.println("Got it. I've added this task:");
                    System.out.println(" " + newTask);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } else if (input.startsWith("event")) {
                    String body = input.substring("event".length()).trim();
                    // if desc missing "/from" or "/to"
                    if (!body.contains("/from") || !body.contains("/to")) {
                        throw new DukeException("OOPS!!! Event format: event <desc> /from <start date> /to <end date>.");
                    }

                    String[] dateSplit = body.split("/from", 2);
                    // if desc or timeline missing
                    if (dateSplit.length < 2) {
                        throw new DukeException("OOPS!!! Event format: event <desc> /from <start date> /to <end date>.");
                    }

                    String desc = dateSplit[0].trim();
                    String[] toSplit = dateSplit[1].split("/to",2);
                    // if start or end date missing
                    if (toSplit.length < 2) {
                        throw new DukeException("OOPS!!! Event format: event <desc> /from <start date> /to <end date>.");
                    }

                    String start = toSplit[0].trim();
                    String end = toSplit[1].trim();

                    // if desc or start or end date missing
                    if (desc.isBlank() || start.isBlank() || end.isBlank()) {
                        throw new DukeException("OOPS!!! Event format: event <desc> /from <start date> /to <end date>.");
                    }

                    Task newTask = new Event(desc, start, end);
                    tasks.add(newTask);
                    System.out.println("Got it. I've added this task:");
                    System.out.println(" " + newTask);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } else if (input.startsWith("delete ")) {
                    String indexStr = input.substring(7).trim();
                    if (indexStr.isBlank()) {
                        throw new DukeException("OOPS!!! Please provide a task number to delete, e.g., 'delete 3'.");
                    }
                    int idx;
                    try {
                        idx = Integer.parseInt(indexStr);
                    } catch (NumberFormatException e) {
                        throw new DukeException("OOPS!!! Task number must be an integer.");
                    }
                    if (idx < 1 || idx > tasks.size()) {
                        throw new DukeException("OOPS!!! Task number out of range.");
                    }
                    Task removed = tasks.remove(idx - 1);
                    System.out.println("Noted. I've removed this task:");
                    System.out.println("  " + removed);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                } else {
                    throw new DukeException("OOPS!!! I'm sorry, but I don't know what that means :-(");
                }
            } catch (DukeException e) {
                System.out.println(" " + e.getMessage());
            } catch (Exception e) {
                printError("Somthing went wrong: " + e.getMessage());
            }
        }
        sc.close();
    }
}
