package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestTodo {

    @Test
    void constructor_validName_createsTodo() {
        Todo todo = new Todo("buy milk");
        assertEquals("buy milk", todo.getName());
        assertFalse(todo.getIsMarked());
    }

    @Test
    void getTag_returnsTTag() {
        assertEquals("T", Todo.getTag());
    }

    @Test
    void toString_unmarked_correctFormat() {
        Todo todo = new Todo("buy milk");
        assertEquals("[T] [ ] buy milk", todo.toString());
    }

    @Test
    void toString_marked_correctFormat() {
        Todo todo = new Todo("buy milk");
        todo.mark();
        assertEquals("[T] [X] buy milk", todo.toString());
    }

    @Test
    void serialize_unmarkedNoTags_correctFormat() {
        Todo todo = new Todo("buy milk");
        String serialized = todo.serialize();
        // Should contain name, "false", and the no-tags sentinel
        assertTrue(serialized.contains("buy milk"));
        assertTrue(serialized.contains("false"));
    }

    @Test
    void serialize_markedNoTags_containsTrue() {
        Todo todo = new Todo("buy milk");
        todo.mark();
        assertTrue(todo.serialize().contains("true"));
    }

    @Test
    void deserialize_unmarkedTodo_restoresCorrectly() {
        Todo original = new Todo("read a book");
        String serialized = original.serialize();
        Task restored = Todo.deserialize(serialized);

        assertEquals("read a book", restored.getName());
        assertFalse(restored.getIsMarked());
        assertEquals("T", Todo.getTag());
    }

    @Test
    void deserialize_markedTodo_restoresMarkedState() {
        Todo original = new Todo("read a book");
        original.mark();
        String serialized = original.serialize();
        Task restored = Todo.deserialize(serialized);

        assertEquals("read a book", restored.getName());
        assertTrue(restored.getIsMarked());
    }

    @Test
    void deserialize_todoWithTags_restoresTags() throws Exception {
        Todo original = new Todo("go for a run");
        original.addTaskTags(new TaskTag("health"), new TaskTag("morning"));
        String serialized = original.serialize();
        Task restored = Todo.deserialize(serialized);

        assertEquals(2, restored.getTaskTags().size());
        assertEquals("health", restored.getTaskTags().get(0).getName());
        assertEquals("morning", restored.getTaskTags().get(1).getName());
    }

    @Test
    void toString_todoWithTags_includesTags() throws Exception {
        Todo todo = new Todo("exercise");
        todo.addTaskTags(new TaskTag("fitness"));
        String str = todo.toString();
        assertTrue(str.contains("#fitness"));
    }

    @Test
    void mark_unmarkedTask_marksSuccessfully() {
        Todo todo = new Todo("write report");
        todo.mark();
        assertTrue(todo.getIsMarked());
    }

    @Test
    void unmark_markedTask_unmarksSuccessfully() {
        Todo todo = new Todo("write report");
        todo.mark();
        todo.unmark();
        assertFalse(todo.getIsMarked());
    }

    @Test
    void addTaskTags_duplicateTag_throwsException() throws Exception {
        Todo todo = new Todo("study");
        todo.addTaskTags(new TaskTag("urgent"));
        assertThrows(TaskTagAlreadyExistsException.class, () -> {
            todo.addTaskTags(new TaskTag("urgent"));
        });
    }

    @Test
    void removeTaskTags_nonExistentTag_throwsException() {
        Todo todo = new Todo("study");
        assertThrows(TaskTagDoesNotExistException.class, () -> {
            todo.removeTaskTags(new TaskTag("urgent"));
        });
    }
}
