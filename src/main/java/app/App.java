package app;

import java.awt.event.PaintEvent;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

import app.task.TaskList;
import app.parser.RegexParser;
import app.parser.ParserTag;
import app.parser.DuplicatePatternException;
import utilities.Pair;

import javax.swing.text.html.parser.Parser;

public class App {
    private final String botName;
    private final String lineSeparator;

    private final Scanner appScanner = new Scanner(System.in);
    private final TaskList taskList = new TaskList();
    private final RegexParser<ParserTag> regexParser = new RegexParser<ParserTag>();

    public App(String botName, String lineSeparator) {
        this.botName = botName;
        this.lineSeparator = lineSeparator;
    }

    public void run() {
        System.out.printf("%s\n\n", this.lineSeparator);
        try {
            this.regexParser.addPatternTagMappings(
                    Map.ofEntries(
                            Map.entry(Pattern.compile("^bye$"), ParserTag.BYE),
                            Map.entry(Pattern.compile("^list$"), ParserTag.LIST),
                            Map.entry(Pattern.compile("^add$"), ParserTag.ADD)
                    )
            );
        }
        catch (DuplicatePatternException exception) {
            this.printToStdOut(
                    "Received exception: %s\nTerminating app...".formatted(exception.toString())
            );
        }

        this.printToStdOut("Hello! I'm %s!\nWhat can I do for you?".formatted(this.botName));

        appLoop: while(true) {
            String userInput = this.appScanner.nextLine();

            List<Pair<ParserTag, Matcher>> parsedResults = this.regexParser.parse(userInput);

            if (parsedResults.isEmpty()) {
//                this.printToStdOut("Unrecognized command, please try again.");
                this.taskList.add(userInput.trim());
                this.printToStdOut("Added: %s".formatted(userInput.trim()));
                continue;
            }
            else if (parsedResults.size() > 1) {
                this.printToStdOut("ERROR: User Input matched multiple entries.\nTerminating app...");
                return;
            }

            Pair<ParserTag, Matcher> parsedResult = parsedResults.getFirst();
            ParserTag tag = parsedResult.getKey();
            Matcher matcher = parsedResult.getValue();

            switch (tag) {
                case ParserTag.BYE -> {
                    break appLoop;
                }

                case ParserTag.LIST -> this.printToStdOut(this.taskList.toString());

                default -> this.printToStdOut("TODO: Tag not implemented.");
            }
        }

        this.printToStdOut("Bye. Hope to see you again soon!");
    }

    private void printToStdOut(String message) {
        System.out.printf("%s\n%s\n\n", message, this.lineSeparator);
    }
}
