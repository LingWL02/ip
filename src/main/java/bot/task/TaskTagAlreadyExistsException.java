package bot.task;

/**
 * Exception thrown when attempting to add a task tag that already exists on a task.
 */
public class TaskTagAlreadyExistsException extends Exception {

    /**
     * Constructs a new TaskTagAlreadyExistsException with the specified detail message.
     *
     * @param message The detail message explaining which tag already exists.
     */
    public TaskTagAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new TaskTagAlreadyExistsException with the specified detail message and cause.
     *
     * @param message The detail message explaining which tag already exists.
     * @param cause   The underlying throwable that caused this exception.
     */
    public TaskTagAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}