package app.task;

/**
 * Exception thrown when attempting to mark a task that is already marked as completed.
 * This exception is used to prevent redundant mark operations and inform the user
 * that the requested action is not necessary.
 */
public class TaskIsMarkedException extends Exception {

    /**
     * Constructs a new TaskIsMarkedException with the specified detail message.
     *
     * @param message The detail message explaining which task was already marked.
     */
    public TaskIsMarkedException(String message) {
        super(message);
    }
}
