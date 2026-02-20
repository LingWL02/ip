package bot.task;

/**
 * Represents a Todo task in the Duke chatbot application.
 * A Todo is a simple task with only a name and no associated date/time.
 *
 * <p>Serialization format: "T,name,isMarked"</p>
 */
public class Todo extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "T";

    /**
     * Constructs a new Todo task with the specified name.
     * The task is initially unmarked (not completed).
     *
     * @param name The name or description of the todo task.
     */
    public Todo(String name) {
        super(name);
        assert name != null : "Todo name cannot be null";
        assert !name.trim().isEmpty() : "Todo name cannot be empty";
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates a Todo with the specified name and marked status.
     *
     * @param name           The name or description of the todo task.
     * @param isMarkedString String representation of the marked status ("true" or "false").
     */
    private Todo(String name, String isMarkedString) {
        super(name, isMarkedString);
    }

    /**
     * Serializes this Todo task to a string format for persistent storage.
     *
     * @return A serialized string in the format "T,name,isMarked".
     */
    @Override
    public String serialize() {
        return (
            getTag()
            + DELIMITER
            + this.getName()
            + DELIMITER
            + this.getIsMarked().toString()
            );
    }

    /**
     * Deserializes a string to create a Todo object.
     *
     * @param serializedTask The serialized string representation of a Todo task.
     * @return A new Todo object with the deserialized data.
     * @throws RuntimeException If the serialized string format is invalid or
     *                          the tag does not match the expected Todo tag.
     */
    public static Task deserialize(String serializedTask) {
        String[] serializedParts = serializedTask.split(DELIMITER);
        if (serializedParts.length != 3) {
            throw new RuntimeException(); // TODO
        }
        if (!serializedParts[0].equals(tag)) {
            throw new RuntimeException(); // TODO
        }
        return new Todo(
            serializedParts[1],
            serializedParts[2]
        );
    }

    /**
     * Returns the tag identifier for the Todo task type.
     *
     * @return The tag string "T".
     */
    public static String getTag() {
        return tag;
    }

    /**
     * Returns a string representation of this Todo task.
     * Format: "[T] [X/space] name"
     *
     * @return A formatted string representation of the Todo.
     */
    @Override
    public String toString() {
        return "[%s] %s".formatted(getTag(), super.toString());
    }
}
