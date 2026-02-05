package app.task;


import java.time.LocalDateTime;


public class Deadline extends Task {
    private static final String tag = "D";
    private LocalDateTime by;

    public Deadline (String name, LocalDateTime by) {
        super(name);
        this.by = by;
    }

    private Deadline(String name, String isMarkedString, String byString) {
        super(name, isMarkedString);
        this.by = LocalDateTime.parse(byString);
    }

    @Override
    public String serialize() {
        return super.serialize() + delimiter + this.by.toString();
    }

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


    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "%s (by %s)".formatted(super.toString(), this.by);
    }

}
