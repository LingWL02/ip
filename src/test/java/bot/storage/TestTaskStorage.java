package bot.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bot.task.Deadline;
import bot.task.DuplicateTagException;
import bot.task.Event;
import bot.task.Task;
import bot.task.Todo;

class TestTaskStorage {

    @TempDir
    Path tempDir;

    private TaskStorage taskStorage;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve("test_tasks.txt").toString();
        taskStorage = new TaskStorage(testFilePath);
    }

    @AfterEach
    void tearDown() throws IOException {
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void constructor_createsTaskStorageWithFilePath() {
        TaskStorage storage = new TaskStorage("test.txt");
        assertTrue(storage != null);
    }

    @Test
    void subscribeTaskDeserialization_validTaskClasses_subscribesSuccessfully() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Deadline.class);
        taskClasses.add(Event.class);

        taskStorage.subscribeTaskDeserialization(taskClasses);

        // If no exception is thrown, subscription was successful
        assertTrue(true);
    }

    @Test
    void subscribeTaskDeserialization_duplicateTags_throwsDuplicateTagException() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Todo.class); // Adding same class twice should cause duplicate tag

        assertThrows(DuplicateTagException.class, () -> {
            taskStorage.subscribeTaskDeserialization(taskClasses);
        });
    }

    @Test
    void getTasks_emptyFile_returnsEmptyList() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        List<Task> tasks = taskStorage.getTasks();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void add_singleTask_addsTaskToStorage() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        Task task = new Todo("Read book");
        taskStorage.add(task);

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Read book", tasks.get(0).getName());
        assertFalse(tasks.get(0).getIsMarked());
    }

    @Test
    void add_multipleTasks_addsAllTasksToStorage() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Deadline.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        Task todo = new Todo("Buy groceries");
        Task deadline = new Deadline("Submit assignment", LocalDateTime.of(2026, 3, 15, 23, 59));

        taskStorage.add(todo);
        taskStorage.add(deadline);

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Buy groceries", tasks.get(0).getName());
        assertEquals("Submit assignment", tasks.get(1).getName());
    }

    @Test
    void modify_validIndex_modifiesTask() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        Task originalTask = new Todo("Original task");
        taskStorage.add(originalTask);

        Task modifiedTask = new Todo("Modified task");
        modifiedTask.mark();
        taskStorage.modify(0, modifiedTask);

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Modified task", tasks.get(0).getName());
        assertTrue(tasks.get(0).getIsMarked());
    }

    @Test
    void remove_validIndex_removesTask() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        Task task1 = new Todo("Task 1");
        Task task2 = new Todo("Task 2");
        Task task3 = new Todo("Task 3");

        taskStorage.add(task1);
        taskStorage.add(task2);
        taskStorage.add(task3);

        taskStorage.remove(1); // Remove middle task

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
        assertEquals("Task 3", tasks.get(1).getName());
    }

    @Test
    void getTasks_afterModifications_returnsCorrectTasks() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Deadline.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        // Add initial tasks
        Task todo = new Todo("Initial todo");
        Task deadline = new Deadline("Initial deadline", LocalDateTime.of(2026, 4, 1, 12, 0));
        taskStorage.add(todo);
        taskStorage.add(deadline);

        // Modify first task
        Task modifiedTodo = new Todo("Modified todo");
        modifiedTodo.mark();
        taskStorage.modify(0, modifiedTodo);

        // Remove second task
        taskStorage.remove(1);

        // Add new task
        Task newTask = new Todo("New task");
        taskStorage.add(newTask);

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Modified todo", tasks.get(0).getName());
        assertTrue(tasks.get(0).getIsMarked());
        assertEquals("New task", tasks.get(1).getName());
        assertFalse(tasks.get(1).getIsMarked());
    }

    @Test
    void getTasks_mixedTaskTypes_deserializesAllCorrectly() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Deadline.class);
        taskClasses.add(Event.class);
        taskStorage.subscribeTaskDeserialization(taskClasses);

        Todo todo = new Todo("Buy milk");
        todo.mark();
        Deadline deadline = new Deadline("Submit report", LocalDateTime.of(2026, 3, 20, 15, 30));
        Event event = new Event("Team meeting", LocalDateTime.of(2026, 3, 18, 10, 0),
                              LocalDateTime.of(2026, 3, 18, 11, 0));

        taskStorage.add(todo);
        taskStorage.add(deadline);
        taskStorage.add(event);

        List<Task> tasks = taskStorage.getTasks();
        assertEquals(3, tasks.size());

        // Check todo
        assertTrue(tasks.get(0) instanceof Todo);
        assertEquals("Buy milk", tasks.get(0).getName());
        assertTrue(tasks.get(0).getIsMarked());

        // Check deadline
        assertTrue(tasks.get(1) instanceof Deadline);
        assertEquals("Submit report", tasks.get(1).getName());
        assertFalse(tasks.get(1).getIsMarked());

        // Check event
        assertTrue(tasks.get(2) instanceof Event);
        assertEquals("Team meeting", tasks.get(2).getName());
        assertFalse(tasks.get(2).getIsMarked());
    }

    @Test
    void getTasks_fileDoesNotExist_createsFileAndReturnsEmptyList() throws Exception {
        String nonExistentPath = tempDir.resolve("non_existent.txt").toString();
        TaskStorage storage = new TaskStorage(nonExistentPath);

        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        storage.subscribeTaskDeserialization(taskClasses);

        List<Task> tasks = storage.getTasks();

        assertTrue(tasks.isEmpty());
        assertTrue(new File(nonExistentPath).exists());
    }

    @Test
    void storage_persistsAcrossInstances() throws Exception {
        List<Class<? extends Task>> taskClasses = new ArrayList<>();
        taskClasses.add(Todo.class);
        taskClasses.add(Deadline.class);

        // First instance - add tasks
        TaskStorage storage1 = new TaskStorage(testFilePath);
        storage1.subscribeTaskDeserialization(taskClasses);

        Task todo = new Todo("Persistent task");
        todo.mark();
        Task deadline = new Deadline("Persistent deadline", LocalDateTime.of(2026, 5, 1, 9, 0));

        storage1.add(todo);
        storage1.add(deadline);

        // Second instance - read tasks
        TaskStorage storage2 = new TaskStorage(testFilePath);
        storage2.subscribeTaskDeserialization(taskClasses);

        List<Task> tasks = storage2.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Persistent task", tasks.get(0).getName());
        assertTrue(tasks.get(0).getIsMarked());
        assertEquals("Persistent deadline", tasks.get(1).getName());
        assertFalse(tasks.get(1).getIsMarked());
    }
}
