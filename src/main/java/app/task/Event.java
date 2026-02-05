package app.task;

import java.time.LocalDateTime;

public class Event extends Task {
    private static final String tag = "E";
    private LocalDateTime start;
    private LocalDateTime end;

    public Event(String name, LocalDateTime start, LocalDateTime end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    private Event(String name, String isMarkedString, String startString, String endString) {
        super(name, isMarkedString);
        this.start = LocalDateTime.parse(startString);
        this.end = LocalDateTime.parse(endString);
    }


    @Override
    public String serialize() {
        return (
            getTag() + delimiter +
            this.getName() + delimiter +
            this.getIsMarked().toString() + delimiter +
            this.start.toString() + delimiter +
            this.end.toString());
    }


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


    public static String getTag() {
        return tag;
    }


    @Override
    public String toString() {
        return "[%s] %s (start: %s, end: %s)".formatted(
            getTag(), super.toString(), this.start.toString(), this.end.toString()
        );
    }
}
