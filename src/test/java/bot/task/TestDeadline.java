package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class TestDeadline {

    private static final LocalDateTime SAMPLE_DT = LocalDateTime.of(2026, 6, 15, 23, 59);

    @Test
    void constructor_validInputs_createsDeadline() {
        Deadline d = new Deadline("submit report", SAMPLE_DT);
        assertEquals("submit report", d.getName());
        assertFalse(d.getIsMarked());
    }

    @Test
    void getTag_returnsDTag() {
        assertEquals("D", Deadline.getTag());
    }

    @Test
    void toString_unmarked_withTimeFormat() {
        Deadline d = new Deadline("submit report", SAMPLE_DT, true);
        String str = d.toString();
        assertTrue(str.startsWith("[D] [ ] submit report"));
        assertTrue(str.contains("Jun 15 2026, 23:59"));
    }

    @Test
    void toString_marked_withTimeFormat() {
        Deadline d = new Deadline("submit report", SAMPLE_DT, true);
        d.mark();
        String str = d.toString();
        assertTrue(str.contains("[X]"));
        assertTrue(str.contains("Jun 15 2026, 23:59"));
    }

    @Test
    void toString_withoutTime_excludesTime() {
        Deadline d = new Deadline("submit report", SAMPLE_DT, false);
        String str = d.toString();
        assertTrue(str.contains("Jun 15 2026"));
        assertFalse(str.contains("23:59"));
    }

    @Test
    void getByString_withTime_includesTime() {
        Deadline d = new Deadline("task", SAMPLE_DT, true);
        assertEquals("Jun 15 2026, 23:59", d.getByString());
    }

    @Test
    void getByString_withoutTime_excludesTime() {
        Deadline d = new Deadline("task", SAMPLE_DT, false);
        assertEquals("Jun 15 2026", d.getByString());
    }

    @Test
    void serialize_deserialize_roundTrip() {
        Deadline original = new Deadline("finish essay", SAMPLE_DT, true);
        String serialized = original.serialize();
        Deadline restored = Deadline.deserialize(serialized);

        assertEquals("finish essay", restored.getName());
        assertFalse(restored.getIsMarked());
        assertEquals(original.getByString(), restored.getByString());
    }

    @Test
    void serialize_deserialize_markedRoundTrip() {
        Deadline original = new Deadline("finish essay", SAMPLE_DT, true);
        original.mark();
        String serialized = original.serialize();
        Deadline restored = Deadline.deserialize(serialized);

        assertTrue(restored.getIsMarked());
    }

    @Test
    void serialize_deserialize_noTimeRoundTrip() {
        Deadline original = new Deadline("due date task", SAMPLE_DT, false);
        String serialized = original.serialize();
        Deadline restored = Deadline.deserialize(serialized);

        assertEquals("Jun 15 2026", restored.getByString());
    }

    @Test
    void serialize_deserialize_withTags() throws Exception {
        Deadline original = new Deadline("tagged task", SAMPLE_DT);
        original.addTaskTags(new TaskTag("work"), new TaskTag("priority"));
        String serialized = original.serialize();
        Deadline restored = Deadline.deserialize(serialized);

        assertEquals(2, restored.getTaskTags().size());
        assertEquals("work", restored.getTaskTags().get(0).getName());
        assertEquals("priority", restored.getTaskTags().get(1).getName());
    }
}
