package de.redno.aiparticipant;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatController {

  private final OllamaChatModel chatModel;

  @Value("${spring.ai.ollama.chat.model:mistral}")
  private String model;

  @Autowired
  public ChatController(OllamaChatModel chatModel) {
    this.chatModel = chatModel;
  }

  public void logic() {
    OllamaChatOptions options =
        OllamaChatOptions.builder()
            .model(model)
            .toolCallbacks(ToolCallbacks.from(new ToolService()))
            .build();

    Prompt prompt =
        new Prompt(
            "What's the current time? (Don't set an alarm.) Answer short and with emojis.",
            options);
    ChatResponse response = this.chatModel.call(prompt);
    int counter = 0;
    System.out.println(++counter + ": " + response);

    ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

    while (response.hasToolCalls()) {
      ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, response);
      prompt = new Prompt(result.conversationHistory(), options);
      response = this.chatModel.call(prompt);
      System.out.println(++counter + ": " + response);
    }

    System.out.println(response.getResult().getOutput().getText());
  }
}
