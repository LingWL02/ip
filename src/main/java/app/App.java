package app;

import java.time.DateTimeException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.time.LocalDateTime;

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
    private final TaskList taskList = new TaskList("data/tasks.txt");
    private final RegexParser<ParserTag> regexParser = new RegexParser<ParserTag>();


    public App(String botName, String lineSeparator) {
        this.botName = botName;
        this.lineSeparator = lineSeparator;
    }


    public void run() {
        System.out.printf("%s\n\n", this.lineSeparator);

        try {
            this.confgureTaskList();
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

    private void confgureTaskList() throws Exception {
        return;
    }


    private void configureParser() throws Exception {
        this.regexParser.addPatternTagMappings(
            Map.ofEntries(
                Map.entry(Pattern.compile("^\\s*bye\\b(?:\\s+(?<arg>.*))?\\s*$"), ParserTag.BYE),
                Map.entry(Pattern.compile("^\\s*list\\b(?:\\s+(?<arg>.*))?\\s*$"), ParserTag.LIST),
                Map.entry(Pattern.compile("^\\s*mark\\b(?:\\s+(?<index>.*))?\\s*$"), ParserTag.MARK),
                Map.entry(Pattern.compile("^\\s*unmark\\b(?:\\s+(?<index>.*))?\\s*$"), ParserTag.UNMARK),
                Map.entry(Pattern.compile("^\\s*todo\\b(?:\\s+(?<name>.*))?\\s*$"), ParserTag.TODO),
                Map.entry(Pattern.compile(
                    """
                    ^\\s*deadline\\b
                    (?<byField>\\s+-by\\b
                    (?<by>\\s+
                    (?<year>\\d{4})-(?<month>\\d{1,2})-(?<day>\\d{1,2})
                    (?:\\s*,\\s*(?<hour>\\d{1,2}):(?<minute>\\d{1,2}))?)?)?
                    (?:\\s+(?<name>.*))?\\s*$
                    """, Pattern.COMMENTS), ParserTag.DEADLINE
                ),
                Map.entry(Pattern.compile(
                    """
                    ^\\s*event\\b
                    (?<fromField>\\s+-from\\b
                    (?<from>\\s+
                    (?<fromYear>\\d{4})-(?<fromMonth>\\d{1,2})-(?<fromDay>\\d{1,2})
                    (?:\\s*,\\s*(?<fromHour>\\d{1,2}):(?<fromMinute>\\d{1,2}))?)?)?
                    (?<toField>\\s+-to\\b
                    (?<to>\\s+
                    (?<toYear>\\d{4})-(?<toMonth>\\d{1,2})-(?<toDay>\\d{1,2})
                    (?:\\s*,\\s*(?<toHour>\\d{1,2}):(?<toMinute>\\d{1,2}))?)?)?
                    (?:\\s+(?<name>.*))?\\s*$
                    """, Pattern.COMMENTS), ParserTag.EVENT
                ),
                Map.entry(Pattern.compile("^\\s*delete\\b(?:\\s+(?<index>.*))?\\s*$"), ParserTag.DELETE)
            )
        );
    }


    private void handleParsedResults(Pair<ParserTag, Matcher> parsedResult) {
        ParserTag tag = parsedResult.getKey();
        Matcher matcher = parsedResult.getValue();

        switch (tag) {
            case BYE -> this.handleBye(matcher);
            case LIST -> this.handleList(matcher);
            case MARK -> this.handleMark(matcher);
            case UNMARK -> this.handleUnmark(matcher);
            case TODO -> this.handleTodo(matcher);
            case DEADLINE -> this.handleDeadline(matcher);
            case EVENT -> this.handleEvent(matcher);
            case DELETE -> this.handleDelete(matcher);
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
        String expectedFormatMessage = "deadline -by <year>-<month>-<day>[,<hour>:<minute>] <name>";
        String byField = matcher.group("byField");
        String by = matcher.group("by");
        String name = matcher.group("name");


        if (byField == null) {
            printMissingFlags(expectedFormatMessage, "Command 'deadline' expects flag '-by'");
            return;
        }
        if (by == null) {
            printIllegalFlags(
                expectedFormatMessage,
                "Command 'deadline' flag '-by' expects date and time in the specified format."
            );
            return;
            }
        if (name == null) {
            printMissingArguments(expectedFormatMessage, "Command 'deadline' expects argument 'name'");
            return;
        }
        String yearString = matcher.group("year");
        String monthString = matcher.group("month");
        String dayString = matcher.group("day");
        String hourString = matcher.group("hour");
        String minuteString = matcher.group("minute");

        int year = Integer.parseUnsignedInt(yearString);
        int month = Integer.parseUnsignedInt(monthString);
        int day = Integer.parseUnsignedInt(dayString);
        int hour = (hourString == null) ? 23 : Integer.parseUnsignedInt(hourString);
        int minute = (minuteString == null) ? 59 : Integer.parseUnsignedInt(minuteString);

        try {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
            Deadline deadline = new Deadline(name, dateTime);
            this.taskList.add(deadline);
            this.printToStdOut("Deadline added:\n%s".formatted(deadline.toString()));
        }
        catch (DateTimeException exception) {
            printIllegalArguments(expectedFormatMessage, exception.getMessage());
        }
    }


    private void handleEvent(Matcher matcher) {
        String expectedFormatMessage =
            """
            event -from <year>-<month>-<day>[,<hour>:<minute>] -to <year>-<month>-<day>[,<hour>:<minute>] <name>
            """;

        String fromField = matcher.group("fromField");
        String from = matcher.group("from");
        String toField = matcher.group("toField");
        String to = matcher.group("to");
        String name = matcher.group("name");

        if (fromField == null) {
            this.printMissingFlags(expectedFormatMessage, "Command 'event' expects flag '-from'.");
            return;
        }
        if (toField == null) {
            this.printMissingFlags(expectedFormatMessage, "Command 'event' expects flag '-to'.");
            return;
        }
        if (from == null) {
            this.printIllegalFlags(
                    expectedFormatMessage,
                    "Command 'event' flag '-from' expects a valid date/time.");
            return;
        }
        if (to == null) {
            this.printIllegalFlags(
                    expectedFormatMessage,
                    "Command 'event' flag '-to' expects a valid date/time.");
            return;
        }
        if (name == null) {
            this.printMissingArguments(expectedFormatMessage, "Command 'event' expects argument 'name'.");
            return;
        }
        int fromYear = Integer.parseUnsignedInt(matcher.group("fromYear"));
        int fromMonth = Integer.parseUnsignedInt(matcher.group("fromMonth"));
        int fromDay = Integer.parseUnsignedInt(matcher.group("fromDay"));
        String fromHourStr = matcher.group("fromHour");
        String fromMinStr = matcher.group("fromMinute");
        int fromHour = (fromHourStr == null) ? 0 : Integer.parseUnsignedInt(fromHourStr);
        int fromMin = (fromMinStr == null) ? 0 : Integer.parseUnsignedInt(fromMinStr);

        int toYear = Integer.parseUnsignedInt(matcher.group("toYear"));
        int toMonth = Integer.parseUnsignedInt(matcher.group("toMonth"));
        int toDay = Integer.parseUnsignedInt(matcher.group("toDay"));
        String toHourStr = matcher.group("toHour");
        String toMinStr = matcher.group("toMinute");
        int toHour = (toHourStr == null) ? 23 : Integer.parseUnsignedInt(toHourStr);
        int toMin = (toMinStr == null) ? 59 : Integer.parseUnsignedInt(toMinStr);

        try {
            LocalDateTime startDateTime = LocalDateTime.of(fromYear, fromMonth, fromDay, fromHour, fromMin);
            LocalDateTime endDateTime = LocalDateTime.of(toYear, toMonth, toDay, toHour, toMin);
            Event event = new Event(name.strip(), startDateTime, endDateTime);
            this.taskList.add(event);
            this.printToStdOut("Event added:\n%s".formatted(event.toString()));
        }
        catch (DateTimeException exception) {
            printIllegalArguments(expectedFormatMessage, exception.getMessage());
        }
    }


    private void handleDelete(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "delete <index>";

        if (indexString == null) {
            this.printMissingArguments(
                expectedFormatMessage, "Command 'index' expects argument 'index'."
            );
            return;
        }
        indexString = indexString.strip();

        try {
            int index = Integer.parseUnsignedInt(indexString);
            int sizeAfter = this.taskList.getSize() - 1;
            this.printToStdOut(
                "Deleted:\n%s\n%d %s remaining,".formatted(
                    this.taskList.pop(index).toString(), sizeAfter, (sizeAfter > 1) ? "tasks" : "task"
                )
            );
        }
        catch (IndexOutOfBoundsException exception) {
            this.printDisallowed(expectedFormatMessage, exception.getMessage());
        }
        catch (NumberFormatException exception) {
            this.printIllegalArguments(
                expectedFormatMessage,
                "Command 'delete' expects argument 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        }
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