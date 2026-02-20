package bot.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an Event task in the Duke chatbot application.
 * An Event is a task with a name, a start date/time, and an end date/time.
 *
 * <p>Serialization format: "E,name,isMarked,fromDateTime,hasStartTime,toDateTime,hasEndTime"</p>
 */
public class Event extends Task {

    /** The tag identifier used for serialization and deserialization. */
    private static final String TAG = "E";

    /** The start date and time of the event. */
    private LocalDateTime fromDateTime;
    private Boolean hasStartTime = true;

    /** The end date and time of the event. */
    private LocalDateTime toDateTime;
    private Boolean hasEndTime = true;

    /**
     * Constructs a new Event task with the specified name, start time, and end time.
     * The task is initially unmarked (not completed).
     *
     * @param name  The name or description of the event.
     * @param fromDateTime The start date and time of the event.
     * @param toDateTime   The end date and time of the event.
     */
    public Event(String name, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        super(name);
        assert fromDateTime != null : "Event start time cannot be null";
        assert toDateTime != null : "Event end time cannot be null";
        assert fromDateTime.isBefore(toDateTime) || fromDateTime.isEqual(toDateTime)
                : "Event start time must be before or equal to end time";
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
    }

    /**
     * Constructs an Event with detailed time inclusion settings.
     *
     * @param name the name of the event
     * @param fromDateTime the start date and time
     * @param hasStartTime whether to include time in the start display
     * @param toDateTime the end date and time
     * @param hasEndTime whether to include time in the end display
     */
    public Event(
        String name, LocalDateTime fromDateTime, Boolean hasStartTime,
        LocalDateTime toDateTime, Boolean hasEndTime
    ) {
        super(name);
        assert fromDateTime != null : "Event start time cannot be null";
        assert toDateTime != null : "Event end time cannot be null";
        assert hasStartTime != null : "Include start time flag cannot be null";
        assert hasEndTime != null : "Include end time flag cannot be null";
        assert fromDateTime.isBefore(toDateTime) || fromDateTime.isEqual(toDateTime)
                : "Event start time must be before or equal to end time";
        this.fromDateTime = fromDateTime;
        this.hasStartTime = hasStartTime;
        this.toDateTime = toDateTime;
        this.hasEndTime = hasEndTime;
    }

    /**
     * Private constructor for deserialization purposes.
     * Creates an Event with the specified name, marked status, start time, and end time.
     *
     * @param name                   The name or description of the event.
     * @param isMarkedString         String representation of the marked status ("true" or "false").
     * @param fromDateTimeString            String representation of the start date/time in ISO-8601 format.
     * @param hasStartTimeString String representation of whether to include start time ("true" or "false").
     * @param toDateTimeString              String representation of the end date/time in ISO-8601 format.
     * @param hasEndTimeString   String representation of whether to include end time ("true" or "false").
     */
    private Event(
        String name, String isMarkedString,
        String fromDateTimeString, String hasStartTimeString,
        String toDateTimeString, String hasEndTimeString, String taskTagsString
    ) {
        super(name, isMarkedString, taskTagsString);
        this.fromDateTime = LocalDateTime.parse(fromDateTimeString);
        this.hasStartTime = Boolean.parseBoolean(hasStartTimeString);
        this.toDateTime = LocalDateTime.parse(toDateTimeString);
        this.hasEndTime = Boolean.parseBoolean(hasEndTimeString);
    }

    /**
     * Serializes this Event task to a string format for persistent storage.
     *
     * @return A serialized string in the format "E,name,isMarked,fromDateTime,hasStartTime,toDateTime,hasEndTime".
     */
    @Override
    public String serialize() {
        return (
            this.getName()
            + DELIMITER
            + this.getIsMarked().toString()
            + DELIMITER
            + this.fromDateTime.toString()
            + DELIMITER
            + this.hasStartTime.toString()
            + DELIMITER
            + this.toDateTime.toString()
            + DELIMITER
            + this.hasEndTime.toString()
            + DELIMITER
            + TaskTag.serializeTaskTags(this.getTaskTags())
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
        assert serializedParts.length == 7 : "Serialized Event must have 7 parts";
        return new Event(
            serializedParts[0],
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
        return TAG;
    }

    /**
     * Returns a string representation of this Event task.
     * Format: "[E] [X/space] name (start: startDateTime, end: endDateTime)"
     *
     * @return A formatted string representation of the Event.
     */
    @Override
    public String toString() {
        return "[%s] %s (start: %s | end: %s) %s".formatted(
            getTag(), super.toString(), this.getStartString(), this.getEndString(), this.getTaskTagsString()
        ).strip();
    }


    public String getStartString() {
        if (hasStartTime) {
            return this.fromDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.fromDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
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
            return this.toDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        } else {
            return this.toDateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
        }
    }
}
