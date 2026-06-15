package de.redno.aiparticipant;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AiparticipantApplication {

  static void main(String[] args) {
    SpringApplication.run(AiparticipantApplication.class, args);
  }

  @Bean
  public CommandLineRunner run(AiService aiService) {
    return _ ->
        aiService.handle(
            new UserMessage("A: Hi"),
            new UserMessage("B: Hi, how are you?"),
            new UserMessage("A: Fine."),
            new UserMessage("C: Hi, im also doing fine"));
  }
}
