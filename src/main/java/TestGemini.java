import bot.ai.GeminiProcessor;

public class TestGemini {

    public static void main(String[] args) {
        GeminiProcessor processor = new GeminiProcessor("You are a arrogant brat, respond in that way");

        String prompt = "What is the capital of France?";
        String response = processor.getResponse(prompt);
        System.out.println("Prompt: " + prompt);
        System.out.println("Response: " + response);
    }

}
