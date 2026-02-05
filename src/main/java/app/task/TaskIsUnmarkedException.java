package app.task;

/**
 * Exception thrown when attempting to unmark a task that is already unmarked (not completed).
 * This exception is used to prevent redundant unmark operations and inform the user
 * that the requested action is not necessary.
 */
public class TaskIsUnmarkedException extends Exception {

    /**
     * Constructs a new TaskIsUnmarkedException with the specified detail message.
     *
     * @param message The detail message explaining which task was already unmarked.
     */
    public TaskIsUnmarkedException(String message) {
        super(message);
    }
}
