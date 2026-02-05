package app.task;

public abstract class Task {
    protected static final String delimiter = ",";
    private final String name;
    private Boolean isMarked = false;

    public Task(String name) {
        this.name = name;
    }


    protected Task(String name, String isMarkedString) {
        this.name = name;
        this.isMarked = Boolean.parseBoolean(isMarkedString);
    }


    @Override
    public String toString() {
        return "[%s] %s".formatted((this.isMarked ? "X" : " "), this.name);
    }


    public abstract String serialize();


    public static Task deserialize(String serializedTask) {
        throw new UnsupportedOperationException("Cannot call static method deserialize on Task class.");
    }


    public static String getTag() {
        throw new UnsupportedOperationException("Cannot call static method getTag on Task class.");
    }


    public void mark() {
        this.isMarked = true;
    }


    public void unmark() {
        this.isMarked = false;
    }


    public String getName() {
        return name;
    }


    public Boolean getIsMarked() {
        return this.isMarked;
    }
}
