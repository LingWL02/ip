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
        catch (Exception exception) {
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


    private void printToStdOut (String message) {
        System.out.printf("%s\n%s\n\n", message, this.lineSeparator);
    }


    private void configureParser() throws DuplicatePatternException {
        this.regexParser.addPatternTagMappings(
                Map.ofEntries(
                        Map.entry(Pattern.compile("^\\s*bye\\b(?<arg>\\s+.*)?$"), ParserTag.BYE),
                        Map.entry(Pattern.compile("^\\s*list\\b(?<arg>\\s+.*)?$"), ParserTag.LIST),
                        Map.entry(Pattern.compile("^\\s*mark\\b(?<index>\\s+.*)?\\s*$"), ParserTag.MARK),
                        Map.entry(Pattern.compile("^\\s*unmark\\b(?<index>\\s+.*)?\\s*$"), ParserTag.UNMARK),
                        Map.entry(Pattern.compile("^\\s*todo\\b(?<name>\\s+.*)?\\s*$"), ParserTag.TODO),
                        Map.entry(Pattern.compile(
                        """
                            ^\\s*deadline\\b
                            (?<byField>\\s+-by\\b
                            (?<by>\\s+
                            (?<year>\\d+)-(?<month>\\d+)-(?<day>\\d+),(?<hour>\\d+):(?<minute>\\d+))?)?
                            (?<name>\\s+.+)?\\s*$
                            """, Pattern.COMMENTS), ParserTag.DEADLINE
                        ),
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
            case ParserTag.TODO -> this.handleTodo(matcher);
            case ParserTag.DEADLINE -> this.handleDeadline(matcher);
            case ParserTag.EVENT -> this.handleEvent(matcher);
            default -> this.printToStdOut("TODO: Tag not implemented.");
        }
    }


    private void handleBye(Matcher matcher) {
        String arg = matcher.group("arg");

        if (arg != null) {
            this.printIllegalArguments(
                "bye",
                "Command 'bye' does not accept any arguments.");
            return;
        }
        this.isAlive = false;
    }


    private void handleList(Matcher matcher) {
        String arg = matcher.group("arg");

        if (arg != null) {
            this.printIllegalArguments(
                "list",
                "Command 'list' does not accept any arguments.");
            return;
        }
        this.printToStdOut("Task List:\n%s".formatted(this.taskList.toString()));
    }


    private void handleMark(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "mark <index>";

        if (indexString == null) {
            this.printMissingArguments(
                expectedFormatMessage, "Command 'mark' expects argument 'index'."
            );
            return;
        }
        indexString = indexString.strip();

        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            this.printToStdOut("Marked:\n%s".formatted(this.taskList.mark(index).toString()));
        }
        catch (IndexOutOfBoundsException | TaskIsMarkedException exception) {
            this.printDisallowed(expectedFormatMessage, exception.getMessage());
        }
        catch (NumberFormatException exception) {
            this.printIllegalArguments(
                expectedFormatMessage,
                "Command 'mark' expects argument 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        }
    }


    private void handleUnmark(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "unmark <index>";

        if (indexString == null) {
            this.printMissingArguments(
                expectedFormatMessage, "Command 'unmark' expects argument 'index'."
            );
            return;
        }
        indexString = indexString.strip();

        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            this.printToStdOut("Unmarked:\n%s".formatted(this.taskList.unmark(index).toString()));
        }
        catch (IndexOutOfBoundsException | TaskIsUnmarkedException exception) {
            this.printDisallowed(expectedFormatMessage, exception.getMessage());
        }
        catch (NumberFormatException exception) {
            this.printIllegalArguments(
                expectedFormatMessage,
                "Command 'unmark' expects argument 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        }
    }


    private void handleTodo(Matcher matcher) {
        String name = matcher.group("name");
        if (name == null) {
            this.printMissingArguments(
                "todo <name>",
                "Command 'todo' expects argument 'name'."
            );
            return;
        }
        name = name.strip();
        Todo todo = new Todo(name);
        this.taskList.add(todo);
        this.printToStdOut("Todo added:\n%s".formatted(todo.toString()));
    }


    private void handleDeadline(Matcher matcher) {
        String expectedFormatMessage = "deadline -by <YYYY>-<MM>-<DD>,<HH>:<MM> <name>";
        String byField = matcher.group("byField");
        String by = matcher.group("by");
        String name = matcher.group("name");


        if (byField == null) {
            printMissingFlags(expectedFormatMessage, "Command 'deadline' expects flag '-by'");
            return;
        }

        if (by == null) {
            printIllegalFlags(expectedFormatMessage, "Command 'deadline' flag '-by' expects... TODO");
        }

        if (name == null) {
            printMissingArguments(expectedFormatMessage, "Command 'deadline' expects argument 'name'");
        }

        // Parse and add task logic goes here
    }


    private void handleEvent(Matcher matcher) {
        // TODO
    }

    private void printIllegalArguments(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nILLEGAL ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    private void printMissingArguments(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nMISSING ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    private void printDisallowed(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nDISALLOWED: %s".formatted(expectedFormatMessage, errorMessage));
    }

    private void printMissingFlags(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nMISSING FLAGS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    private void printIllegalFlags(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nILLEGAL FLAGS: %s".formatted(expectedFormatMessage, errorMessage));
    }
}