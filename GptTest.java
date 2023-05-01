package gradleProject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GptTest {
	public record ChatGptRequest(String model, String prompt, int temperature, int max_tokens) {
	}

	public record ChatGptResponse(String id, String object, int created, String model, ChatGptResponseChoice[] choices,
			ChatGptResponseUsage usage) {
	}

	public record ChatGptResponseChoice(String text, int index, Object logprobs, String finish_reason) {
	}

	public record ChatGptResponseUsage(int prompt_tokens, int completion_tokens, int total_tokens) {
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String prompt;
		if (args.length > 0) {
			prompt = args[0];
		} else {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter a string to search for: ");
			prompt = scanner.nextLine();
		}

		ObjectMapper mapper = new ObjectMapper();
		ChatGptRequest chatGptRequest = new ChatGptRequest("text-davinci-001", prompt, 1, 100);
		String input = mapper.writeValueAsString(chatGptRequest);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.openai.com/v1/completions"))
				.header("Content-Type", "application/json").header("Authorization", "Bearer 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")
				.POST(HttpRequest.BodyPublishers.ofString(input)).build();

		HttpClient client = HttpClient.newHttpClient();
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			ChatGptResponse chatGptResponse = mapper.readValue(response.body(), ChatGptResponse.class);
			String answer = chatGptResponse.choices()[chatGptResponse.choices().length - 1].text();
			if (!answer.isEmpty()) {
				System.out.println(answer.replace("\n", "").trim());
			}
		} else {
			System.out.println(response.statusCode());
			System.out.println(response.body());
		}
	}
}