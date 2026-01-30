package app.task;

import java.time.LocalDateTime;

public class Event extends Task {
    LocalDateTime start;
    LocalDateTime end;

    public Event(String name, LocalDateTime start, LocalDateTime end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "[E] %s (start: %s, end: %s)".formatted(
            super.toString(), this.start.toString(), this.end.toString()
        );
    }
}
