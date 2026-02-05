package app.task;

/**
 * Abstract base class representing a task in the Duke chatbot application.
 * Tasks have a name and a marked/unmarked status indicating completion.
 * Subclasses must implement serialization and provide a unique tag for persistence.
 *
 * <p>Concrete implementations include {@link Todo}, {@link Deadline}, and {@link Event}.</p>
 */
public abstract class Task {

    /** Delimiter used for serializing task fields to a string. */
    protected static final String delimiter = ",";

    /** The name/description of the task. */
    private final String name;

    /** Whether the task has been marked as completed. */
    private Boolean isMarked = false;

    /**
     * Constructs a new Task with the specified name.
     * The task is initially unmarked (not completed).
     *
     * @param name The name or description of the task.
     */
    public Task(String name) {
        this.name = name;
    }

    /**
     * Protected constructor for deserialization purposes.
     * Creates a Task with the specified name and marked status.
     *
     * @param name           The name or description of the task.
     * @param isMarkedString String representation of the marked status ("true" or "false").
     */
    protected Task(String name, String isMarkedString) {
        this.name = name;
        this.isMarked = Boolean.parseBoolean(isMarkedString);
    }

    /**
     * Returns a string representation of the task.
     * Format: "[X] name" if marked, "[ ] name" if unmarked.
     *
     * @return A string representation of the task.
     */
    @Override
    public String toString() {
        return "[%s] %s".formatted((this.isMarked ? "X" : " "), this.name);
    }

    /**
     * Serializes the task to a string format for persistent storage.
     * The serialization format is specific to each task type.
     *
     * @return A serialized string representation of the task.
     */
    public abstract String serialize();

    /**
     * Deserializes a string to create a Task object.
     * This method should not be called on the abstract Task class directly.
     *
     * @param serializedTask The serialized string representation of a task.
     * @return The deserialized Task object.
     * @throws UnsupportedOperationException Always thrown when called on Task class.
     */
    public static Task deserialize(String serializedTask) {
        throw new UnsupportedOperationException("Cannot call static method deserialize on Task class.");
    }

    /**
     * Returns the tag identifier for this task type.
     * This method should not be called on the abstract Task class directly.
     *
     * @return The tag string for this task type.
     * @throws UnsupportedOperationException Always thrown when called on Task class.
     */
    public static String getTag() {
        throw new UnsupportedOperationException("Cannot call static method getTag on Task class.");
    }

    /**
     * Marks the task as completed.
     */
    public void mark() {
        this.isMarked = true;
    }

    /**
     * Unmarks the task (marks as not completed).
     */
    public void unmark() {
        this.isMarked = false;
    }

    /**
     * Returns the name of the task.
     *
     * @return The task name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the task is marked as completed.
     *
     * @return {@code true} if the task is marked, {@code false} otherwise.
     */
    public Boolean getIsMarked() {
        return this.isMarked;
    }
}
