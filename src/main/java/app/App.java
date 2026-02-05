package app;

import java.time.DateTimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.io.IOException;

import app.task.Deadline;
import app.task.Event;
import app.task.TaskIsMarkedException;
import app.task.TaskIsUnmarkedException;
import app.task.TaskList;
import app.task.Todo;
import app.parser.RegexParser;
import app.parser.ParserTag;
import utilities.Pair;

/**
 * Main application class for the Duke chatbot.
 * This class handles the main application loop, user input parsing,
 * and command execution for task management operations.
 *
 * <p>Supported commands include: bye, list, mark, unmark, todo, deadline, event, delete.</p>
 */
public class App {

    /** The display name of the chatbot. */
    private final String botName;

    /** The line separator used for formatting console output. */
    private final String lineSeparator;

    /** Flag indicating whether the application is running. */
    private Boolean isAlive = true;

    /** Scanner for reading user input from the console. */
    private final Scanner appScanner = new Scanner(System.in);

    /** The task list manager for storing and manipulating tasks. */
    private final TaskList taskList = new TaskList(".\\data\\tasks.txt");

    /** The regex parser for parsing and routing user commands. */
    private final RegexParser<ParserTag> regexParser = new RegexParser<ParserTag>();

    /**
     * Constructs a new App instance with the specified bot name and line separator.
     *
     * @param botName       The display name of the chatbot.
     * @param lineSeparator The string used to separate output lines for formatting.
     */
    public App(String botName, String lineSeparator) {
        this.botName = botName;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Runs the main application loop.
     * Initializes the task list and parser, displays a greeting,
     * then continuously reads and processes user commands until 'bye' is entered.
     */
    public void run() {
        System.out.printf("%s\n\n", this.lineSeparator);

        try {
            this.configureTaskList();
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

    /**
     * Prints a message to standard output with the line separator.
     *
     * @param message The message to print.
     */
    private void printToStdOut(String message) {
        System.out.printf("%s\n%s\n\n", message, this.lineSeparator);
    }

    /**
     * Configures the task list by registering task types and loading existing tasks.
     * Registers Todo, Deadline, and Event task types for deserialization.
     *
     * @throws Exception If task registration or loading fails.
     */
    private void configureTaskList() throws Exception {
        this.taskList.subscribeTaskDeserialization(
            Arrays.asList(Todo.class, Deadline.class, Event.class)
        );
        this.taskList.load();
    }

    /**
     * Configures the regex parser with command patterns.
     * Registers patterns for all supported commands: bye, list, mark, unmark,
     * todo, deadline, event, and delete.
     *
     * @throws Exception If pattern registration fails due to duplicate patterns.
     */
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

    /**
     * Routes a parsed command result to the appropriate handler method.
     *
     * @param parsedResult A pair containing the command tag and the regex matcher with captured groups.
     */
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

    /**
     * Handles the 'bye' command to exit the application.
     * Sets the isAlive flag to false if no extraneous arguments are provided.
     *
     * @param matcher The regex matcher containing captured groups from the command.
     */
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

    /**
     * Handles the 'list' command to display all tasks.
     * Prints an error if extraneous arguments are provided.
     *
     * @param matcher The regex matcher containing captured groups from the command.
     */
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

    /**
     * Handles the 'mark' command to mark a task as completed.
     * Expects a 1-based index argument specifying which task to mark.
     *
     * @param matcher The regex matcher containing the index captured group.
     */
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
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'unmark' command to unmark a task (mark as not completed).
     * Expects a 1-based index argument specifying which task to unmark.
     *
     * @param matcher The regex matcher containing the index captured group.
     */
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
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'todo' command to create a new Todo task.
     * Expects a name argument for the task description.
     *
     * @param matcher The regex matcher containing the name captured group.
     */
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
        try {
            this.taskList.add(todo);
            this.printToStdOut("Todo added:\n%s".formatted(todo.toString()));
        }
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'deadline' command to create a new Deadline task.
     * Expects a name and a '-by' flag with date/time in format: YYYY-MM-DD[,HH:MM].
     * If time is not specified, defaults to 23:59.
     *
     * @param matcher The regex matcher containing name, year, month, day, hour, minute captured groups.
     */
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
        boolean includeByTime = (hourString != null && minuteString != null);

        try {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
            Deadline deadline = new Deadline(name, dateTime, includeByTime);
            this.taskList.add(deadline);
            this.printToStdOut("Deadline added:\n%s".formatted(deadline.toString()));
        }
        catch (DateTimeException exception) {
            printIllegalArguments(expectedFormatMessage, exception.getMessage());
        }
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'event' command to create a new Event task.
     * Expects a name, '-from' flag with start date/time, and '-to' flag with end date/time.
     * Date/time format: YYYY-MM-DD[,HH:MM]. If start time is not specified, defaults to 00:00.
     * If end time is not specified, defaults to 23:59.
     *
     * @param matcher The regex matcher containing name, from/to date/time captured groups.
     */
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
        boolean includeStartTime = (fromHourStr != null && fromMinStr != null);
        int toMin = (toMinStr == null) ? 59 : Integer.parseUnsignedInt(toMinStr);
        boolean includeEndTime = (toHourStr != null && toMinStr != null);

        try {
            LocalDateTime startDateTime = LocalDateTime.of(fromYear, fromMonth, fromDay, fromHour, fromMin);
            LocalDateTime endDateTime = LocalDateTime.of(toYear, toMonth, toDay, toHour, toMin);
            Event event = new Event(name.strip(), startDateTime, includeStartTime, endDateTime, includeEndTime);
            this.taskList.add(event);
            this.printToStdOut("Event added:\n%s".formatted(event.toString()));
        }
        catch (DateTimeException exception) {
            printIllegalArguments(expectedFormatMessage, exception.getMessage());
        }
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'delete' command to remove a task from the list.
     * Expects a 1-based index argument specifying which task to delete.
     *
     * @param matcher The regex matcher containing the index captured group.
     */
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
        catch (IOException | ReflectiveOperationException | SecurityException exception) {
            this.printInternalError(
                "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Prints an error message for illegal arguments.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     */
    private void printIllegalArguments(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nILLEGAL ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    /**
     * Prints an error message for missing arguments.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     */
    private void printMissingArguments(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nMISSING ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    /**
     * Prints an error message for disallowed operations.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     */
    private void printDisallowed(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nDISALLOWED: %s".formatted(expectedFormatMessage, errorMessage));
    }

    /**
     * Prints an error message for missing command flags.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     */
    private void printMissingFlags(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nMISSING FLAGS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    /**
     * Prints an error message for illegal flag values.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     */
    private void printIllegalFlags(String expectedFormatMessage, String errorMessage) {
        this.printToStdOut("EXPECTED FORMAT: %s\nILLEGAL FLAGS: %s".formatted(expectedFormatMessage, errorMessage));
    }

    /**
     * Prints an internal error message.
     *
     * @param errorMessage The error description.
     */
    private void printInternalError(String errorMessage) {
        this.printToStdOut("INTERNAL ERROR: %s".formatted(errorMessage));
    }
}