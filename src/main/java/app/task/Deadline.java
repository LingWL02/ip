package app.task;

import java.time.LocalDateTime;

/**
 * Represents a Deadline task in the Duke chatbot application.
 * A Deadline is a task with a name and a due date/time by which it should be completed.
 *
 * <p>Serialization format: "D,name,isMarked,byDateTime"</p>
 */
public class Deadline extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "D";

    /** The date and time by which this deadline should be completed. */
    private LocalDateTime by;

    /**
     * Constructs a new Deadline task with the specified name and due date/time.
     * The task is initially unmarked (not completed).
     *
     * @param name The name or description of the deadline task.
     * @param by   The date and time by which the task should be completed.
     */
    public Deadline(String name, LocalDateTime by) {
        super(name);
        this.by = by;
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates a Deadline with the specified name, marked status, and due date.
     *
     * @param name           The name or description of the deadline task.
     * @param isMarkedString String representation of the marked status ("true" or "false").
     * @param byString       String representation of the due date/time in ISO-8601 format.
     */
    private Deadline(String name, String isMarkedString, String byString) {
        super(name, isMarkedString);
        this.by = LocalDateTime.parse(byString);
    }

    /**
     * Serializes this Deadline task to a string format for persistent storage.
     *
     * @return A serialized string in the format "D,name,isMarked,byDateTime".
     */
    @Override
    public String serialize() {
        return (
            getTag() + delimiter +
            this.getName() + delimiter +
            this.getIsMarked().toString() + delimiter +
            this.by.toString()
        );
    }

    /**
     * Deserializes a string to create a Deadline object.
     *
     * @param serializedTask The serialized string representation of a Deadline task.
     * @return A new Deadline object with the deserialized data.
     * @throws RuntimeException If the serialized string format is invalid or
     *                          the tag does not match the expected Deadline tag.
     */
    public static Deadline deserialize(String serializedTask) {
        String[] serializedParts = serializedTask.split(delimiter);
        if (serializedParts.length != 4) {
            throw new RuntimeException(); // TODO
        }
        if (!serializedParts[0].equals(tag)) {
            throw new RuntimeException(); // TODO
        }
        return new Deadline(
            serializedParts[1],
            serializedParts[2],
            serializedParts[3]
        );
    }

    /**
     * Returns the tag identifier for the Deadline task type.
     *
     * @return The tag string "D".
     */
    public static String getTag() {
        return tag;
    }

    /**
     * Returns a string representation of this Deadline task.
     * Format: "[D] [X/space] name (by dateTime)"
     *
     * @return A formatted string representation of the Deadline.
     */
    @Override
    public String toString() {
        return "[%s] %s (by %s)".formatted(getTag(), super.toString(), this.by);
    }

}
