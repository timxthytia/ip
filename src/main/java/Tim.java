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
        private final LocalDateTime due;
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

    /* Code for Level-7*/
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

    // Parsing Date and Time */
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
                    saveTasks();
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
                    saveTasks();
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
                    saveTasks();
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

                    LocalDateTime dueDateTime;
                    try {
                        dueDateTime = parseStrictDateOrDateTime(date);
                    } catch (IllegalArgumentException ex) {
                        throw new DukeException("OOPS!!! I couldn't understand that date/time. " +
                                "Please use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 1800).");
                    }

                    Task newTask = new Deadline(desc, dueDateTime);
                    tasks.add(newTask);
                    saveTasks();
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

                    LocalDateTime startDateTime;
                    LocalDateTime endDateTime;
                    try {
                        startDateTime = parseStrictDateOrDateTime(start);
                        endDateTime = parseStrictDateOrDateTime(end);
                    } catch (IllegalArgumentException ex) {
                        throw new DukeException("OOPS!!! I couldn't understand those date/times. " +
                                "Please use yyyy-MM-dd or yyyy-MM-dd HHmm (e.g. 2019-10-15 0900).");
                    }

                    Task newTask = new Event(desc, startDateTime, endDateTime);
                    tasks.add(newTask);
                    saveTasks();
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
                    saveTasks();
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
