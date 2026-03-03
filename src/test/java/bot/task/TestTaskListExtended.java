package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utilities.Pair;

class TestTaskListExtended {

    private TaskList list;

    @BeforeEach
    void setUp() {
        list = new TaskList();
    }

    // ─── mark ────────────────────────────────────────────────────────────────

    @Test
    void mark_validIndex_marksTask() throws Exception {
        list.add(new Todo("task A"));
        Task marked = list.mark(1);
        assertTrue(marked.getIsMarked());
    }

    @Test
    void mark_invalidIndex_throwsIndexOutOfBounds() throws Exception {
        list.add(new Todo("task A"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.mark(5));
    }

    @Test
    void mark_zeroIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.mark(0));
    }

    @Test
    void mark_alreadyMarkedTask_throwsTaskIsMarkedException() throws Exception {
        list.add(new Todo("task A"));
        list.mark(1);
        assertThrows(TaskIsMarkedException.class, () -> list.mark(1));
    }

    // ─── unmark ──────────────────────────────────────────────────────────────

    @Test
    void unmark_markedTask_unmarksTask() throws Exception {
        list.add(new Todo("task B"));
        list.mark(1);
        Task unmarked = list.unmark(1);
        assertFalse(unmarked.getIsMarked());
    }

    @Test
    void unmark_invalidIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.unmark(99));
    }

    @Test
    void unmark_alreadyUnmarkedTask_throwsTaskIsUnmarkedException() throws Exception {
        list.add(new Todo("task B"));
        assertThrows(TaskIsUnmarkedException.class, () -> list.unmark(1));
    }

    // ─── pop ─────────────────────────────────────────────────────────────────

    @Test
    void pop_invalidIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.pop(1));
    }

    @Test
    void pop_negativeIndex_throwsIndexOutOfBounds() throws Exception {
        list.add(new Todo("X"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.pop(-1));
    }

    // ─── findTasks ───────────────────────────────────────────────────────────

    @Test
    void findTasks_matchingKeyword_returnsPairs() throws Exception {
        list.add(new Todo("buy groceries"));
        list.add(new Todo("buy medicine"));
        list.add(new Todo("read book"));

        List<Pair<Integer, Task>> results = list.findTasks("buy");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getValue().getName().equals("buy groceries")));
        assertTrue(results.stream().anyMatch(p -> p.getValue().getName().equals("buy medicine")));
    }

    @Test
    void findTasks_noMatch_returnsEmptyList() throws Exception {
        list.add(new Todo("buy groceries"));
        List<Pair<Integer, Task>> results = list.findTasks("exercise");
        assertTrue(results.isEmpty());
    }

    @Test
    void findTasks_correctIndices() throws Exception {
        list.add(new Todo("alpha"));
        list.add(new Todo("beta"));
        list.add(new Todo("alpha two"));

        List<Pair<Integer, Task>> results = list.findTasks("alpha");
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getKey());
        assertEquals(3, results.get(1).getKey());
    }

    // ─── addTagsToTask ───────────────────────────────────────────────────────

    @Test
    void addTagsToTask_validIndex_addsTags() throws Exception {
        list.add(new Todo("study"));
        list.addTagsToTask(1, new TaskTag("urgent"), new TaskTag("school"));

        Task task = list.findTasks("study").get(0).getValue();
        assertEquals(2, task.getTaskTags().size());
    }

    @Test
    void addTagsToTask_invalidIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () ->
            list.addTagsToTask(1, new TaskTag("work"))
        );
    }

    @Test
    void addTagsToTask_duplicateTag_throwsTaskTagAlreadyExistsException() throws Exception {
        list.add(new Todo("project"));
        list.addTagsToTask(1, new TaskTag("cs"));
        assertThrows(TaskTagAlreadyExistsException.class, () ->
            list.addTagsToTask(1, new TaskTag("cs"))
        );
    }

    // ─── removeTagsFromTask ──────────────────────────────────────────────────

    @Test
    void removeTagsFromTask_existingTag_removesTag() throws Exception {
        list.add(new Todo("workout"));
        list.addTagsToTask(1, new TaskTag("health"));
        list.removeTagsFromTask(1, new TaskTag("health"));

        Task task = list.findTasks("workout").get(0).getValue();
        assertTrue(task.getTaskTags().isEmpty());
    }

    @Test
    void removeTagsFromTask_nonExistentTag_throwsTaskTagDoesNotExistException() throws Exception {
        list.add(new Todo("workout"));
        assertThrows(TaskTagDoesNotExistException.class, () ->
            list.removeTagsFromTask(1, new TaskTag("health"))
        );
    }

    @Test
    void removeTagsFromTask_invalidIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () ->
            list.removeTagsFromTask(5, new TaskTag("tag"))
        );
    }

    // ─── toString ────────────────────────────────────────────────────────────

    @Test
    void toString_multipleTasksList_numberedCorrectly() throws Exception {
        list.add(new Todo("first"));
        list.add(new Todo("second"));
        String str = list.toString();
        assertTrue(str.contains("1. "));
        assertTrue(str.contains("2. "));
        assertTrue(str.contains("first"));
        assertTrue(str.contains("second"));
    }

    @Test
    void toString_emptyList_returnsEmptyString() {
        assertEquals("", list.toString());
    }

    // ─── Deadline and Event in TaskList ───────────────────────────────────────

    @Test
    void add_deadlineTask_storesCorrectly() throws Exception {
        Deadline d = new Deadline("submit report", LocalDateTime.of(2026, 12, 1, 23, 59));
        list.add(d);
        assertEquals(1, list.getSize());
        assertTrue(list.toString().contains("submit report"));
    }

    @Test
    void add_eventTask_storesCorrectly() throws Exception {
        Event e = new Event("hackathon",
            LocalDateTime.of(2026, 7, 1, 9, 0),
            LocalDateTime.of(2026, 7, 2, 18, 0)
        );
        list.add(e);
        assertEquals(1, list.getSize());
        assertTrue(list.toString().contains("hackathon"));
    }
}
