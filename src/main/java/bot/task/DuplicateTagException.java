package bot.task;

/**
 * Exception thrown when attempting to register a task class with a serialization tag
 * that already exists in the TaskList's deserialization mappings.
 * This prevents duplicate tag registrations which could cause ambiguous deserialization behavior.
 */
public class DuplicateTagException extends Exception {

    /**
     * Constructs a new DuplicateTagException with the specified detail message.
     *
     * @param message The detail message explaining which tag was duplicated.
     */
    public DuplicateTagException(String message) {
        super(message);
    }
}
