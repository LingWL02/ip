package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class TestEvent {

    private static final LocalDateTime FROM = LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 5, 1, 17, 0);

    @Test
    void constructor_validInputs_createsEvent() {
        Event e = new Event("team meeting", FROM, TO);
        assertEquals("team meeting", e.getName());
        assertFalse(e.getIsMarked());
    }

    @Test
    void getTag_returnsETag() {
        assertEquals("E", Event.getTag());
    }

    @Test
    void toString_unmarked_withBothTimes_correctFormat() {
        Event e = new Event("conference", FROM, true, TO, true);
        String str = e.toString();
        assertTrue(str.startsWith("[E] [ ] conference"));
        assertTrue(str.contains("May 01 2026, 09:00"));
        assertTrue(str.contains("May 01 2026, 17:00"));
    }

    @Test
    void toString_marked_correctFormat() {
        Event e = new Event("conference", FROM, TO);
        e.mark();
        String str = e.toString();
        assertTrue(str.contains("[X]"));
    }

    @Test
    void toString_withoutStartTime_excludesStartTime() {
        Event e = new Event("conference", FROM, false, TO, true);
        String str = e.toString();
        assertTrue(str.contains("May 01 2026"));
        assertFalse(str.contains("09:00"));
    }

    @Test
    void toString_withoutEndTime_excludesEndTime() {
        Event e = new Event("conference", FROM, true, TO, false);
        String str = e.toString();
        assertFalse(str.contains("17:00"));
    }

    @Test
    void getStartString_withTime_includesTime() {
        Event e = new Event("conference", FROM, true, TO, true);
        assertEquals("May 01 2026, 09:00", e.getStartString());
    }

    @Test
    void getStartString_withoutTime_excludesTime() {
        Event e = new Event("conference", FROM, false, TO, true);
        assertEquals("May 01 2026", e.getStartString());
    }

    @Test
    void getEndString_withTime_includesTime() {
        Event e = new Event("conference", FROM, true, TO, true);
        assertEquals("May 01 2026, 17:00", e.getEndString());
    }

    @Test
    void getEndString_withoutTime_excludesTime() {
        Event e = new Event("conference", FROM, true, TO, false);
        assertEquals("May 01 2026", e.getEndString());
    }

    @Test
    void serialize_deserialize_roundTrip() {
        Event original = new Event("annual meeting", FROM, true, TO, true);
        String serialized = original.serialize();
        Task restored = Event.deserialize(serialized);

        assertEquals("annual meeting", restored.getName());
        assertFalse(restored.getIsMarked());
    }

    @Test
    void serialize_deserialize_markedRoundTrip() {
        Event original = new Event("annual meeting", FROM, TO);
        original.mark();
        String serialized = original.serialize();
        Task restored = Event.deserialize(serialized);

        assertTrue(restored.getIsMarked());
    }

    @Test
    void serialize_deserialize_noTimesRoundTrip() {
        Event original = new Event("all day event", FROM, false, TO, false);
        String serialized = original.serialize();
        Event restored = (Event) Event.deserialize(serialized);

        assertEquals("May 01 2026", restored.getStartString());
        assertEquals("May 01 2026", restored.getEndString());
    }

    @Test
    void serialize_deserialize_withTags() throws Exception {
        Event original = new Event("hackathon", FROM, TO);
        original.addTaskTags(new TaskTag("tech"), new TaskTag("team"));
        String serialized = original.serialize();
        Task restored = Event.deserialize(serialized);

        assertEquals(2, restored.getTaskTags().size());
        assertEquals("tech", restored.getTaskTags().get(0).getName());
        assertEquals("team", restored.getTaskTags().get(1).getName());
    }

    @Test
    void event_sameStartAndEndTime_isValid() {
        LocalDateTime same = LocalDateTime.of(2026, 1, 1, 10, 0);
        // Should not throw
        Event e = new Event("instant event", same, same);
        assertEquals("instant event", e.getName());
    }
}
