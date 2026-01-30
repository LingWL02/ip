package app;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import app.task.*;
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
            this.configureParser();
        }
        catch (Exception exception) {  // TODO catch more specific exceptions
            this.printToStdOut(
                    "EXCEPTION: %s\nTerminating app...".formatted(exception.toString())
            );
            return;
        }
        this.printToStdOut("Hello! I'm %s!\nWhat can I do for you?".formatted(this.botName));

        while(this.isAlive) {
            String userInput = this.appScanner.nextLine();

            List<Pair<ParserTag, Matcher>> parsedResults = this.regexParser.parse(userInput);

            if (parsedResults.isEmpty()) {
                this.printToStdOut("UNRECOGNIZED COMMAND: Please try again.");
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


    private void configureParser() throws DuplicatePatternException {
        this.regexParser.addPatternTagMappings(
            Map.ofEntries(
                Map.entry(Pattern.compile("^\\s*bye\\b\\s*(?<arg>.*)$"), ParserTag.BYE),
                Map.entry(Pattern.compile("^\\s*list\\b\\s*(?<arg>.*)$"), ParserTag.LIST),
                Map.entry(Pattern.compile("^\\s*mark\\b\\s*(?<index>.*)\\s*$"), ParserTag.MARK),
                Map.entry(Pattern.compile("^\\s*unmark\\b\\s*(?<index>.*)\\s*$"), ParserTag.UNMARK),
                Map.entry(Pattern.compile("^\\s*todo\\b\\s+(.*\\S+)\\s*$"), ParserTag.TODO),
                Map.entry(Pattern.compile(
                """
                ^\\s*deadline\\b\\s+(?<name>.+?)\\s+
                -by\\s+(?<by>\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})\\s*$
                """, Pattern.COMMENTS
                ), ParserTag.DEADLINE),
                Map.entry(Pattern.compile(
                """
                ^\\s*event\\b\\s+(?<name>.+?)\\s+
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
            case ParserTag.BYE -> this.handleBye(matcher);

            case ParserTag.LIST -> this.handleList(matcher);

            case ParserTag.MARK -> this.handleMark(matcher);

            case ParserTag.UNMARK -> this.handleUnmark(matcher);

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


    private void handleBye(Matcher matcher) {
        String arg = matcher.group("arg");

        if (!arg.isBlank()) {
            this.printToStdOut("ILLEGAL ARGUMENTS: Command 'bye' does not accept any arguments.");
            return;
        }
        this.isAlive = false;
    }


    private void handleList(Matcher matcher) {
        String arg = matcher.group("arg");

        if (!arg.isBlank()) {
            this.printToStdOut("ILLEGAL ARGUMENTS: Command 'list' does not accept any arguments.");
            return;
        }
        this.printToStdOut("Task List:\n%s".formatted(this.taskList.toString()));
    }


    private void handleMark(Matcher matcher) {
        String indexString = matcher.group("index");

        if (indexString.isBlank()) {
            this.printToStdOut("ILLEGAL ARGUMENTS: Command 'mark' expects an argument 'index'.");
            return;
        }
        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            this.printToStdOut("Marked:\n%s".formatted(this.taskList.mark(index).toString()));
        }
        catch (IndexOutOfBoundsException | TaskIsMarkedException exception) {
            this.printToStdOut("DISALLOWED: %s".formatted(exception.getMessage()));
        }
        catch (NumberFormatException exception) {
            this.printToStdOut(
                "ILLEGAL ARGUMENTS: Expected 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        }
    }


    private void handleUnmark(Matcher matcher) {
        String indexString = matcher.group("index");

        if (indexString.isBlank()) {
            this.printToStdOut("ILLEGAL ARGUMENTS: Command 'unmark' expects an argument 'index'.");
            return;
        }
        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            this.printToStdOut("Unmarked:\n%s".formatted(this.taskList.unmark(index).toString()));
        }
        catch (IndexOutOfBoundsException | TaskIsUnmarkedException exception) {
            this.printToStdOut("DISALLOWED: %s".formatted(exception.getMessage()));
        }
        catch (NumberFormatException exception) {
            this.printToStdOut(
                    "ILLEGAL ARGUMENTS: Expected 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        }
    }


    private void handleTodo() {}


    private void handleDeadline() {}


    private void handleEvent() {}
}
