package bot.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class TestTaskTag {

    @Test
    void constructor_validName_createsTag() {
        TaskTag tag = new TaskTag("urgent");
        assertEquals("urgent", tag.getName());
    }

    @Test
    void constructor_nameWithWhitespace_stripsWhitespace() {
        TaskTag tag = new TaskTag("  health  ");
        assertEquals("health", tag.getName());
    }

    @Test
    void toString_returnsPrefixedName() {
        TaskTag tag = new TaskTag("work");
        assertEquals("#work", tag.toString());
    }

    @Test
    void equals_sameNameTags_areEqual() {
        TaskTag tag1 = new TaskTag("urgent");
        TaskTag tag2 = new TaskTag("urgent");
        assertEquals(tag1, tag2);
    }

    @Test
    void equals_differentNameTags_areNotEqual() {
        TaskTag tag1 = new TaskTag("urgent");
        TaskTag tag2 = new TaskTag("low");
        assertNotEquals(tag1, tag2);
    }

    @Test
    void equals_null_returnsFalse() {
        TaskTag tag = new TaskTag("urgent");
        assertFalse(tag.equals(null));
    }

    @Test
    void equals_sameObject_returnsTrue() {
        TaskTag tag = new TaskTag("urgent");
        assertTrue(tag.equals(tag));
    }

    @Test
    void hashCode_equalTags_samHashCode() {
        TaskTag tag1 = new TaskTag("urgent");
        TaskTag tag2 = new TaskTag("urgent");
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    void serializeTaskTags_emptyList_returnsSentinel() {
        String serialized = TaskTag.serializeTaskTags(List.of());
        // The sentinel should be returned for empty lists
        List<TaskTag> deserialized = TaskTag.deserializeTaskTags(serialized);
        assertTrue(deserialized.isEmpty());
    }

    @Test
    void serializeTaskTags_singleTag_roundTrip() {
        List<TaskTag> tags = List.of(new TaskTag("work"));
        String serialized = TaskTag.serializeTaskTags(tags);
        List<TaskTag> deserialized = TaskTag.deserializeTaskTags(serialized);

        assertEquals(1, deserialized.size());
        assertEquals("work", deserialized.get(0).getName());
    }

    @Test
    void serializeTaskTags_multipleTags_roundTrip() {
        List<TaskTag> tags = List.of(new TaskTag("work"), new TaskTag("urgent"), new TaskTag("personal"));
        String serialized = TaskTag.serializeTaskTags(tags);
        List<TaskTag> deserialized = TaskTag.deserializeTaskTags(serialized);

        assertEquals(3, deserialized.size());
        assertEquals("work", deserialized.get(0).getName());
        assertEquals("urgent", deserialized.get(1).getName());
        assertEquals("personal", deserialized.get(2).getName());
    }
}
