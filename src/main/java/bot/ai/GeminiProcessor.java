package bot.ai;

import java.util.Optional;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import bot.response.Response;

/**
 * Wraps the Google Gemini API for generating text responses.
 */
public class GeminiProcessor {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");

    private final String model = "gemini-2.5-flash";

    private String historyContext = "";

    private final Optional<Client> client = (API_KEY != null && !API_KEY.isBlank())
        ? Optional.of(Client.builder().apiKey(API_KEY).build())
        : Optional.empty();

    private final GenerateContentConfig config;

    /**
     * Constructs a GeminiProcessor with the given system prompt.
     *
     * @param systemPrompt The system instruction that defines the AI's persona and behaviour.
     */
    public GeminiProcessor(String systemPrompt) {
        config = GenerateContentConfig.builder()
            .systemInstruction(
                Content.fromParts(Part.fromText(systemPrompt))
            )
            .build();
    }


    /**
     * Augments a bot response using Gemini, rewriting it in the configured persona.
     * For unrecognized inputs, Gemini determines intent and responds accordingly.
     *
     * @param userInput The original user input string.
     * @param response  The raw bot response to augment.
     * @return A new Response with the Gemini-augmented message, or the original if unavailable.
     */
    public Response augmentResponse(String userInput, Response response) {
        String history = historyContext.isBlank()
            ? ""
            : """
            CONVERSATION HISTORY (for context only) — use it to maintain continuity, \
            refer back when relevant: \
            %s

            """.formatted(historyContext);

        String prompt = (response.getType() == Response.Type.UNKNOWN)
            ? """
            %sThe user's input didn't match any command. Stay fully in persona and respond naturally based \
            on what they seem to want:
            - If it reads like casual conversation, small talk, or a follow-up to the chat \
            history — just talk back. Be engaging, witty, in character. No need to mention \
            commands unless they come up naturally.
            - If they seem to be asking what the bot can do or asking for help — give a SHORT \
            list of command names with a one-liner each. No syntax or examples unless they ask \
            about one specific command.
            - If they're asking about a specific command — give its syntax and one example only.
            - If it looks like a typo of a real command — correct it with the right syntax and \
            one example in one sentence.
            Use the conversation history to maintain continuity. \
            Keep the response concise and fully in persona.
            User Input: %s \
            Response Type: %s \
            Original Response: %s
            """.formatted(history, userInput, response.getType().name(), response.getMessage())
            : """
            %sRewrite the following bot response in your persona. \
            Stay fully in character — do not sound robotic or generic. \
            Preserve all key information exactly: do not add, remove, or change any facts, \
            command syntax, task names, dates, or indices. \
            If the user's input feels conversational, let your personality shine through \
            while still delivering the information cleanly. \
            User Input: %s \
            Response Type: %s \
            Original Response: %s
            """.formatted(history, userInput, response.getType().name(), response.getMessage());

        Response augmentedResponse = this.client.map(
            c -> {
                GenerateContentResponse genResponse = c.models.generateContent(this.model, prompt, this.config);
                String augmentedMessage = genResponse.text();
                return new Response(augmentedMessage, response.getType());
            }
        ).orElse(response);

        historyContext += "\nUser Input: " + userInput + "\nBot Response: " + augmentedResponse.getMessage();

        // Keep only the last 50 lines of history
        String[] lines = historyContext.split("\n", -1);
        if (lines.length > 50) {
            historyContext = String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - 50, lines.length));
        }

        return augmentedResponse;
    }

    // for testing
    public String getResponse(String prompt) {
        return this.client.map(
            c -> {
                GenerateContentResponse response = c.models.generateContent(this.model, prompt, this.config);
                return response.text();
            }
        ).orElse("GeminiProcessor unavailable");
    }
}
