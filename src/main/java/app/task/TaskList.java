package app.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private final File file;

    /** The delimiter used to separate the task tag from the serialized task data. */
    private final String tagDelimiter = "<DELIMITER>";

    /** The in-memory list of all tasks. */
    private final List<Task> taskList = new ArrayList<>();

    /** Map from task type tags to their corresponding Task subclass for deserialization. */
    private final HashMap<String, Class<? extends Task>> deserializationTagTaskMap = new HashMap<>();

    /**
     * Constructs a new TaskList that persists to the specified file path.
     *
     * @param filePath The path to the file for storing tasks.
     */
    public TaskList(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * Loads tasks from the persistent storage file.
     * Creates the parent directory and file if they don't exist.
     * Deserializes each line in the file and adds valid tasks to the in-memory list.
     *
     * @throws IOException                If an I/O error occurs during file operations.
     * @throws ReflectiveOperationException If a reflection error occurs during deserialization.
     * @throws SecurityException          If a security manager denies access.
     */
    public void load() throws IOException, ReflectiveOperationException, SecurityException {
        if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdirs();
        }
        if (!this.file.createNewFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tagAndSerializedTask = line.split(this.tagDelimiter);
                    if (tagAndSerializedTask.length != 2) {
                        continue;  // TODO: Handle invalid serialized task line
                    }
                    String tag = tagAndSerializedTask[0];
                    String serializedTask = tagAndSerializedTask[1];
                    if (!this.deserializationTagTaskMap.containsKey(tag)) {
                        continue;  // TODO: Handle unknown deserialization tag
                    }

                    Class<? extends Task> taskClass = this.deserializationTagTaskMap.get(tag);
                    Method deserializeMethod = taskClass.getMethod("deserialize", String.class);
                    Task task = (Task) deserializeMethod.invoke(null, serializedTask);
                    this.taskList.add(task);
                }
            }
        }
    }

    /**
     * Registers task classes for deserialization.
     * Each task class must have a static getTag() method that returns a unique tag string.
     *
     * @param taskClasses A list of Task subclasses to register for deserialization.
     * @throws DuplicateTagException      If a task class has a tag that is already registered.
     * @throws ReflectiveOperationException If the getTag() method cannot be invoked.
     * @throws SecurityException          If a security manager denies reflective access.
     */
    public void subscribeTaskDeserialization(
        List<Class<? extends Task>> taskClasses
    ) throws DuplicateTagException, ReflectiveOperationException, SecurityException {
        for (Class<? extends Task> taskClass : taskClasses) {
            String tag = (String) taskClass.getMethod("getTag").invoke(null);

            if (this.deserializationTagTaskMap.containsKey(tag)) {
                throw new DuplicateTagException(
                    "Attempted to add Tag %s which already exists".formatted(tag)
                );
            }
            this.deserializationTagTaskMap.put(tag, taskClass);
        }
    }

    /**
     * Adds a new task to the list and persists it to the storage file.
     *
     * @param task The task to add.
     * @throws IOException                If an I/O error occurs while writing to the file.
     * @throws ReflectiveOperationException If the task's getTag() method cannot be invoked.
     * @throws SecurityException          If a security manager denies reflective access.
     */
    public void add(Task task) throws IOException, ReflectiveOperationException, SecurityException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, true))) {
            writer.write((String) task.getClass().getMethod("getTag").invoke(null) + this.tagDelimiter + task.serialize());
            writer.newLine();
        }
        this.taskList.add(task);
    }

    /**
     * Marks a task as completed by its 1-based index.
     * Updates both the in-memory list and the persistent storage.
     *
     * @param index The 1-based index of the task to mark.
     * @return The marked task.
     * @throws IndexOutOfBoundsException   If the index is out of valid range.
     * @throws TaskIsMarkedException       If the task is already marked.
     * @throws IOException                 If an I/O error occurs while updating the file.
     * @throws ReflectiveOperationException If a reflection error occurs during file update.
     * @throws SecurityException           If a security manager denies reflective access.
     */
    public Task mark(int index)
    throws IndexOutOfBoundsException, TaskIsMarkedException, IOException, ReflectiveOperationException, SecurityException {
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
        try {
            this.modifyFile(index - 1, task, false);
        } catch (IOException exception) {
            task.unmark();
            throw exception;
        }
        return task;
    }

    /**
     * Unmarks a task (marks as not completed) by its 1-based index.
     * Updates both the in-memory list and the persistent storage.
     *
     * @param index The 1-based index of the task to unmark.
     * @return The unmarked task.
     * @throws IndexOutOfBoundsException    If the index is out of valid range.
     * @throws TaskIsUnmarkedException      If the task is already unmarked.
     * @throws IOException                  If an I/O error occurs while updating the file.
     * @throws ReflectiveOperationException If a reflection error occurs during file update.
     * @throws SecurityException            If a security manager denies reflective access.
     */
    public Task unmark(int index)
    throws IndexOutOfBoundsException, TaskIsUnmarkedException, IOException, ReflectiveOperationException, SecurityException {
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
        try {
            this.modifyFile(index - 1, task, false);
        } catch (IOException exception) {
            task.mark();
            throw exception;
        }
        return task;
    }

    /**
     * Removes and returns a task by its 1-based index.
     * Updates both the in-memory list and the persistent storage.
     *
     * @param index The 1-based index of the task to remove.
     * @return The removed task.
     * @throws IndexOutOfBoundsException   If the index is out of valid range.
     * @throws IOException                 If an I/O error occurs while updating the file.
     * @throws ReflectiveOperationException If a reflection error occurs during file update.
     * @throws SecurityException           If a security manager denies reflective access.
     */
    public Task pop(int index) throws IndexOutOfBoundsException, IOException, ReflectiveOperationException, SecurityException {
        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        this.modifyFile(index - 1, null, true);  // Whatever I dont care
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

    /**
     * Modifies a specific line in the storage file.
     * Used internally to update or delete task entries.
     *
     * @param index  The 0-based index of the line to modify.
     * @param task   The task to write (used when updating, ignored when deleting).
     * @param delete If true, the line is deleted; if false, the line is updated.
     * @throws IOException                 If an I/O error occurs during file operations.
     * @throws ReflectiveOperationException If the task's getTag() method cannot be invoked.
     * @throws SecurityException           If a security manager denies reflective access.
     */
    private void modifyFile(int index, Task task, boolean delete) throws IOException, ReflectiveOperationException, SecurityException {
        File tempFile = File.createTempFile("temp",".tmp", this.file.getParentFile());
        try (
            BufferedReader reader = new BufferedReader(new FileReader(this.file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            int currentIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (currentIndex++ == index) {
                    if (delete) {
                        continue;
                    } else {
                        writer.write(
                            (String) task.getClass().getMethod("getTag").invoke(null) +
                            this.tagDelimiter +
                            task.serialize()
                        );
                    }
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        }

        if (!file.delete()) {
            throw new IOException("Could not delete original file");
        }

        if (!tempFile.renameTo(file)) {
            throw new IOException("Could not rename temp file");
        }
    }

}
