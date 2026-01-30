package app.task;

import java.util.List;
import java.util.ArrayList;

public class TaskList {
    private final List<Task> taskList= new ArrayList<>();


    public void add(Task task) {
        this.taskList.add(task);
    }


    public Task mark(Integer index) throws IndexOutOfBoundsException, TaskIsMarkedException {
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


    public Task unmark(Integer index) throws IndexOutOfBoundsException, TaskIsUnmarkedException {
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


    @Override
    public String toString() {
        StringBuilder bobTheBuilder = new StringBuilder();

        for (int i = 0; i < this.taskList.size(); i++) {
            Task task = taskList.get(i);
            bobTheBuilder.append("%s%d. %s".formatted((i > 0) ? "\n" : "", i + 1, task.toString()));
        }
        return bobTheBuilder.toString();
    }

}
