package bot.task;

/**
 * Exception thrown when attempting to remove a task tag that does not exist on a task.
 */
public class TaskTagDoesNotExistException extends Exception {

    /**
     * Constructs a new TaskTagDoesNotExistException with the specified detail message.
     *
     * @param message The detail message explaining which tag does not exist.
     */
    public TaskTagDoesNotExistException(String message) {
        super(message);
    }

    /**
     * Constructs a new TaskTagDoesNotExistException with the specified detail message and cause.
     *
     * @param message The detail message explaining which tag does not exist.
     * @param cause   The underlying throwable that caused this exception.
     */
    public TaskTagDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}