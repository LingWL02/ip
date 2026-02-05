package app.task;

import java.time.LocalDateTime;

/**
 * Represents an Event task in the Duke chatbot application.
 * An Event is a task with a name, a start date/time, and an end date/time.
 *
 * <p>Serialization format: "E,name,isMarked,startDateTime,endDateTime"</p>
 */
public class Event extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "E";

    /** The start date and time of the event. */
    private LocalDateTime start;

    /** The end date and time of the event. */
    private LocalDateTime end;

    /**
     * Constructs a new Event task with the specified name, start time, and end time.
     * The task is initially unmarked (not completed).
     *
     * @param name  The name or description of the event.
     * @param start The start date and time of the event.
     * @param end   The end date and time of the event.
     */
    public Event(String name, LocalDateTime start, LocalDateTime end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates an Event with the specified name, marked status, start time, and end time.
     *
     * @param name           The name or description of the event.
     * @param isMarkedString String representation of the marked status ("true" or "false").
     * @param startString    String representation of the start date/time in ISO-8601 format.
     * @param endString      String representation of the end date/time in ISO-8601 format.
     */
    private Event(String name, String isMarkedString, String startString, String endString) {
        super(name, isMarkedString);
        this.start = LocalDateTime.parse(startString);
        this.end = LocalDateTime.parse(endString);
    }

    /**
     * Serializes this Event task to a string format for persistent storage.
     *
     * @return A serialized string in the format "E,name,isMarked,startDateTime,endDateTime".
     */
    @Override
    public String serialize() {
        return (
            getTag() + delimiter +
            this.getName() + delimiter +
            this.getIsMarked().toString() + delimiter +
            this.start.toString() + delimiter +
            this.end.toString());
    }

    /**
     * Deserializes a string to create an Event object.
     *
     * @param serializedTask The serialized string representation of an Event task.
     * @return A new Event object with the deserialized data.
     * @throws RuntimeException If the serialized string format is invalid or
     *                          the tag does not match the expected Event tag.
     */
    public static Task deserialize(String serializedTask) {
        String[] serializedParts = serializedTask.split(delimiter);
        if (serializedParts.length != 5) {
            throw new RuntimeException(); // TODO
        }
        if (!serializedParts[0].equals(tag)) {
            throw new RuntimeException(); // TODO
        }
        return new Event(
            serializedParts[1],
            serializedParts[2],
            serializedParts[3],
            serializedParts[4]
        );
    }

    /**
     * Returns the tag identifier for the Event task type.
     *
     * @return The tag string "E".
     */
    public static String getTag() {
        return tag;
    }

    /**
     * Returns a string representation of this Event task.
     * Format: "[E] [X/space] name (start: startDateTime, end: endDateTime)"
     *
     * @return A formatted string representation of the Event.
     */
    @Override
    public String toString() {
        return "[%s] %s (start: %s, end: %s)".formatted(
            getTag(), super.toString(), this.start.toString(), this.end.toString()
        );
    }
}
