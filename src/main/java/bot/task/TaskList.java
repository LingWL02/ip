package bot.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private List<Task> taskList = new ArrayList<>();

    public void mountStorage(TaskStorage storage)
    throws IOException, ReflectiveOperationException, SecurityException {
        this.storage = Optional.of(storage);
        this.taskList = storage.getTasks();
    }


    public void add(Task task) throws IOException, ReflectiveOperationException, SecurityException {
        if (this.storage.isPresent()) {
            TaskStorage storage = this.storage.get();
            storage.add(task);
        }
        this.taskList.add(task);
    }


    public Task mark(int index)
    throws IndexOutOfBoundsException, TaskIsMarkedException,
    IOException, ReflectiveOperationException, SecurityException {
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
        StringBuilder bobTheBuilder = new StringBuilder();

        for (int i = 0; i < this.taskList.size(); i++) {
            Task task = taskList.get(i);
            bobTheBuilder.append("%s%d. %s".formatted((i > 0) ? "\n" : "", i + 1, task.toString()));
        }
        return bobTheBuilder.toString();
    }

    public List<Pair<Integer, Task>> findTasks(String keyword) {
        List<Pair<Integer, Task>> foundTasks = new ArrayList<>();
        for (int i = 0; i < this.taskList.size(); i++) {
            Task task = taskList.get(i);
            if (task.getName().contains(keyword)) {
                foundTasks.add(new Pair<>(i + 1, task));
            }
        }
        return foundTasks;
    }
}
