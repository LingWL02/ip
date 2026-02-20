package bot.task;

/**
 * Represents a tag that can be associated with a task.
 * Tags have a name and are displayed with a '#' prefix.
 */
public class TaskTag {
    private String name;

    /**
     * Creates a new TaskTag with the specified name.
     * The name is trimmed of leading and trailing whitespace.
     *
     * @param name The name of the tag (cannot be null or empty).
     */
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
