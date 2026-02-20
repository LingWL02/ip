package bot.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an Event task in the Duke chatbot application.
 * An Event is a task with a name, a start date/time, and an end date/time.
 *
 * <p>Serialization format: "E,name,isMarked,start,hasStartTime,end,hasEndTime"</p>
 */
public class Event extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "E";

    /** The start date and time of the event. */
    private LocalDateTime start;
    private Boolean hasStartTime = true;

    /** The end date and time of the event. */
    private LocalDateTime end;
    private Boolean hasEndTime = true;

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
        assert start != null : "Event start time cannot be null";
        assert end != null : "Event end time cannot be null";
        assert start.isBefore(end) || start.isEqual(end) : "Event start time must be before or equal to end time";
        this.start = start;
        this.end = end;
    }

    /**
     * Constructs an Event with detailed time inclusion settings.
     *
     * @param name the name of the event
     * @param start the start date and time
     * @param hasStartTime whether to include time in the start display
     * @param end the end date and time
     * @param hasEndTime whether to include time in the end display
     */
    public Event(
        String name, LocalDateTime start, Boolean hasStartTime,
        LocalDateTime end, Boolean hasEndTime
    ) {
        super(name);
        assert start != null : "Event start time cannot be null";
        assert end != null : "Event end time cannot be null";
        assert hasStartTime != null : "Include start time flag cannot be null";
        assert hasEndTime != null : "Include end time flag cannot be null";
        assert start.isBefore(end) || start.isEqual(end) : "Event start time must be before or equal to end time";
        this.start = start;
        this.hasStartTime = hasStartTime;
        this.end = end;
        this.hasEndTime = hasEndTime;
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates an Event with the specified name, marked status, start time, and end time.
     *
     * @param name                   The name or description of the event.
     * @param isMarkedString         String representation of the marked status ("true" or "false").
     * @param startString            String representation of the start date/time in ISO-8601 format.
     * @param hasStartTimeString String representation of whether to include start time ("true" or "false").
     * @param endString              String representation of the end date/time in ISO-8601 format.
     * @param hasEndTimeString   String representation of whether to include end time ("true" or "false").
     */
    private Event(
        String name, String isMarkedString,
        String startString, String hasStartTimeString,
        String endString, String hasEndTimeString
    ) {
        super(name, isMarkedString);
        this.start = LocalDateTime.parse(startString);
        this.hasStartTime = Boolean.parseBoolean(hasStartTimeString);
        this.end = LocalDateTime.parse(endString);
        this.hasEndTime = Boolean.parseBoolean(hasEndTimeString);
    }

    /**
     * Serializes this Event task to a string format for persistent storage.
     *
     * @return A serialized string in the format "E,name,isMarked,start,hasStartTime,end,hasEndTime".
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
            + this.start.toString()
            + DELIMITER
            + this.hasStartTime.toString()
            + DELIMITER
            + this.end.toString()
            + DELIMITER
            + this.hasEndTime.toString()
            );
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
        String[] serializedParts = serializedTask.split(DELIMITER);
        if (serializedParts.length != 7) {
            throw new RuntimeException(); // TODO
        }
        if (!serializedParts[0].equals(tag)) {
            throw new RuntimeException(); // TODO
        }
        return new Event(
            serializedParts[1],
            serializedParts[2],
            serializedParts[3],
            serializedParts[4],
            serializedParts[5],
            serializedParts[6]
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
        return "[%s] %s (start: %s | end: %s)".formatted(
            getTag(), super.toString(), this.getStartString(), this.getEndString()
        );
    }


    public String getStartString() {
        if (hasStartTime) {
            return this.start.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.start.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }

    /**
     * Returns the end date/time as a formatted string.
     * If hasEndTime is true, returns "MMM dd yyyy, HH:mm" format.
     * Otherwise, returns "MMM dd yyyy" format.
     *
     * @return A formatted string representation of the end date/time.
     */
    public String getEndString() {
        if (hasEndTime) {
            return this.end.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.end.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }
}
