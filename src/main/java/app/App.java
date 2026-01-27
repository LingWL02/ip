package app;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Scanner;

public class App {
    private final String botName;
    private final String lineSeparator;

    private final Scanner appScanner = new Scanner(System.in);
    private final List<String> taskList = new ArrayList<String>();

    public App(String botName, String lineSeparator) {
        this.botName = botName;
        this.lineSeparator = lineSeparator;
    }

    public void run() {
        System.out.printf(
            "%s\nHello! I'm %s!\nWhat can I do for you?\n%s\n\n",this.lineSeparator, this.botName, this.lineSeparator
        );
        while(true) {
            String userInput = this.appScanner.nextLine();
            if (userInput.equals("bye")) break;

            System.out.printf("%s\n%s\n\n", userInput, this.lineSeparator);
        }
        System.out.printf("%s\n\nBye. Hope to see you again soon!\n%s", this.lineSeparator, this.lineSeparator);
    }
}
