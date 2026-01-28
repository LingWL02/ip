package app.task;


public class Todo extends Task {
    public Todo(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "[T] %s".formatted(super.toString());
    }
}
