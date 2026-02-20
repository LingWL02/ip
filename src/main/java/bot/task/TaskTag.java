package bot.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tag that can be associated with a task.
 * Tags have a name and are displayed with a '#' prefix.
 */
public class TaskTag {
    private static final String DELIMITER = "<TASK_TAG_DELIMITER>";
    private static final String SENTINEL = "<NO_TAGS>";
    private String name;


    /**
     * Creates a new TaskTag with the specified name.
     * The name is trimmed of leading and trailing whitespace.
     *`
     * @param name The name of the tag (cannot be null or empty).
     */
    public TaskTag(String name) {
        assert name != null : "Task tag name cannot be null";
        name = name.strip();
        assert !name.isEmpty() : "Task tag name cannot be empty or whitespace only";
        this.name = name;
    }


    public static List<TaskTag> deserializeTaskTags(String taskTagsString) {
        return new ArrayList<TaskTag>(
            List.of(taskTagsString.split(DELIMITER))
            .stream()
            .filter(tagName -> !SENTINEL.equals(tagName))
            .map(taskTagName -> new TaskTag(taskTagName))
            .toList()
        );
    }


    public static String serializeTaskTags(List<TaskTag> taskTags) {
        return taskTags.stream()
        .map(taskTag -> taskTag.getName())
        .reduce((acc, taskTagStr) -> acc + DELIMITER + taskTagStr)
        .orElse(SENTINEL);
    }


    @Override
    public String toString() {
        return "#" + this.name;
    }


    public String getName() {
        return this.name;
    }
}
