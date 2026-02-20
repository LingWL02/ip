package bot.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a Deadline task in the Duke chatbot application.
 * A Deadline is a task with a name and a due date/time by which it should be completed.
 *
 * <p>Serialization format: "D,name,isMarked,byDateTime,hasByTime"</p>
 */
public class Deadline extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "D";

    /** The date and time by which this deadline should be completed. */
    private LocalDateTime byDateTime;
    private Boolean hasByTime = true;

    /**
     * Constructs a new Deadline task with the specified name and due date/time.
     * The task is initially unmarked (not completed).
     *
     * @param name The name or description of the deadline task.
     * @param byDateTime   The date and time by which the task should be completed.
     */
    public Deadline(String name, LocalDateTime byDateTime) {
        super(name);
        assert byDateTime != null : "Deadline date cannot be null";
        this.byDateTime = byDateTime;
    }


    /**
     * Constructs a Deadline with the specified name, deadline, and time inclusion preference.
     *
     * @param name the name of the task
     * @param byDateTime the deadline date and time
     * @param hasByTime whether to include time in the deadline display
     */
    public Deadline(String name, LocalDateTime byDateTime, Boolean hasByTime) {
        super(name);
        assert byDateTime != null : "Deadline date cannot be null";
        assert hasByTime != null : "Include time flag cannot be null";
        this.byDateTime = byDateTime;
        this.hasByTime = hasByTime;
    }


    /**
     * Private constructor for deserialization purposes.
     * Creates a Deadline with the specified name, marked status, and due date.
     *
     * @param name              The name or description of the deadline task.
     * @param isMarkedString    String representation of the marked status ("true" or "false").
     * @param byDateTimeString          String representation of the due date/time in ISO-8601 format.
     * @param hasByTimeString String representation of whether to include time ("true" or "false").
     */
    private Deadline(String name, String isMarkedString, String byDateTimeString, String hasByTimeString) {
        super(name, isMarkedString);
        this.byDateTime = LocalDateTime.parse(byDateTimeString);
        this.hasByTime = Boolean.parseBoolean(hasByTimeString);
    }

    /**
     * Serializes this Deadline task to a string format for persistent storage.
     *
     * @return A serialized string in the format "D,name,isMarked,byDateTime,hasByTime".
     */
    @Override
    public String serialize() {
        return (
            getTag()
            + DELIMITER
            + this.getName()
            + DELIMITER
            + this.getIsMarked().toString()
            + DELIMITER
            + this.byDateTime.toString()
            + DELIMITER
            + this.hasByTime.toString()
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
        String[] serializedParts = serializedTask.split(DELIMITER);
        if (serializedParts.length != 5) {
            throw new RuntimeException(); // TODO
        }
        if (!serializedParts[0].equals(tag)) {
            throw new RuntimeException(); // TODO
        }
        return new Deadline(
            serializedParts[1],
            serializedParts[2],
            serializedParts[3],
            serializedParts[4]
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
        return "[%s] %s (by %s)".formatted(getTag(), super.toString(), this.getByString());
    }

    /**
     * Returns the due date/time as a formatted string.
     * If hasByTime is true, returns "MMM dd yyyy, HH:mm" format.
     * Otherwise, returns "MMM dd yyyy" format.
     *
     * @return A formatted string representation of the due date/time.
     */
    public String getByString() {
        if (hasByTime) {
            return this.byDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.byDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }
}
