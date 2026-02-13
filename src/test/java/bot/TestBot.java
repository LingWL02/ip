package bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestBot {

    private Bot bot;

    @BeforeEach
    void setUp() {
        bot = new Bot("TestBot", "====================");
    }

    @Test
    void constructor_validInputs_createsBot() {
        // Test that bot is created successfully and getGreeting works
        String expectedGreeting = "Hello! I'm TestBot!\nWhat can I do for you?";
        assertEquals(expectedGreeting, bot.getGreeting());
    }

    @Test
    void getFarewell_returnsCorrectMessage() {
        // Test that farewell message is correct
        String expectedFarewell = "Bye. Hope to see you again soon!";
        assertEquals(expectedFarewell, bot.getFarewell());
    }

    @Test
    void isAlive_initialState_returnsTrue() {
        // Test that bot is alive initially
        assertTrue(bot.isAlive());
    }
}
