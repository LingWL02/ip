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

public class TaskStorage {
    private static final String tagDelimiter = "<DELIMITER>";
    private final File file;
    private final HashMap<String, Class<? extends Task>> deserializationTagTaskMap = new HashMap<>();


    public TaskStorage(String filePath) {
        this.file = new File(filePath);
    }


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


    public List<Task> getTasks() throws IOException, ReflectiveOperationException, SecurityException {
        ArrayList<Task> taskList = new ArrayList<Task>();
        if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdirs();
        }
        if (!this.file.createNewFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tagAndSerializedTask = line.split(tagDelimiter);
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
                    taskList.add(task);
                }
            }
        }

        return taskList;
    }


    private void modifyOrDeleteFromStorage(int index, Task task, boolean delete)
    throws IOException, ReflectiveOperationException, SecurityException {
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
                            tagDelimiter +
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


    public void modify(int index, Task task)
    throws IOException, ReflectiveOperationException, SecurityException {
        modifyOrDeleteFromStorage(index, task, false);
    }

    public void remove(int index)
    throws IOException, ReflectiveOperationException, SecurityException {
        modifyOrDeleteFromStorage(index, null, true);
    }

    public void add(Task task)
    throws IOException, ReflectiveOperationException, SecurityException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, true))) {
            writer.write((String) task.getClass().getMethod("getTag").invoke(null) + tagDelimiter + task.serialize());
            writer.newLine();
        }
    }
}
