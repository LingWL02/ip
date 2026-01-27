package app.task;

import java.util.List;
import java.util.ArrayList;

public class TaskList {
    private final List<String> taskList= new ArrayList<String>();

    public void add(String task) {
        this.taskList.add(task);
    }

    public String toString() {
        if (this.taskList.isEmpty()) {
            return "Task list is empty!";
        }
        StringBuilder bobTheBuilder = new StringBuilder("Task list:");
        for (int i = 0; i < this.taskList.size(); i++) {
            String task = taskList.get(i);

            bobTheBuilder.append("\n%d. %s".formatted(i + 1, task.toString()));
        }

        return bobTheBuilder.toString();
    }

}
