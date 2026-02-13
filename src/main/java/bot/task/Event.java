package bot.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an Event task in the Duke chatbot application.
 * An Event is a task with a name, a start date/time, and an end date/time.
 *
 * <p>Serialization format: "E,name,isMarked,start,includeStartTime,end,includeEndTime"</p>
 */
public class Event extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String tag = "E";

    /** The start date and time of the event. */
    private LocalDateTime start;
    private Boolean includeStartTime = true;

    /** The end date and time of the event. */
    private LocalDateTime end;
    private Boolean includeEndTime = true;

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
     * Constructs an Event with detailed time inclusion settings.
     *
     * @param name the name of the event
     * @param start the start date and time
     * @param includeStartTime whether to include time in the start display
     * @param end the end date and time
     * @param includeEndTime whether to include time in the end display
     */
    public Event(
        String name, LocalDateTime start, Boolean includeStartTime,
        LocalDateTime end, Boolean includeEndTime
    ) {
        super(name);
        this.start = start;
        this.includeStartTime = includeStartTime;
        this.end = end;
        this.includeEndTime = includeEndTime;
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates an Event with the specified name, marked status, start time, and end time.
     *
     * @param name                   The name or description of the event.
     * @param isMarkedString         String representation of the marked status ("true" or "false").
     * @param startString            String representation of the start date/time in ISO-8601 format.
     * @param includeStartTimeString String representation of whether to include start time ("true" or "false").
     * @param endString              String representation of the end date/time in ISO-8601 format.
     * @param includeEndTimeString   String representation of whether to include end time ("true" or "false").
     */
    private Event(
        String name, String isMarkedString,
        String startString, String includeStartTimeString,
        String endString, String includeEndTimeString
    ) {
        super(name, isMarkedString);
        this.start = LocalDateTime.parse(startString);
        this.includeStartTime = Boolean.parseBoolean(includeStartTimeString);
        this.end = LocalDateTime.parse(endString);
        this.includeEndTime = Boolean.parseBoolean(includeEndTimeString);
    }

    /**
     * Serializes this Event task to a string format for persistent storage.
     *
     * @return A serialized string in the format "E,name,isMarked,start,includeStartTime,end,includeEndTime".
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
            + this.includeStartTime.toString()
            + DELIMITER
            + this.end.toString()
            + DELIMITER
            + this.includeEndTime.toString()
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
        if (includeStartTime) {
            return this.start.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.start.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }

    /**
     * Returns the end date/time as a formatted string.
     * If includeEndTime is true, returns "MMM dd yyyy, HH:mm" format.
     * Otherwise, returns "MMM dd yyyy" format.
     *
     * @return A formatted string representation of the end date/time.
     */
    public String getEndString() {
        if (includeEndTime) {
            return this.end.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.end.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }
}
