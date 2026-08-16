package de.redno.aiparticipant;

import org.springframework.ai.tool.annotation.Tool;

public class Tools {

  @Tool(description = "Use this tool ONLY when you have explicitly verified that your response provides essential information, answers a direct question, or corrects a factual error. Do NOT use this for greetings, acknowledgments, testing purposes, or simple conversation fillers. Use it only to deliver meaningful content.", returnDirect = true)
  public static void sendAnswer(final String message) {
    System.out.println("AI is answering: " + message);
    DiscordService.sendMessage(message);
  }

  @Tool(
      description = "Use this tool as your default action whenever the chat contains only greetings, small talk, tests, or messages that do not require an AI intervention. Use this to maintain a low profile and avoid being perceived as noise in the group chat.",
      returnDirect = true)
  public static void doNotRespond() {
    System.out.println("AI is exiting");
  }
}
