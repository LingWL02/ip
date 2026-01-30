package app;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import app.task.TaskList;
import app.task.Todo;
import app.task.Deadline;
import app.task.Event;
import app.parser.RegexParser;
import app.parser.ParserTag;
import app.parser.DuplicatePatternException;
import utilities.Pair;


public class App {
    private final String botName;
    private final String lineSeparator;
    private Boolean isAlive = true;

    private final Scanner appScanner = new Scanner(System.in);
    private final TaskList taskList = new TaskList();
    private final RegexParser<ParserTag> regexParser = new RegexParser<ParserTag>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public App(String botName, String lineSeparator) {
        this.botName = botName;
        this.lineSeparator = lineSeparator;
    }


    public void run() {
        System.out.printf("%s\n\n", this.lineSeparator);

        try {
            this.configureApp();
        }
        catch (Exception exception) {  // TODO catch more specific exceptions
            this.printToStdOut(
                    "Received exception: %s\nTerminating app...".formatted(exception.toString())
            );
            return;
        }
        this.printToStdOut("Hello! I'm %s!\nWhat can I do for you?".formatted(this.botName));

        while(this.isAlive) {
            String userInput = this.appScanner.nextLine();

            List<Pair<ParserTag, Matcher>> parsedResults = this.regexParser.parse(userInput);

            if (parsedResults.isEmpty()) {
                this.printToStdOut("Unrecognized command, please try again.");
                continue;
            }
            else if (parsedResults.size() > 1) {
                this.printToStdOut("ERROR: User Input matched multiple entries.\nTerminating app...");
                return;
            }
            this.handleParsedResults(parsedResults.getFirst());
        }

        this.printToStdOut("Bye. Hope to see you again soon!");
    }


    private void printToStdOut(String message) {
        System.out.printf("%s\n%s\n\n", message, this.lineSeparator);
    }


    private void configureApp() throws DuplicatePatternException {
        this.regexParser.addPatternTagMappings(
            Map.ofEntries(
                Map.entry(Pattern.compile("^\\s*bye\\s*$"), ParserTag.BYE),
                Map.entry(Pattern.compile("^\\s*list\\s*$"), ParserTag.LIST),
                Map.entry(Pattern.compile("^\\s*add\\s+(.*\\S+)$"), ParserTag.ADD),
                Map.entry(Pattern.compile("^\\s*mark\\s+(\\d+)\\s*$"), ParserTag.MARK),
                Map.entry(Pattern.compile("^\\s*unmark\\s+(\\d+)\\s*$"), ParserTag.UNMARK),
                Map.entry(Pattern.compile("^\\s*todo\\s+(.*\\S+)\\s*$"), ParserTag.TODO),
                Map.entry(Pattern.compile(
                """
                ^\\s*deadline\\s+(?<name>.+?)\\s+
                -by\\s+(?<by>\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s*$
                """, Pattern.COMMENTS
                ), ParserTag.DEADLINE),
                Map.entry(Pattern.compile(
                """
                ^\\s*event\\s+(?<name>.+?)\\s+
                -start\\s+(?<start>\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s+
                -end\\s+(?<end>\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s*$
                """, Pattern.COMMENTS
                ), ParserTag.EVENT)
            )
        );
    }


    private void handleParsedResults(Pair<ParserTag, Matcher> parsedResult) {
        ParserTag tag = parsedResult.getKey();
        Matcher matcher = parsedResult.getValue();

        switch (tag) {
            case ParserTag.BYE -> this.isAlive = false;

            case ParserTag.LIST -> this.printToStdOut("Task List:\n%s".formatted(this.taskList.toString()));

            case ParserTag.ADD -> {
                this.printToStdOut("PROHIBITED: Command is deprecated");
            }
            case ParserTag.MARK -> {
                Integer index = Integer.parseUnsignedInt(matcher.group(1));
                try {
                    this.printToStdOut("Marked:\n%s".formatted(this.taskList.mark(index).toString()));
                }
                catch (Exception exception) {
                    printToStdOut(exception.toString());  // TODO catch more specific exceptions
                }
            }
            case ParserTag.UNMARK -> {
                Integer index = Integer.parseUnsignedInt(matcher.group(1));
                try {
                    this.printToStdOut("Unmarked:\n%s".formatted(this.taskList.unmark(index)));
                }
                catch (Exception exception) {
                    printToStdOut(exception.getMessage());  // TODO catch more specific exceptions
                }
            }
            case ParserTag.TODO -> {
                String name = matcher.group(1);
                Todo todo = new Todo(name);
                this.taskList.add(todo);
                this.printToStdOut("Todo added:\n%s".formatted(todo.toString()));
            }
            case ParserTag.DEADLINE -> {
                String name = matcher.group("name");
                LocalDateTime by = LocalDateTime.parse(matcher.group("by"), this.dateTimeFormatter);

                Deadline deadline = new Deadline(name, by);
                this.taskList.add(deadline);
                this.printToStdOut("Deadline added:\n%s".formatted(deadline.toString()));
            }
            case ParserTag.EVENT -> {
                String name = matcher.group("name");
                LocalDateTime start = LocalDateTime.parse(matcher.group("start"), this.dateTimeFormatter);
                LocalDateTime end = LocalDateTime.parse(matcher.group("end"), this.dateTimeFormatter);

                Event event = new Event(name, start, end);
                this.taskList.add(event);
                this.printToStdOut("Event added:\n%s".formatted(event.toString()));
            }
            default -> this.printToStdOut("TODO: Tag not implemented.");
        }
    }
}
