package bot.storage;

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

import bot.task.DuplicateTagException;
import bot.task.Task;

/**
 * Handles file-based storage operations for tasks, including serialization and deserialization.
 */
public class TaskStorage {
    private static final String tagDelimiter = "<DELIMITER>";
    private final File file;
    private final HashMap<String, Class<? extends Task>> deserializationTagTaskMap = new HashMap<>();

    /**
     * Constructs a TaskStorage with the specified file path.
     *
     * @param filePath the path to the storage file
     */
    public TaskStorage(String filePath) {
        assert filePath != null : "File path cannot be null";
        assert !filePath.trim().isEmpty() : "File path cannot be empty";
        this.file = new File(filePath);
    }

    /**
     * Subscribes task classes for deserialization, allowing them to be loaded from storage.
     *
     * @param taskClasses the list of task classes to subscribe
     * @throws DuplicateTagException if duplicate tags are encountered
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public void subscribeTaskDeserialization(
        Class<? extends Task>... taskClasses
    ) throws DuplicateTagException, ReflectiveOperationException, SecurityException {
        assert taskClasses != null : "Task classes array cannot be null";
        for (Class<? extends Task> taskClass : taskClasses) {
            assert taskClass != null : "Task class cannot be null";
            String tag = (String) taskClass.getMethod("getTag").invoke(null);

            if (this.deserializationTagTaskMap.containsKey(tag)) {
                throw new DuplicateTagException(
                    "Attempted to add Tag %s which already exists".formatted(tag)
                );
            }
            this.deserializationTagTaskMap.put(tag, taskClass);
        }
    }


    public List<Task> getTasks() throws IOException, ReflectiveOperationException, SecurityException {
        ArrayList<Task> taskList = new ArrayList<Task>();
        File parentDir = this.file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!this.file.createNewFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tagAndSerializedTask = line.split(tagDelimiter);
                    assert tagAndSerializedTask.length == 2 : "Invalid serialized task line format";
                    String tag = tagAndSerializedTask[0];
                    String serializedTask = tagAndSerializedTask[1];
                    assert this.deserializationTagTaskMap.containsKey(tag) : "Unknown deserialization tag: " + tag;

                    Class<? extends Task> taskClass = this.deserializationTagTaskMap.get(tag);
                    Method deserializeMethod = taskClass.getMethod("deserialize", String.class);
                    Task task = (Task) deserializeMethod.invoke(null, serializedTask);
                    taskList.add(task);
                }
            }
        }

        return taskList;
    }


    private void modifyOrDeleteFromStorage(int index, Task task, boolean delete)
            throws IOException, ReflectiveOperationException, SecurityException {
        assert index >= 0 : "Index must be non-negative";
        assert file != null : "Storage file must be initialized";
        assert !delete || task == null : "Task must be null when deleting";
        assert delete || task != null : "Task must not be null when modifying";

        File parentDir = this.file.getParentFile();
        File tempFile = File.createTempFile("temp", ".tmp", parentDir);
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
                            (String) task.getClass().getMethod("getTag").invoke(null)
                            + tagDelimiter
                            + task.serialize()
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


    /**
     * Modifies a task at the specified index in the storage file.
     *
     * @param index the index of the task to modify (must be non-negative)
     * @param task the new task to replace the existing task at the given index
     * @throws IOException if an I/O error occurs while writing to the storage file
     * @throws ReflectiveOperationException if reflection fails during task processing
     * @throws SecurityException if security restrictions prevent the operation
     */
    public void modify(int index, Task task)
            throws IOException, ReflectiveOperationException, SecurityException {
        assert index >= 0 : "Index must be non-negative";
        assert task != null : "Task cannot be null for modification";
        modifyOrDeleteFromStorage(index, task, false);
    }

    /**
     * Removes a task at the specified index from the storage file.
     *
     * @param index the index of the task to remove (must be non-negative)
     * @throws IOException if an I/O error occurs while writing to the storage file
     * @throws ReflectiveOperationException if reflection fails during task processing
     * @throws SecurityException if security restrictions prevent the operation
     */
    public void remove(int index)
            throws IOException, ReflectiveOperationException, SecurityException {
        assert index >= 0 : "Index must be non-negative";
        modifyOrDeleteFromStorage(index, null, true);
    }

    /**
     * Adds a task to the storage file.
     *
     * @param task the task to add
     * @throws IOException if an I/O error occurs
     * @throws ReflectiveOperationException if reflection operations fail
     * @throws SecurityException if security restrictions apply
     */
    public void add(Task task)
            throws IOException, ReflectiveOperationException, SecurityException {
        assert task != null : "Task cannot be null";
        assert file != null : "Storage file must be initialized";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, true))) {
            writer.write((String) task.getClass().getMethod("getTag").invoke(null)
                    + tagDelimiter + task.serialize());
            writer.newLine();
        }
    }
}
