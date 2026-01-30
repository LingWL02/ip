package app.task;


import java.time.LocalDateTime;

public class Deadline extends Task {

    LocalDateTime by;

    public Deadline (String name, LocalDateTime by) {
        super(name);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D] %s (by %s)".formatted(super.toString(), this.by);
    }

}
