package bot.response;

/**
 * Represents a response from the bot, carrying both the message text
 * and a semantic type so the UI can style each message appropriately.
 */
public class Response {

    /** Semantic category of the response, used by the UI for styling. */
    public enum Type {
        /** Initial greeting shown when the bot starts. */
        GREETING,
        /** Farewell message shown on exit. */
        FAREWELL,
        /** A task was successfully added, modified, or removed. */
        SUCCESS,
        /** Informational output (e.g. task list, search results). */
        INFO,
        /** A cheerleading / motivational message. */
        CHEER,
        /** Any error – bad arguments, missing flags, internal failures, etc. */
        ERROR
    }

    private final String message;
    private final Type type;

    /**
     * Creates a new Response.
     *
     * @param message The text to display.
     * @param type    The semantic type of this response.
     */
    public Response(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    /** @return The message text. */
    public String getMessage() {
        return message;
    }

    /** @return The semantic type of this response. */
    public Type getType() {
        return type;
    }
}

