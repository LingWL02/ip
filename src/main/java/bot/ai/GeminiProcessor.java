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

    private final static String API_KEY = System.getenv("GEMINI_API_KEY");

    private final String model = "gemini-2.5-flash";

    private String historyContext = "";

    private final Optional<Client> client = (API_KEY != null && !API_KEY.isBlank())
        ? Optional.of(Client.builder().apiKey(API_KEY).build())
        : Optional.empty();

    private final GenerateContentConfig config;

    public GeminiProcessor(String systemPrompt) {
        config = GenerateContentConfig.builder()
            .systemInstruction(
                Content.fromParts(Part.fromText(systemPrompt))
            )
            .build();
    }


    public Response augmentResponse(String userInput, Response response) {
        String history = historyContext.isBlank()
            ? ""
            : """
            CONVERSATION HISTORY (for context only — use it to maintain continuity, \
            refer back when relevant, and avoid repeating yourself):
            %s

            """.formatted(historyContext);

        String prompt = response.getType() == Response.Type.ERROR
            ? """
            %sThe user typed something that isn't a recognized command: "%s"
            Determine their intent:
            - If they seem to be asking for help, a command list, or what the bot can do \
            (e.g. "help", "what can you do", "commands", "how do I..."), respond by walking \
            them through the available commands in your persona — list each command with its \
            syntax and a short example. Be their guide, not a bouncer.
            - If it looks like a typo or near-miss of a real command, call it out and show \
            the correct syntax with an example.
            - If it's genuinely off-topic or nonsensical, acknowledge it briefly in character \
            and nudge them back toward their tasks.
            Keep the response concise and in persona. Do not use markdown.
            """.formatted(history, userInput)
            : """
            %sRewrite the following bot response in your persona. \
            Preserve all key information exactly — do not add, remove, or change any facts, \
            command syntax, task names, dates, or indices. \
            User Input: %s \
            Response type: %s \
            Original message: %s
            """.formatted(history, userInput, response.getType().name(), response.getMessage());

        Response augmentedResponse = this.client.map(
            c -> {
                GenerateContentResponse genResponse = c.models.generateContent(this.model, prompt, this.config);
                String augmentedMessage = genResponse.text();
                return new Response(augmentedMessage, response.getType());
            }
        ).orElse(response);

        historyContext += "\nUser Input: " + userInput
            + "\nBot Response Type: " + response.getType().name()
            + "\nBot Response: " + augmentedResponse.getMessage();

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
