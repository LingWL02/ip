import bot.Bot;

/**
 * Main entry point class for the Duke chatbot application.
 * Duke is a task management chatbot that helps users manage their todos, deadlines, and events.
 */
public class Duke {

    /**
     * Main method that launches the Duke application.
     * Creates an App instance with a bot name and line separator,
     * then starts the application's main loop.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Bot app = new Bot("Duke", "_".repeat(50));

        app.run();
    }
}
