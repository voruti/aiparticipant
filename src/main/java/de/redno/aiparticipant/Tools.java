package de.redno.aiparticipant;

import org.springframework.ai.tool.annotation.Tool;

public class Tools {

  @Tool(description = "Send your answer into the chat.", returnDirect = true)
  public static void sendAnswer(final String message) {
    System.out.println("AI is answering: " + message);
  }

  @Tool(
      description = "You can call this, if you *don't* want to send an answer back to the chat.",
      returnDirect = true)
  public static void doNotRespond() {
    System.out.println("AI is exiting");
  }
}
