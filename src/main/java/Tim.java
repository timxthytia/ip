import java.util.Scanner;
import java.util.ArrayList;

public class Tim {
    // Nested Task class
    static class Task {
        protected String description;
        protected boolean isDone;

        Task(String description) {
            this.description = description;
            this.isDone = false;
        }

        void markAsDone() {
            this.isDone = true;
        }

        void markAsUndone() {
            this.isDone = false;
        }

        String getStatusIcon() {
            return (isDone ? "X" : " ");
        }

        @Override
        public String toString() {
            return "[" + getStatusIcon() + "] " + description;
        }
    }
    static ArrayList<Task> tasks = new ArrayList<>(100);
    public static void main(String[] args) {
        /*
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
         */
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello I'm Tim\nWhat can I do for you? \n");
        while (true) {
            String input = sc.nextLine();
            if (input.equals("list")) {
                System.out.println("Here are the tasks in your list:\n");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks.get(i));
                }
            } else if (input.startsWith("mark ")){
                String index = input.substring(5).trim();
                int idx = Integer.parseInt(index);
                Task done = tasks.get(idx - 1);
                done.markAsDone();
                System.out.println("Nice! I've marked this task as done:");
                System.out.println(" " + done);
            } else if (input.startsWith("unmark ")){
                String index = input.substring(7).trim();
                int idx = Integer.parseInt(index);
                Task undone = tasks.get(idx - 1);
                undone.markAsUndone();
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println(" " + undone);
            } else if (input.equals("bye")){
                System.out.println("Bye! Hope to see you again soon!");
                break;
            } else {
                Task newTask = new Task(input);
                tasks.add(newTask);
                System.out.println(" added: " + input);
            }
        }
        sc.close();
    }
}
