package bot.task;


public class TaskTag {
    public String name;

    public TaskTag(String name) {
        assert name != null : "Task tag name cannot be null";
        name = name.strip();
        assert !name.isEmpty() : "Task tag name cannot be empty or whitespace only";
        this.name = name;
    }


    @Override
    public String toString() {
        return "#" + this.name;
    }


    public String getName() {
        return this.name;
    }
}
