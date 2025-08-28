import java.util.Scanner;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


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

        /* Returns line format that is saved to storage, to be overridden in subclasses */
        String toStorageString() {
            return "? | " + (completed ? "1" : "0") + " | " + description;
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
        private final LocalDateTime due; // store deaedline as LocalDateTime
        Deadline(String description, LocalDateTime due) {
            super(description);
            this.due = due;
        }

        @Override
        String toStorageString() {
            return "D | " + (completed ? "1" : "0") + " | " + description + " | " + due.toString();
        }

        @Override
        public String toString() {
            // Create format for toString method
            DateTimeFormatter DISP = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
            return "[D]" + super.toString() + " (by: " + due.format(DISP) + ")";
        }
    }

    static class Event extends Task {
        private final LocalDateTime start;
        private final LocalDateTime end;
        Event(String description, LocalDateTime start, LocalDateTime end) {
            super(description);
            this.start = start;
            this.end = end;
        }

        @Override
        String toStorageString() {
            return "E | " + (completed ? "1" : "0") + " | " + description +
                    " | " + start.toString() + " to " + end.toString();
        }

        @Override
        public String toString() {
            DateTimeFormatter DISP = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
            if (start.equals(end)) {
                return "[E]" + super.toString() + " (on: " + start.format(DISP) + ")";
            } else {
                return "[E]" + super.toString() + " (from: " + start.format(DISP) + " to: " + end.format(DISP) + ")";
            }
        }
    }

    static class Todo extends Task {
        Todo(String description) {
            super(description);
        }

        @Override
        String toStorageString() {
            return "T | " + (completed ? "1" : "0") + " | " + description;
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

    /* Code for Level-7 */
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path DATA_FILE = DATA_DIR.resolve("tim.text");

    /* Save tasks to ./data/tim.text automatically whenever task list change */
    static void saveTasks() {
        try {
            if (!Files.exists(DATA_DIR)) {
                // if ./data directory not exist, create it
                Files.createDirectories(DATA_DIR);
            }
            try (BufferedWriter bw = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8)) {
                for (Task t : tasks) {
                    // add each task to DATA_FILE tim.text
                    bw.write(t.toStorageString());
                    bw.newLine();
                }
            }
        } catch (IOException ioe) {
            printError("Could not save tasks: " + ioe.getMessage());
        }
    }

    /* Load tasks from ./data/tim.text automatically when chatbot starts up */
    static void loadTasks() {
        if (!Files.exists(DATA_FILE)) {
            // if DATA_FILE not exist when chatbot first start up
            try {
                if (!Files.exists(DATA_DIR)) {
                    // if ./data directory not exist, create it
                    Files.createDirectories(DATA_DIR);
                }
            } catch (IOException ioe) {
                printError("Could not create directory: " + ioe.getMessage());
            }
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                /* Format string format for output */
                String[] parts = line.split("\\s*\\|\\s*");
                if (parts.length < 3) {
                    printError("Skipping corrupted line: " + line);
                    continue;
                }
                String type = parts[0].trim();
                String status = parts[1].trim();
                String description = parts[2].trim();
                boolean isDone = status.equals("1"); // boolean rep if task is marked done

                Task t;
                switch (type) {
                case "T":
                    t = new Todo(description);
                    break;
                case "D":
                    if (parts.length < 4) {
                        printError("Skipping corrupted event: " + line);
                        continue;
                    }
                    LocalDateTime due = parseStrictDateOrDateTime(parts[3]);
                    t = new Deadline(description, due);
                    break;
                case "E":
                    if (parts.length < 4) {
                        printError("Skipping corrupted event: " + line);
                        continue;
                    }
                    // retrieve start and end date of Event object
                    String[] startAndEnd = parts[3].split("\\s*to\\s*");
                    if (startAndEnd.length < 2) {
                        printError("Skipping corrupted event: " + line);
                        continue;
                    }
                    LocalDateTime s = parseStrictDateOrDateTime(startAndEnd[0]);
                    LocalDateTime e = parseStrictDateOrDateTime(startAndEnd[1]);
                    t = new Event(description, s, e);
                    break;
                default:
                    printError("Skipping unknown task type: " + type);
                    continue;
                }
                if (isDone) {
                    t.markAsDone();
                }
                tasks.add(t);
            }
        } catch (IOException ioe) {
            printError("Could not load tasks: " + ioe.getMessage());
        } catch (Exception e) {
            printError("Error parsing data file, continuing with partial load: " + e.getMessage());
        }
    }

    /* Parsing Date and Time */
    private static final DateTimeFormatter INPUT_DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter INPUT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    static LocalDateTime parseStrictDateOrDateTime(String s) {
        String trimmed = s.trim();
        // Datetime with HHmm
        try {
            return LocalDateTime.parse(trimmed, INPUT_DATE_TIME);
        } catch (DateTimeParseException ignored) { }
        // Date only
        try {
            LocalDate d = LocalDate.parse(trimmed, INPUT_DATE_ONLY);
            return d.atStartOfDay();
        } catch (DateTimeParseException ignored) { }
        // ISO-8601 LocalDateTime (used in storage)
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) { }
        throw new IllegalArgumentException("Unrecognized date/time (use yyyy-MM-dd or yyyy-MM-dd HHmm): " + s);
    }

    static ArrayList<Task> tasks = new ArrayList<>();
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        loadTasks();
        Ui ui = new Ui(sc);
        Storage storage = new Storage();
        TaskList tasks = new TaskList(Tim.tasks, Tim::saveTasks);
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
                ui.showLine();
            }
        }
        sc.close();
    }
    /* UI Class for CLI commands/outputs (scalable) */
    static class Ui {
        private final Scanner sc;
        Ui(Scanner sc) {
            this.sc = sc;
        }

        void showWelcome() {
            System.out.println("Hello I'm Tim");
            System.out.println("What can I do for you?");
        }
        String readCommand() {
            return sc.nextLine();
        }
        void showLine() {
            System.out.println("____________________________________________________________");
        }
        void showError(String msg) {
            System.out.println(" " + msg);
        }
        void showList(TaskList tasks) {
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println(" " + (i + 1) + "." + tasks.get(i));
            }
        }
        void showAdded(Task t, TaskList tasks) {
            System.out.println("Got it. I've added this task:");
            System.out.println(" " + t);
            System.out.println("Now you have " + tasks.size() + " tasks in the list.");
        }
        void showRemoved(Task t, TaskList tasks) {
            System.out.println("Noted. I've removed this task:");
            System.out.println("  " + t);
            System.out.println("Now you have " + tasks.size() + " tasks in the list.");
        }
        void showBye() {
            System.out.println("Bye! Hope to see you again soon!");
        }
    }

    /* TaskList Class */
    static class TaskList {
        private final ArrayList<Task> tasks;
        private final Runnable saveHook;
        TaskList(ArrayList<Task> tasks, Runnable saveHook) {
            this.tasks = tasks;
            this.saveHook = saveHook;
        }

        int size() {
            return tasks.size();
        }
        Task get(int idx) {
            return tasks.get(idx);
        }
        void add(Task t) {
            tasks.add(t);
            saveHook.run();
        }
        Task remove(int idx) {
            Task t = tasks.remove(idx);
            saveHook.run();
            return t;
        }
        @Override
        public String toString() {
            return tasks.toString();
        }
    }

    /* Storage Class */
    static class Storage {
        void save(TaskList tasks) {
            Tim.saveTasks();
        }
    }

    /* Parser Class: returns Commands */
    static class Parser {
        // Parses the user input and returns a Command object
        static Command parse(String input) throws DukeException {
            if (input.equals("list")) {
                return new ListCommand();
            } else if (input.startsWith("mark ")) {
                if (input.length() < 6 || input.substring(5).trim().isBlank()) {
                    throw new DukeException("OOPS!!! You cannot mark an empty task. Please provide a task number");
                }
                int idx;
                try {
                    idx = Integer.parseInt(input.substring(5).trim());
                } catch (NumberFormatException e) {
                    throw new DukeException("OOPS!!! Task number must be an integer.");
                }
                return new MarkCommand(idx);
            } else if (input.startsWith("unmark ")) {
                if (input.length() < 8 || input.substring(7).trim().isBlank()) {
                    throw new DukeException("OOPS!!! You cannot unmark an empty task. Please provide a task number");
                }
                int idx;
                try {
                    idx = Integer.parseInt(input.substring(7).trim());
                } catch (NumberFormatException e) {
                    throw new DukeException("OOPS!!! Task number must be an integer.");
                }
                return new UnmarkCommand(idx);
            } else if (input.equals("bye")) {
                return new ExitCommand();
            } else if (input.startsWith("todo")) {
                String desc = input.substring(4).trim();
                return new AddTodoCommand(desc);
            } else if (input.startsWith("deadline")) {
                String body = input.substring("deadline".length()).trim();
                if (!body.contains("/by")) {
                    throw new DukeException("OOPS!!! Deadline format: deadline <desc> /by <due date>.");
                }
                String[] parts = body.split("/by", 2);
                if (parts.length < 2 || parts[0].trim().isBlank() || parts[1].trim().isBlank()) {
                    throw new DukeException("OOPS!!! Deadline format: deadline <desc> /by <due date>.");
                }
                String desc = parts[0].trim();
                String date = parts[1].trim();
                LocalDateTime dueDateTime;
                try {
                    dueDateTime = parseStrictDateOrDateTime(date);
                } catch (IllegalArgumentException ex) {
                    throw new DukeException("OOPS!!! I couldn't understand that date/time. " +
                            "Please use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 1800).");
                }
                return new AddDeadlineCommand(desc, dueDateTime);
            } else if (input.startsWith("event")) {
                String body = input.substring("event".length()).trim();
                if (!body.contains("/from") || !body.contains("/to")) {
                    throw new DukeException("OOPS!!! Event format: event <desc> /from <start> /to <end>.");
                }
                String[] dateSplit = body.split("/from", 2);
                if (dateSplit.length < 2) {
                    throw new DukeException("OOPS!!! Event format: event <desc> /from <start> /to <end>.");
                }
                String desc = dateSplit[0].trim();
                String[] toSplit = dateSplit[1].split("/to", 2);
                if (toSplit.length < 2) {
                    throw new DukeException("OOPS!!! Event format: event <desc> /from <start> /to <end>.");
                }
                String start = toSplit[0].trim();
                String end = toSplit[1].trim();
                if (desc.isBlank() || start.isBlank() || end.isBlank()) {
                    throw new DukeException("OOPS!!! Event format: event <desc> /from <start> /to <end>.");
                }
                LocalDateTime startDateTime;
                LocalDateTime endDateTime;
                try {
                    startDateTime = parseStrictDateOrDateTime(start);
                    endDateTime = parseStrictDateOrDateTime(end);
                } catch (IllegalArgumentException ex) {
                    throw new DukeException("OOPS!!! I couldn't understand those date/times. " +
                            "Please use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 0900).");
                }
                return new AddEventCommand(desc, startDateTime, endDateTime);
            } else if (input.startsWith("delete ")) {
                String indexStr = input.substring(7).trim();
                if (indexStr.isBlank()) {
                    throw new DukeException("OOPS!!! Please provide a task number to delete (e.g.'delete 3').");
                }
                int idx;
                try { idx = Integer.parseInt(indexStr); }
                catch (NumberFormatException e) { throw new DukeException("OOPS!!! Task number must be an integer."); }
                return new DeleteCommand(idx);
            } else {
                throw new DukeException("OOPS!!! I'm sorry, but I don't know what that means :-(");
            }
        }
    }

    /* Command class (abstract) */
    static abstract class Command {
        abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException;
        boolean isExit() {
            return false;
        }
    }

    static class ListCommand extends Command {
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showList(tasks);
        }
    }

    static class MarkCommand extends Command {
        private final int index;
        MarkCommand(int index) { this.index = index; }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            if (index < 1 || index > tasks.size()) {
                throw new DukeException("OOPS!!! Task number out of range.");
            }
            Task done = tasks.get(index - 1);
            done.markAsDone();
            storage.save(tasks);
            System.out.println("Nice! I've marked this task as done:");
            System.out.println(" " + done);
        }
    }

    static class UnmarkCommand extends Command {
        private final int index;
        UnmarkCommand(int index) {
            this.index = index;
        }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            if (index < 1 || index > tasks.size()) {
                throw new DukeException("OOPS!!! Task number out of range.");
            }
            Task undone = tasks.get(index - 1);
            undone.markAsUndone();
            storage.save(tasks);
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println(" " + undone);
        }
    }

    static class AddTodoCommand extends Command {
        private final String desc;
        AddTodoCommand(String desc) {
            this.desc = desc;
        }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            if (desc.isBlank()) {
                throw new DukeException("OOPS!!! The description of a todo cannot be empty.");
            }
            Task newTask = new Todo(desc);
            tasks.add(newTask);
            storage.save(tasks);
            ui.showAdded(newTask, tasks);
        }
    }

    static class AddDeadlineCommand extends Command {
        private final String desc;
        private final LocalDateTime due;
        AddDeadlineCommand(String desc, LocalDateTime due) {
            this.desc = desc;
            this.due = due;
        }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            Task newTask = new Deadline(desc, due);
            tasks.add(newTask);
            storage.save(tasks);
            ui.showAdded(newTask, tasks);
        }
    }

    static class AddEventCommand extends Command {
        private final String desc;
        private final LocalDateTime start;
        private final LocalDateTime end;
        AddEventCommand(String desc, LocalDateTime start, LocalDateTime end) {
            this.desc = desc;
            this.start = start;
            this.end = end;
        }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            Task newTask = new Event(desc, start, end);
            tasks.add(newTask);
            storage.save(tasks);
            ui.showAdded(newTask, tasks);
        }
    }

    static class DeleteCommand extends Command {
        private final int index;
        DeleteCommand(int index) {
            this.index = index;
        }
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
            if (index < 1 || index > tasks.size()) {
                throw new DukeException("OOPS!!! Task number out of range.");
            }
            Task removed = tasks.remove(index - 1);
            storage.save(tasks);
            ui.showRemoved(removed, tasks);
        }
    }

    static class ExitCommand extends Command {
        @Override
        void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showBye();
        }
        @Override
        boolean isExit() {
            return true;
        }
    }
}
