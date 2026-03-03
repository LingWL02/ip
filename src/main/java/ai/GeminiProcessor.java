package ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.ThinkingLevel;

import bot.response.Response;

/**
 * Wraps the Google Gemini API for generating text responses.
 */
public class GeminiProcessor {

    private final String model = "gemini-2.5-flash";

    private final Client client = new Client();

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
        GenerateContentResponse response =
        this.client.models.generateContent(this.model, "Hello there", this.config);

        return response.text();
    }
}
