package app.task;

public class Task {
    private final String name;
    private Boolean isMarked = false;

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, Boolean marked) {
        this.name = name;
        this.isMarked = marked;
    }

    public String toString() {
        return "[%s] %s".formatted((this.isMarked ? "X" : " "), this.name);
    }

    public void mark() {this.isMarked = true;}
    public void unmark() {this.isMarked = false;}

    public String getName() {
        return name;
    }

    public boolean getIsMarked() {
        return this.isMarked;
    }
}
