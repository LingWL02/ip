package bot.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import bot.storage.TaskStorage;
import utilities.Pair;

/**
 * Manages a list of tasks with persistent storage capabilities.
 * This class handles loading, saving, and manipulating tasks stored in a file.
 * It supports multiple task types through a tag-based deserialization system.
 *
 * <p>Tasks are stored in a file with each line containing a serialized task
 * prefixed by its type tag and a delimiter.</p>
 */
public class TaskList {

    /** The file used for persistent storage of tasks. */
    private Optional<TaskStorage> storage = Optional.empty();

    /** The in-memory list of all tasks. */
    private List<Task> taskList = new ArrayList<Task>();

    /**
     * Mounts a storage system for persistent task management.
     *
     * @param storage the storage system to mount
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public void mountStorage(TaskStorage storage)
            throws IOException, ReflectiveOperationException, SecurityException {
        this.storage = Optional.of(storage);
        this.taskList = storage.getTasks();
    }


    /**
     * Adds a task to the list and updates storage if available.
     *
     * @param task the task to add
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public void add(Task task) throws IOException, ReflectiveOperationException, SecurityException {
        assert task != null : "Task cannot be null";

        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            storage.add(task);
        }
        this.taskList.add(task);
    }


    /**
     * Marks a task as completed.
     *
     * @param index the 1-based index of the task to mark
     * @return the marked task
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws TaskIsMarkedException if the task is already marked
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public Task mark(int index)
            throws IndexOutOfBoundsException, TaskIsMarkedException,
                    IOException, ReflectiveOperationException, SecurityException {
        assert taskList.size() >= 0 : "Task list size must be non-negative";

        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        Task task = this.taskList.get(index - 1);

        if (task.getIsMarked()) {
            throw new TaskIsMarkedException(
                "%s has already been marked.".formatted(task.toString())
            );
        }
        task.mark();
        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            try {
                storage.modify(index - 1, task);
            } catch (Exception exception) {
                task.unmark();
                throw exception;
            }
        }
        return task;
    }


    /**
     * Unmarks a task (marks it as incomplete).
     *
     * @param index the 1-based index of the task to unmark
     * @return the unmarked task
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws TaskIsUnmarkedException if the task is already unmarked
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public Task unmark(int index)
            throws IndexOutOfBoundsException, TaskIsUnmarkedException,
                    IOException, ReflectiveOperationException, SecurityException {

        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        Task task = this.taskList.get(index - 1);

        if (!task.getIsMarked()) {
            throw new TaskIsUnmarkedException(
                "%s has already been unmarked.".formatted(task.toString())
            );
        }
        task.unmark();

        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            try {
                storage.modify(index - 1, task);
            } catch (Exception exception) {
                task.mark();
                throw exception;
            }
        }
        return task;
    }


    /**
     * Removes and returns a task from the list.
     *
     * @param index the 1-based index of the task to remove
     * @return the removed task
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public Task pop(int index)
            throws IndexOutOfBoundsException,
                    IOException, ReflectiveOperationException, SecurityException {

        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            storage.remove(index - 1);
        }
        return this.taskList.remove(index - 1);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return The size of the task list.
     */
    public int getSize() {
        return this.taskList.size();
    }

    /**
     * Returns a string representation of the task list.
     * Each task is displayed on a separate line with its 1-based index.
     *
     * @return A formatted string containing all tasks with their indices.
     */
    @Override
    public String toString() {
        return this.getIndexedTaskStream()
            .map(pair -> "%d. %s".formatted(pair.getKey(), pair.getValue().toString()))
            .reduce((acc, taskStr) -> acc + "\n" + taskStr)
            .orElse("");
    }

    private Stream<Pair<Integer, Task>> getIndexedTaskStream() {
        return IntStream.range(0, this.taskList.size())
        .mapToObj(i -> new Pair<Integer, Task>(i + 1, taskList.get(i)))
        .peek(pair -> {
            assert pair.getValue() != null : "Task at index " + (pair.getKey()) + " should not be null";
        });
    }

    /**
     * Finds tasks that contain the specified keyword in their name.
     *
     * @param keyword the keyword to search for
     * @return a list of pairs containing 1-based indices and matching tasks
     */
    public List<Pair<Integer, Task>> findTasks(String keyword) {
        assert keyword != null : "Search keyword cannot be null";
        return this.getIndexedTaskStream()
        .filter(pair -> pair.getValue().getName().contains(keyword))
        .toList();
    }


    private Task modifyTagsOnTask(int index, boolean addTaskTag, TaskTag... taskTags)
        throws IndexOutOfBoundsException, IOException, ReflectiveOperationException,
        SecurityException, TaskTagAlreadyExistsException, TaskTagDoesNotExistException {
        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        Task task = this.taskList.get(index - 1);

        if (addTaskTag) {
            task.addTaskTags(taskTags);
        } else {
            task.removeTaskTags(taskTags);
        }

        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            storage.modify(index - 1, task);
        }
        return task;
    }


    public Task addTagsToTask(int index, TaskTag... taskTags)
        throws IndexOutOfBoundsException, IOException, ReflectiveOperationException,
        SecurityException, TaskTagAlreadyExistsException {
        assert taskTags != null : "Task tags array cannot be null";

        try {
            return this.modifyTagsOnTask(index, true, taskTags);
        } catch (TaskTagDoesNotExistException exception) {
            throw new RuntimeException("Unexpected TaskTagDoesNotExistException when adding tags to task", exception);
        }
    }


    public Task removeTagsFromTask(int index, TaskTag... taskTags)
        throws IndexOutOfBoundsException, IOException, ReflectiveOperationException,
        SecurityException, TaskTagDoesNotExistException {
        assert taskTags != null : "Task tags array cannot be null";

        try {
            return this.modifyTagsOnTask(index, false, taskTags);
        } catch (TaskTagAlreadyExistsException exception) {
            throw new RuntimeException("Unexpected TaskTagAlreadyExistsException when removing tags from task", exception);
        }
    }
}
