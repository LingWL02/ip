package bot.task;

/**
 * Exception thrown when attempting to add a task that is an exact duplicate of an existing task.
 * Two tasks are considered duplicates when they are of the same type and have identical
 * content (name, dates, times), regardless of their marked/unmarked status.
 */
public class DuplicateTaskException extends Exception {

    /**
     * Constructs a new DuplicateTaskException with the specified detail message.
     *
     * @param message The detail message describing which task was duplicated.
     */
    public DuplicateTaskException(String message) {
        super(message);
    }
}
