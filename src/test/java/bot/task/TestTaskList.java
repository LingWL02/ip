package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TaskListTest {

    @Test
    void addTask_increasesSize() throws Exception {
        TaskList list = new TaskList();
        Task t = new Todo("read book"); // use your real Task subclass

        list.add(t);

        assertEquals(1, list.getSize());
        assertTrue(list.toString().contains("read book"));
    }

    @Test
    void pop_validIndex_removesTask() throws Exception {
        TaskList list = new TaskList();
        Task t1 = new Todo("A");
        Task t2 = new Todo("B");

        list.add(t1);
        list.add(t2);

        Task removed = list.pop(1);

        assertEquals("A", removed.getName());
        assertEquals(1, list.getSize());
        assertTrue(list.toString().contains("B"));
    }
}
