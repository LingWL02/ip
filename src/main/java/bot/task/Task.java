package bot.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class representing a task in the Duke chatbot application.
 * Tasks have a name and a marked/unmarked status indicating completion.
 * Subclasses must implement serialization and provide a unique tag for persistence.
 *
 * <p>Concrete implementations include {@link Todo}, {@link Deadline}, and {@link Event}.</p>
 */
public abstract class Task {

    /** Delimiter used for serializing task fields to a string. */
    protected static final String DELIMITER = "<SERIALIZATION_DELIMITER>";

    /** The name/description of the task. */
    private final String name;

    /** Whether the task has been marked as completed. */
    private Boolean isMarked = false;

    private final List<TaskTag> taskTags;

    /**
     * Constructs a new Task with the specified name.
     * The task is initially unmarked (not completed).
     *
     * @param name The name or description of the task.
     */
    public Task(String name) {
        assert name != null : "Task name cannot be null";
        assert !name.trim().isEmpty() : "Task name cannot be empty or whitespace only";
        this.name = name;
        this.taskTags = new ArrayList<TaskTag>();
    }

    protected Task(String name, String isMarkedString, String taskTagsString) {
        assert name != null : "Task name cannot be null";
        assert !name.trim().isEmpty() : "Task name cannot be empty or whitespace only";
        assert isMarkedString != null : "IsMarked string cannot be null";
        assert taskTagsString != null : "Task tags string cannot be null";
        this.name = name;
        this.isMarked = Boolean.parseBoolean(isMarkedString);
        this.taskTags = TaskTag.deserializeTaskTags(taskTagsString);
    }

    /**
     * Returns a string representation of the task.
     * Format: "[X] name" if marked, "[ ] name" if unmarked.
     *
     * @return A string representation of the task.
     */
    @Override
    public String toString() {
        return "[%s] %s".formatted((this.isMarked ? "X" : " "), this.name);
    }

    /**
     * Serializes the task to a string format for persistent storage.
     * The serialization format is specific to each task type.
     *
     * @return A serialized string representation of the task.
     */
    public abstract String serialize();

    /**
     * Deserializes a string to create a Task object.
     * This method should not be called on the abstract Task class directly.
     *
     * @param serializedTask The serialized string representation of a task.
     * @return The deserialized Task object.
     * @throws UnsupportedOperationException Always thrown when called on Task class.
     */
    public static Task deserialize(String serializedTask) {
        throw new UnsupportedOperationException("Cannot call static method deserialize on Task class.");
    }

    /**
     * Returns the tag identifier for this task type.
     * This method should not be called on the abstract Task class directly.
     *
     * @return The tag string for this task type.
     * @throws UnsupportedOperationException Always thrown when called on Task class.
     */
    public static String getTag() {
        throw new UnsupportedOperationException("Cannot call static method getTag on Task class.");
    }

    /**
     * Marks the task as completed.
     */
    public void mark() {
        this.isMarked = true;
    }

    /**
     * Unmarks the task (marks as not completed).
     */
    public void unmark() {
        this.isMarked = false;
    }

    /**
     * Returns the name of the task.
     *
     * @return The task name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the task is marked as completed.
     *
     * @return {@code true} if the task is marked, {@code false} otherwise.
     */
    public Boolean getIsMarked() {
        return this.isMarked;
    }

    public List<TaskTag> getTaskTags() {
        return this.taskTags;
    }

    /**
     * Adds one or more task tags to this task.
     * Throws an exception if any of the tags already exist on this task.
     *
     * @param taskTags The task tags to add.
     * @throws TaskTagAlreadyExistsException If any tag already exists on this task.
     */
    public void addTaskTags(TaskTag... taskTags) throws TaskTagAlreadyExistsException {
        assert taskTags != null : "Task tags array cannot be null";
        for (TaskTag taskTag : taskTags) {
            assert taskTag != null : "Task tag cannot be null";
            if (this.taskTags.contains(taskTag)) {
                throw new TaskTagAlreadyExistsException("Task already has tag: " + taskTag);
            }
            this.taskTags.add(taskTag);
        }
    }

    /**
     * Removes one or more task tags from this task.
     * Throws an exception if any of the tags do not exist on this task.
     *
     * @param taskTags The task tags to remove.
     * @throws TaskTagDoesNotExistException If any tag does not exist on this task.
     */
    public void removeTaskTags(TaskTag... taskTags) throws TaskTagDoesNotExistException {
        for (TaskTag taskTag : taskTags) {
            assert taskTag != null : "Task tag cannot be null";
            if (!this.taskTags.contains(taskTag)) {
                throw new TaskTagDoesNotExistException("Task does not have tag: " + taskTag);
            }
            this.taskTags.remove(taskTag);
        }
    }


    public String getTaskTagsString() {
        if (this.taskTags.isEmpty()) {
            return "";
        }
        return this.getTaskTags().stream()
            .map(TaskTag::toString)
            .collect(Collectors.joining(", "));
    }
}
