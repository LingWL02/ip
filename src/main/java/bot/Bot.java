package bot;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bot.cheerleader.Cheerleader;
import bot.parser.ParserTag;
import bot.parser.RegexParser;
import bot.storage.TaskStorage;
import bot.task.Deadline;
import bot.task.Event;
import bot.task.Task;
import bot.task.TaskIsMarkedException;
import bot.task.TaskIsUnmarkedException;
import bot.task.TaskList;
import bot.task.TaskTag;
import bot.task.TaskTagAlreadyExistsException;
import bot.task.TaskTagDoesNotExistException;
import bot.task.Todo;
import utilities.Pair;

/**
 * Main application class for the Duke chatbot.
 * This class handles the main application loop, user input parsing,
 * and command execution for task management operations.
 *
 * <p>Supported commands include: bye, list, mark, unmark, todo, deadline, event, delete.</p>
 */
public class Bot {

    // Pattern constants for command parsing
    private static final String BYE_PATTERN = "^\\s*bye\\b(?:\\s+(?<arg>.*))?\\s*$";
    private static final String LIST_PATTERN = "^\\s*list\\b(?:\\s+(?<arg>.*))?\\s*$";
    private static final String MARK_PATTERN = "^\\s*mark\\b(?:\\s+(?<index>.*))?\\s*$";
    private static final String UNMARK_PATTERN = "^\\s*unmark\\b(?:\\s+(?<index>.*))?\\s*$";
    private static final String TODO_PATTERN = "^\\s*todo\\b(?:\\s+(?<name>.*))?\\s*$";
    private static final String DEADLINE_PATTERN = """
            ^\\s*deadline\\b
            (?<byField>\\s+-by\\b
            (?<by>\\s+
            (?<year>\\d{4})-(?<month>\\d{1,2})-(?<day>\\d{1,2})
            (?:\\s*,\\s*(?<hour>\\d{1,2}):(?<minute>\\d{1,2}))?)?)?
            (?:\\s+(?<name>.*))?\\s*$
            """;
    private static final String EVENT_PATTERN = """
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
            """;
    private static final String DELETE_PATTERN = "^\\s*delete\\b(?:\\s+(?<index>.*))?\\s*$";
    private static final String FIND_PATTERN = "^\\s*find\\b(?:\\s+(?<keyword>.*))?\\s*$";
    private static final String CHEER_PATTERN = "^\\s*cheer\\b(?:\\s+(?<arg>.*))?\\s*$";
    private static final String TAG_PATTERN = """
            ^\\s*tag\\b
            (?<nameFields>\\s+-names\\b
            (?<names>\\s+[a-zA-Z0-9]+
            (?:\\s*,\\s*[a-zA-Z0-9]+)*)?)?
            (?:\\s+(?<index>.*))?\\s*$
            """;
    private static final String UNTAG_PATTERN = """
            ^\\s*untag\\b
            (?<nameFields>\\s+-names\\b
            (?<names>\\s+[a-zA-Z0-9]+
            (?:\\s*,\\s*[a-zA-Z0-9]+)*)?)?
            (?:\\s+(?<index>.*))?\\s*$
            """;

    /**
     * The display name of the chatbot.
     */
    private final String name;

    /**
     * The line separator used for formatting console output.
     */
    private final String lineSeparator;

    /**
     * Flag indicating whether the application is running.
     */
    private Boolean isAlive = true;

    private Boolean isPersistent = true;

    /**
     * Scanner for reading user input from the console.
     */
    private final Scanner appScanner = new Scanner(System.in);

    /**
     * The task list manager for storing and manipulating tasks.
     */
    private final TaskList taskList = new TaskList();

    /**
     * The regex parser for parsing and routing user commands.
     */
    private final RegexParser<ParserTag> regexParser = new RegexParser<ParserTag>();

    private final TaskStorage taskStorage = new TaskStorage(".\\data\\tasks.txt");

    private final Cheerleader cheerleader = new Cheerleader(".\\data\\cheer.txt");

    /**
     * Constructs a new App instance with the specified bot name and line separator.
     *
     * @param name       The display name of the chatbot.
     * @param lineSeparator The string used to separate output lines for formatting.
     */
    public Bot(String name, String lineSeparator) {
        assert name != null : "Bot name cannot be null";
        assert !name.trim().isEmpty() : "Bot name cannot be empty";
        assert lineSeparator != null : "Line separator cannot be null";
        this.name = name;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Constructs a new Bot instance with the specified bot name, line separator, and persistence mode.
     *
     * @param name The display name of the chatbot.
     * @param lineSeperator The string used to separate output lines for formatting.
     * @param isPersistent Whether the bot should persist data to storage.
     */
    public Bot(String name, String lineSeperator, Boolean isPersistent) {
        this(name, lineSeperator);
        this.isPersistent = isPersistent;
    }

    /**
     * Runs the main application loop.
     * Initializes the task list and parser, displays a greeting,
     * then continuously reads and processes user commands until 'bye' is entered.
     */
    public void run() {
        System.out.printf("%s\n\n", this.lineSeparator);

        try {
            this.initialize();
        } catch (Exception exception) {
            this.printToStdOut(
                    "EXCEPTION: %s\nTerminating app...".formatted(exception.toString())
            );
            return;
        }
        this.printToStdOut(this.getGreeting());

        while (this.isAlive) {
            if (!this.appScanner.hasNextLine()) {
                return;
            }
            String userInput = this.appScanner.nextLine();
            String response = this.getResponse(userInput);
            this.printToStdOut(response);
        }
    }

    /**
     * Prints a message to standard output with the line separator.
     *
     * @param message The message to print.
     */
    private void printToStdOut(String message) {
        System.out.printf("%s\n%s\n\n", message, this.lineSeparator);
    }

    private void configureTaskStorage() throws Exception {
        this.taskStorage.subscribeTaskDeserialization(
            Todo.class, Deadline.class, Event.class
        );
    }

    /**
     * Configures the task list by registering task types and loading existing tasks.
     * Registers Todo, Deadline, and Event task types for deserialization.
     *
     * @throws Exception If task registration or loading fails.
     */
    private void configureTaskList() throws Exception {
        if (this.isPersistent) {
            this.taskList.mountStorage(this.taskStorage);
        }
    }


    private void configureCheerLeader() throws Exception {
        this.cheerleader.load();
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
                        Map.entry(Pattern.compile(BYE_PATTERN), ParserTag.BYE),
                        Map.entry(Pattern.compile(LIST_PATTERN), ParserTag.LIST),
                        Map.entry(Pattern.compile(MARK_PATTERN), ParserTag.MARK),
                        Map.entry(Pattern.compile(UNMARK_PATTERN), ParserTag.UNMARK),
                        Map.entry(Pattern.compile(TODO_PATTERN), ParserTag.TODO),
                        Map.entry(Pattern.compile(DEADLINE_PATTERN, Pattern.COMMENTS), ParserTag.DEADLINE),
                        Map.entry(Pattern.compile(EVENT_PATTERN, Pattern.COMMENTS), ParserTag.EVENT),
                        Map.entry(Pattern.compile(DELETE_PATTERN), ParserTag.DELETE),
                        Map.entry(Pattern.compile(FIND_PATTERN), ParserTag.FIND),
                        Map.entry(Pattern.compile(CHEER_PATTERN), ParserTag.CHEER),
                        Map.entry(Pattern.compile(TAG_PATTERN, Pattern.COMMENTS), ParserTag.TAG),
                        Map.entry(Pattern.compile(UNTAG_PATTERN, Pattern.COMMENTS), ParserTag.UNTAG)
                )
        );
    }

    /**
     * Initializes the bot by configuring all necessary components.
     *
     * @throws Exception if any component fails to initialize
     */
    public void initialize() throws Exception {
        try {
            this.configureTaskStorage();
            this.configureTaskList();
            this.configureParser();
            this.configureCheerLeader();
        } catch (Exception exception) {
            this.isAlive = false;
            throw exception;
        }
    }

    public String getGreeting() {
        return "Hello! I'm %s!\nWhat can I do for you?".formatted(this.name);

    }

    public String getFarewell() {
        return "Bye. Hope to see you again soon!";
    }

    /**
     * Checks if the bot is still alive/running.
     *
     * @return true if the bot is still alive, false if it should be terminated
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    public String getResponse(String input) {
        List<Pair<ParserTag, Matcher>> parsedResults = this.regexParser.parse(input);

        if (parsedResults.isEmpty()) {
            return "UNRECOGNIZED COMMAND: Please try again.";
        } else if (parsedResults.size() > 1) {
            this.isAlive = false; // Terminate app for multiple matches
            return "ERROR: User Input matched multiple entries.\nTerminating app...";
        }
        return this.handleParsedResults(parsedResults.getFirst());
    }

    /**
     * Routes a parsed command result to the appropriate handler method.
     *
     * @param parsedResult A pair containing the command tag and the regex matcher with captured groups.
     * @return The response string for the command
     */
    private String handleParsedResults(Pair<ParserTag, Matcher> parsedResult) {
        ParserTag tag = parsedResult.getKey();
        Matcher matcher = parsedResult.getValue();

        return switch (tag) {
        case BYE -> this.handleBye(matcher);
        case LIST -> this.handleList(matcher);
        case MARK -> this.handleMark(matcher);
        case UNMARK -> this.handleUnmark(matcher);
        case TODO -> this.handleTodo(matcher);
        case DEADLINE -> this.handleDeadline(matcher);
        case EVENT -> this.handleEvent(matcher);
        case DELETE -> this.handleDelete(matcher);
        case FIND -> this.handleFind(matcher);
        case CHEER -> this.handleCheer(matcher);
        case TAG -> this.handleTag(matcher);
        case UNTAG -> this.handleUntag(matcher);
        default -> "TODO: Tag not implemented.";
        };
    }

    /**
     * Handles the 'bye' command to exit the application.
     * Sets the isAlive flag to false if no extraneous arguments are provided.
     *
     * @param matcher The regex matcher containing captured groups from the command.
     * @return The response string for the bye command
     */
    private String handleBye(Matcher matcher) {
        String arg = matcher.group("arg");

        if (arg != null) {
            return this.formatIllegalArguments(
                    "bye",
                    "Command 'bye' does not accept any arguments.");
        }
        this.isAlive = false;
        return this.getFarewell();
    }

    /**
     * Handles the 'list' command to display all tasks.
     * Returns an error if extraneous arguments are provided.
     *
     * @param matcher The regex matcher containing captured groups from the command.
     * @return The response string for the list command
     */
    private String handleList(Matcher matcher) {
        String arg = matcher.group("arg");

        if (arg != null) {
            return this.formatIllegalArguments(
                    "list",
                    "Command 'list' does not accept any arguments.");
        }
        return "Task List:\n%s".formatted(this.taskList.toString());
    }

    /**
     * Handles the 'mark' command to mark a task as completed.
     * Expects a 1-based index argument specifying which task to mark.
     *
     * @param matcher The regex matcher containing the index captured group.
     * @return The response string for the mark command
     */
    private String handleMark(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "mark <index>";

        if (indexString == null) {
            return this.formatMissingArguments(
                    expectedFormatMessage, "Command 'mark' expects argument 'index'."
            );
        }
        indexString = indexString.strip();

        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            return "Marked:\n%s".formatted(this.taskList.mark(index).toString());
        } catch (IndexOutOfBoundsException | TaskIsMarkedException exception) {
            return this.formatDisallowed(expectedFormatMessage, exception.getMessage());
        } catch (NumberFormatException exception) {
            return this.formatIllegalArguments(
                    expectedFormatMessage,
                    "Command 'mark' expects argument 'index' to be a positive integer, got '%s'".formatted(indexString)
            );
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'unmark' command to unmark a task (mark as not completed).
     * Expects a 1-based index argument specifying which task to unmark.
     *
     * @param matcher The regex matcher containing the index captured group.
     * @return The response string for the unmark command
     */
    private String handleUnmark(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "unmark <index>";

        if (indexString == null) {
            return this.formatMissingArguments(
                    expectedFormatMessage, "Command 'unmark' expects argument 'index'."
            );
        }
        indexString = indexString.strip();

        try {
            Integer index = Integer.parseUnsignedInt(indexString);
            return "Unmarked:\n%s".formatted(this.taskList.unmark(index).toString());
        } catch (IndexOutOfBoundsException | TaskIsUnmarkedException exception) {
            return this.formatDisallowed(expectedFormatMessage, exception.getMessage());
        } catch (NumberFormatException exception) {
            return this.formatIllegalArguments(
                    expectedFormatMessage,
                    "Command 'unmark' expects argument 'index' to be a positive integer, got '%s'"
                            .formatted(indexString)
            );
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'todo' command to create a new Todo task.
     * Expects a name argument for the task description.
     *
     * @param matcher The regex matcher containing the name captured group.
     * @return The response string for the todo command
     */
    private String handleTodo(Matcher matcher) {
        String name = matcher.group("name");
        if (name == null) {
            return this.formatMissingArguments(
                    "todo <name>",
                    "Command 'todo' expects argument 'name'."
            );
        }
        name = name.strip();
        Todo todo = new Todo(name);
        try {
            this.taskList.add(todo);
            return "Todo added:\n%s".formatted(todo.toString());
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
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
     * @return The response string for the deadline command
     */
    private String handleDeadline(Matcher matcher) {
        String expectedFormatMessage = "deadline -by <year>-<month>-<day>[,<hour>:<minute>] <name>";
        String byField = matcher.group("byField");
        String by = matcher.group("by");
        String name = matcher.group("name");


        if (byField == null) {
            return formatMissingFlags(expectedFormatMessage, "Command 'deadline' expects flag '-by'");
        }
        if (by == null) {
            return formatIllegalFlags(
                    expectedFormatMessage,
                    "Command 'deadline' flag '-by' expects date and time in the specified format."
            );
        }
        if (name == null) {
            return formatMissingArguments(expectedFormatMessage, "Command 'deadline' expects argument 'name'");
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
        boolean hasByTime = (hourString != null && minuteString != null);

        try {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
            Deadline deadline = new Deadline(name, dateTime, hasByTime);
            this.taskList.add(deadline);
            return "Deadline added:\n%s".formatted(deadline.toString());
        } catch (DateTimeException exception) {
            return formatIllegalArguments(expectedFormatMessage, exception.getMessage());
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
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
     * @return The response string for the event command
     */
    private String handleEvent(Matcher matcher) {
        String expectedFormatMessage =
                """
                        event -from <year>-<month>-<day>[,<hour>:<minute>]
                                -to <year>-<month>-<day>[,<hour>:<minute>] <name>
                        """;

        String fromField = matcher.group("fromField");
        String from = matcher.group("from");
        String toField = matcher.group("toField");
        String to = matcher.group("to");
        String name = matcher.group("name");

        if (fromField == null) {
            return this.formatMissingFlags(expectedFormatMessage, "Command 'event' expects flag '-from'.");
        }
        if (toField == null) {
            return this.formatMissingFlags(expectedFormatMessage, "Command 'event' expects flag '-to'.");
        }
        if (from == null) {
            return this.formatIllegalFlags(
                    expectedFormatMessage,
                    "Command 'event' flag '-from' expects a valid date/time.");
        }
        if (to == null) {
            return this.formatIllegalFlags(
                    expectedFormatMessage,
                    "Command 'event' flag '-to' expects a valid date/time.");
        }
        if (name == null) {
            return this.formatMissingArguments(expectedFormatMessage, "Command 'event' expects argument 'name'.");
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
        boolean hasStartTime = (fromHourStr != null && fromMinStr != null);
        int toMin = (toMinStr == null) ? 59 : Integer.parseUnsignedInt(toMinStr);
        boolean hasEndTime = (toHourStr != null && toMinStr != null);

        try {
            LocalDateTime startDateTime = LocalDateTime.of(fromYear, fromMonth, fromDay, fromHour, fromMin);
            LocalDateTime endDateTime = LocalDateTime.of(toYear, toMonth, toDay, toHour, toMin);
            Event event = new Event(name.strip(), startDateTime, hasStartTime, endDateTime, hasEndTime);
            this.taskList.add(event);
            return "Event added:\n%s".formatted(event.toString());
        } catch (DateTimeException exception) {
            return formatIllegalArguments(expectedFormatMessage, exception.getMessage());
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occured: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Handles the 'delete' command to remove a task from the list.
     * Expects a 1-based index argument specifying which task to delete.
     *
     * @param matcher The regex matcher containing the index captured group.
     * @return The response string for the delete command
     */
    private String handleDelete(Matcher matcher) {
        String indexString = matcher.group("index");
        String expectedFormatMessage = "delete <index>";

        if (indexString == null) {
            return this.formatMissingArguments(
                    expectedFormatMessage, "Command 'index' expects argument 'index'."
            );
        }
        indexString = indexString.strip();

        try {
            int index = Integer.parseUnsignedInt(indexString);
            int sizeAfter = this.taskList.getSize() - 1;
            return "Deleted:\n%s\n%d %s remaining".formatted(
                            this.taskList.pop(index).toString(), sizeAfter, (sizeAfter > 1) ? "tasks" : "task"
                    );
        } catch (IndexOutOfBoundsException exception) {
            return this.formatDisallowed(expectedFormatMessage, exception.getMessage());
        } catch (NumberFormatException exception) {
            return this.formatIllegalArguments(
                    expectedFormatMessage,
                    "Command 'delete' expects argument 'index' to be a positive integer, got '%s'"
                            .formatted(indexString)
            );
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occurred: %s".formatted(exception.getMessage())
            );
        }
    }

    private String handleFind(Matcher matcher) {
        String keyword = matcher.group("keyword");
        String expectedFormatMessage = "find <keyword>";

        if (keyword == null) {
            return this.formatMissingArguments(
                    expectedFormatMessage, "Command 'find' expects argument 'keyword'."
            );
        }
        keyword = keyword.strip();
        List<Pair<Integer, Task>> foundTasks = this.taskList.findTasks(keyword);
        StringBuilder bobTheBuilder = new StringBuilder("Here are the matching tasks:\n");
        for (int i = 0; i < foundTasks.size(); i++) {
            Pair<Integer, Task> pair = foundTasks.get(i);
            bobTheBuilder.append("%d. %s\n".formatted(pair.getKey(), pair.getValue().toString()));
        }
        return bobTheBuilder.toString();
    }


    private String handleCheer(Matcher matcher) {
        String arg = matcher.group("arg");

        if (arg != null) {
            return this.formatIllegalArguments(
                    "cheer",
                    "Command 'cheer' does not accept any arguments.");
        }
        try {
            return this.cheerleader.cheer();
        } catch (IOException exception) {
            return this.formatInternalError(
                "An internal error occurred: %s".formatted(exception.getMessage())
            );
        }
    }


    private String handleTag(Matcher matcher) {
        String expectedFormatMessage = "tag -names <name1,name2,...> <index>";

        String nameFields = matcher.group("nameFields");
        String names = matcher.group("names");
        String indexString = matcher.group("index");

        if (nameFields == null) {
            return this.formatMissingFlags(expectedFormatMessage, "Command 'tag' expects flag '-names'.");
        }
        if (names == null) {
            return this.formatIllegalFlags(expectedFormatMessage,
                    "Command 'tag' flag '-names' expects at least one tag name.");
        }
        if (indexString == null) {
            return this.formatMissingArguments(expectedFormatMessage, "Command 'tag' expects argument 'index'.");
        }
        nameFields = nameFields.strip();
        indexString = indexString.strip();

        String[] tagNames = names.split("\\s*,\\s*");
        TaskTag[] taskTags = List.of(tagNames).stream().map(TaskTag::new).toArray(TaskTag[]::new);
        try {
            int index = Integer.parseUnsignedInt(indexString);
            Task task = this.taskList.addTagsToTask(index, taskTags);
            return "Tagged:\n%s".formatted(task.toString());
        } catch (IndexOutOfBoundsException exception) {
            return this.formatDisallowed(expectedFormatMessage, exception.getMessage());
        } catch (NumberFormatException exception) {
            return this.formatIllegalArguments(
                    expectedFormatMessage,
                    "Command 'tag' expects argument 'index' to be a positive integer, got '%s'"
                            .formatted(indexString)
            );
        } catch (TaskTagAlreadyExistsException exception) {
            return this.formatIllegalFlags(expectedFormatMessage, exception.getMessage());
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occurred: %s".formatted(exception.getMessage())
            );
        }
    }

    private String handleUntag(Matcher matcher) {
        String expectedFormatMessage = "untag -names <name1,name2,...> <index>";

        String nameFields = matcher.group("nameFields");
        String names = matcher.group("names");
        String indexString = matcher.group("index");

        if (nameFields == null) {
            return this.formatMissingFlags(expectedFormatMessage, "Command 'untag' expects flag '-names'.");
        }
        if (names == null) {
            return this.formatIllegalFlags(expectedFormatMessage,
                    "Command 'untag' flag '-names' expects at least one tag name.");
        }
        if (indexString == null) {
            return this.formatMissingArguments(expectedFormatMessage, "Command 'untag' expects argument 'index'.");
        }
        nameFields = nameFields.strip();
        indexString = indexString.strip();

        String[] tagNames = names.split("\\s*,\\s*");
        TaskTag[] taskTags = List.of(tagNames).stream().map(TaskTag::new).toArray(TaskTag[]::new);
        try {
            int index = Integer.parseUnsignedInt(indexString);
            Task task = this.taskList.removeTagsFromTask(index, taskTags);
            return "Untagged:\n%s".formatted(task.toString());
        } catch (IndexOutOfBoundsException exception) {
            return this.formatDisallowed(expectedFormatMessage, exception.getMessage());
        } catch (NumberFormatException exception) {
            return this.formatIllegalArguments(
                    expectedFormatMessage,
                    "Command 'untag' expects argument 'index' to be a positive integer, got '%s'"
                            .formatted(indexString)
            );
        } catch (TaskTagDoesNotExistException exception) {
            return this.formatIllegalFlags(expectedFormatMessage, exception.getMessage());
        } catch (IOException | ReflectiveOperationException | SecurityException exception) {
            return this.formatInternalError(
                    "An internal error occurred: %s".formatted(exception.getMessage())
            );
        }
    }

    /**
     * Formats an error message for illegal arguments.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     * @return The formatted error message
     */
    private String formatIllegalArguments(String expectedFormatMessage, String errorMessage) {
        return "EXPECTED FORMAT: %s\nILLEGAL ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage);
    }

    /**
     * Formats an error message for missing arguments.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     * @return The formatted error message
     */
    private String formatMissingArguments(String expectedFormatMessage, String errorMessage) {
        return "EXPECTED FORMAT: %s\nMISSING ARGUMENTS: %s".formatted(expectedFormatMessage, errorMessage);
    }

    /**
     * Formats an error message for disallowed operations.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     * @return The formatted error message
     */
    private String formatDisallowed(String expectedFormatMessage, String errorMessage) {
        return "EXPECTED FORMAT: %s\nDISALLOWED: %s".formatted(expectedFormatMessage, errorMessage);
    }

    /**
     * Formats an error message for missing command flags.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     * @return The formatted error message
     */
    private String formatMissingFlags(String expectedFormatMessage, String errorMessage) {
        return "EXPECTED FORMAT: %s\nMISSING FLAGS: %s".formatted(expectedFormatMessage, errorMessage);
    }

    /**
     * Formats an error message for illegal flag values.
     *
     * @param expectedFormatMessage The expected command format.
     * @param errorMessage          The specific error description.
     * @return The formatted error message
     */
    private String formatIllegalFlags(String expectedFormatMessage, String errorMessage) {
        return "EXPECTED FORMAT: %s\nILLEGAL FLAGS: %s".formatted(expectedFormatMessage, errorMessage);
    }

    /**
     * Formats an internal error message.
     *
     * @param errorMessage The error description.
     * @return The formatted error message
     */
    private String formatInternalError(String errorMessage) {
        return "INTERNAL ERROR: %s".formatted(errorMessage);
    }
}
