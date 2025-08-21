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
    static Task[] tasks = new Task[100];
    static int taskCount = 0;
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
            if (input.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks[i]);
                }
            } else if (input.startsWith("mark ")){
                String index = input.substring(5).trim();
                int idx = Integer.parseInt(index);
                Task done = tasks[idx - 1];
                done.markAsDone();
                System.out.println("Nice! I've marked this task as done:");
                System.out.println(" " + done);
            } else if (input.startsWith("unmark ")){
                String index = input.substring(7).trim();
                int idx = Integer.parseInt(index);
                Task undone = tasks[idx - 1];
                undone.markAsUndone();
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println(" " + undone);
            } else if (input.equals("bye")){
                System.out.println("Bye! Hope to see you again soon!");
                break;
            } else if (input.startsWith("todo")) {
                String desc = input.substring(4).trim();
                Task newTask = new Todo(desc);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println("Got it. I've added this task:");
                System.out.println(" " + newTask);
                System.out.println("Now you have " + taskCount + " tasks in the list.");
            } else if (input.startsWith("deadline")) {
                String body = input.substring("deadline".length()).trim();
                String[] parts = body.split("/by", 2);
                String desc = parts[0].trim();
                String date = parts[1].trim();
                Task newTask = new Deadline(desc, date);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println("Got it. I've added this task:");
                System.out.println(" " + newTask);
                System.out.println("Now you have " + taskCount + " tasks in the list.");
            } else if (input.startsWith("event")) {
                String body = input.substring("event".length()).trim();
                String[] dateSplit = body.split("/from", 2);
                String desc = dateSplit[0].trim();
                String[] toSplit = dateSplit[1].split("/to",2);
                String start = toSplit[0].trim();
                String end = toSplit[1].trim();

                Task newTask = new Event(desc, start, end);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println("Got it. I've added this task:");
                System.out.println(" " + newTask);
                System.out.println("Now you have " + taskCount + " tasks in the list.");
            } else {
                Task newTask = new Task(input);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println(" added: " + newTask);
            }
        }
        sc.close();
    }
}
