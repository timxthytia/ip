import java.util.Scanner;

public class Tim {
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
            if (input.equals("bye")) {
                System.out.println("Bye! Hope to see you again soon!");
                break;
            } else {
                System.out.println(" " + input);
            }
        }
        sc.close();
    }
}
