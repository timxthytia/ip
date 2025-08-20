import java.util.Scanner;
import java.util.ArrayList;

public class Tim {
    static ArrayList<String> tasks = new ArrayList<>(100);
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
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks.get(i));
                }

            } else if (input.equals("bye")){
                System.out.println("Bye! Hope to see you again soon!");
                break;
            } else {
                tasks.add(input);
                System.out.println(" added: " + input);
            }
        }
        sc.close();
    }
}
