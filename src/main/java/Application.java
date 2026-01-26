import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Application {
    private final String name;
    private final String separator;

    private final Scanner appScanner = new Scanner(System.in);
    private final List<String> toDoList = new ArrayList<String>();

    public Application(String name, String separator) {
        this.name = name;
        this.separator = separator;
    }

    public void run() {
        System.out.printf(
            "%s\nHello! I'm %s!\nWhat can I do for you?\n%s\n\n",this.separator, this.name, this.separator
        );
        while(true) {
            String userInput = this.appScanner.nextLine();
            if (userInput.equals("bye")) break;

            System.out.printf("%s\n%s\n\n", userInput, this.separator);
        }
        System.out.printf("%s\n\nBye. Hope to see you again soon!\n%s", this.separator, this.separator);
    }

}
