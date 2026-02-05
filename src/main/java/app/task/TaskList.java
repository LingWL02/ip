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

public class TaskList {
    private final File file;
    private final String tagDelimiter = "||||";

    private final List<Task> taskList= new ArrayList<>();
    private final HashMap<String, Class<? extends Task>> deserializationTagTaskMap = new HashMap<>();

    public TaskList(String filePath) {
        this.file = new File(filePath);
    }

    public void load() throws IOException, ReflectiveOperationException, SecurityException {
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


    public void add(Task task) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, true))) {
            writer.write(task.getTag() + this.tagDelimiter + task.serialize());
            writer.newLine();
        }
        this.taskList.add(task);
    }


    public Task mark(int index) throws IndexOutOfBoundsException, TaskIsMarkedException {
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
        return task;
    }


    public Task unmark(int index) throws IndexOutOfBoundsException, TaskIsUnmarkedException {
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
        return task;
    }


    public Task pop(int index) throws IndexOutOfBoundsException {
        if (index < 1 || index > this.taskList.size()) {
            throw new IndexOutOfBoundsException(
                "Index %d is out of bounds of Task List of size %d.".formatted(index, this.taskList.size())
            );
        }
        return this.taskList.remove(index - 1);
    }


    public int getSize() {
        return this.taskList.size();
    }


    @Override
    public String toString() {
        StringBuilder bobTheBuilder = new StringBuilder();

        for (int i = 0; i < this.taskList.size(); i++) {
            Task task = taskList.get(i);
            bobTheBuilder.append("%s%d. %s".formatted((i > 0) ? "\n" : "", i + 1, task.toString()));
        }
        return bobTheBuilder.toString();
    }


    private void modifyFile(int index, String newString, boolean delete) throws IOException {
        File tempFile = File.createTempFile("",".tmp", this.file.getParentFile());
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
                        writer.write(newString);
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
