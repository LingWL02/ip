package app.task;


public class Todo extends Task {
    private static final String tag = "T";

    public Todo(String name) {
        super(name);
    }


    private Todo(String name, String isMarkedString) {
        super(name, isMarkedString);
    }


    @Override
    public String serialize() {
        return (
            getTag() + delimiter +
            this.getName() + delimiter +
            this.getIsMarked().toString()
        );
    }


    public static Task deserialize(String serializedTask) {
        String[] serializedParts = serializedTask.split(delimiter);
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


    public static String getTag() {
        return tag;
    }


    @Override
    public String toString() {
        return "[%s] %s".formatted(getTag(), super.toString());
    }
}
