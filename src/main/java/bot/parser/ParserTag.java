package bot.parser;

/**
 * Enumeration representing the different command types that the parser can recognize.
 * Each tag corresponds to a specific user command in the Duke chatbot application.
 */
public enum ParserTag {

    /** Command to exit the application. */
    BYE,

    /** Command to list all tasks. */
    LIST,

    /** Command to mark a task as completed. */
    MARK,

    /** Command to unmark a task (mark as not completed). */
    UNMARK,

    /** Command to add a new Todo task. */
    TODO,

    /** Command to add a new Deadline task. */
    DEADLINE,

    /** Command to add a new Event task. */
    EVENT,

    /** Command to delete a task. */
    DELETE,

    FIND,

    CHEER
}
