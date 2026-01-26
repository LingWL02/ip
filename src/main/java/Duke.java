import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duke {
    public static void main(String[] args) {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println("Hello from\n" + logo);

        String separator = "_".repeat(50);

        System.out.println(separator + "\nHello! I'm Duke!\nWhat can I do for you?\n"+ separator + "\n");

        Scanner myScanner = new Scanner(System.in);
        Pattern endPattern = Pattern.compile("bye", Pattern.CASE_INSENSITIVE);

        while(true) {
            String userInput = myScanner.nextLine();
            if (endPattern.matcher(userInput).find()) break;

            System.out.println(userInput + "\n" + separator + "\n");
        }

        System.out.println(separator + "\nBye. Hope to see you again soon!\n" + separator);
    }
}
