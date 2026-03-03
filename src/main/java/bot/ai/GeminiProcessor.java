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

    private final Optional<Client> client = (API_KEY != null && !API_KEY.isBlank())
        ? Optional.of(new Client())
        : Optional.empty();

    private final GenerateContentConfig config;

    public GeminiProcessor(String systemPrompt) {
        config = GenerateContentConfig.builder()
            .systemInstruction(
                Content.fromParts(Part.fromText(systemPrompt))
            )
            .build();
    }


    public Response augmentResponse(Response response) {
        return response;
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
