package de.redno.aiparticipant;

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
  public CommandLineRunner run(ChatController chatController) {
    return args -> {
      chatController.logic();
    };
  }
}
