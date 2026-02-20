package bot.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     *
     * @param name The name of the tag (cannot be null or empty).
     */
    public TaskTag(String name) {
        assert name != null : "Task tag name cannot be null";
        name = name.strip();
        assert !name.isEmpty() : "Task tag name cannot be empty or whitespace only";
        this.name = name;
    }


    /**
     * Deserializes a string representation of task tags back to a list of TaskTag objects.
     *
     * @param taskTagsString The serialized string representation of task tags.
     * @return A list of TaskTag objects.
     */
    public static List<TaskTag> deserializeTaskTags(String taskTagsString) {
        return new ArrayList<TaskTag>(
            List.of(taskTagsString.split(DELIMITER))
            .stream()
            .filter(tagName -> !SENTINEL.equals(tagName))
            .map(taskTagName -> new TaskTag(taskTagName))
            .toList()
        );
    }


    /**
     * Serializes a list of TaskTag objects to a string representation for storage.
     *
     * @param taskTags The list of TaskTag objects to serialize.
     * @return A string representation of the task tags.
     */
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


    /**
     * Gets the name of this task tag.
     *
     * @return The name of the task tag.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Compares this TaskTag with another object for equality.
     * Two TaskTags are equal if they have the same name.
     *
     * @param obj The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TaskTag taskTag = (TaskTag) obj;
        return Objects.equals(name, taskTag.name);
    }

    /**
     * Returns the hash code for this TaskTag.
     *
     * @return The hash code based on the tag name.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
