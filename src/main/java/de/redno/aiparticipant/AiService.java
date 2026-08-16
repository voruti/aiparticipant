package de.redno.aiparticipant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

  private final OllamaChatModel chatModel;
  private final OllamaChatOptions.Builder options;

  @Autowired
  public AiService(
      OllamaChatModel chatModel, @Value("${spring.ai.ollama.chat.model:mistral}") String model) {
    this.chatModel = chatModel;
    this.options =
        OllamaChatOptions.builder().model(model).toolCallbacks(ToolCallbacks.from(new Tools()));
  }

  public void handle(Message... messages) {
    handle(Arrays.stream(messages).toList());
  }

  public void handle(List<Message> messages) {
    if (!messages.stream()
        .allMatch(
            message ->
                message.getMessageType() == MessageType.USER
                    || message.getMessageType() == MessageType.ASSISTANT))
      throw new IllegalArgumentException("Only user and assistant messages are allowed");

    // var r =
    ChatClient.create(chatModel)
        .prompt()
        .messages(
            Stream.concat(
                    messages.stream(),
                    Stream.of(
                        new SystemMessage(
                            """
                            You are an AI chat bot inside a group chat. Your primary goal is to remain invisible and avoid being perceived as "noise" or "spam".

                            **Rules for Participation:**
                            1. **Strict Silence:** Do NOT respond to greetings (e.g., "Hi", "Hello"), small talk, single-word messages (e.g., "Test", "Lol"), or messages that do not contain a clear question or task directed at you.
                            2. **When to Respond:** Only participate if:
                               - You are explicitly tagged (e.g., "@AI") with a specific question or command.
                               - A user asks a direct question that requires factual information.
                               - There is a significant factual error in the chat that *requires* a correction to maintain the integrity of the conversation.
                            3. **No Self-Validation:** Do NOT confirm your own operational status (e.g., do not reply to "Are you online?" or "Test" with "I am online" or "Success") unless specifically instructed to do so.
                            4. **Minimalism:** If you must respond, be extremely brief. Avoid unnecessary chatter or conversational filler.

                            **Decision Process:**
                            1. Formulate an answer in the user's language.
                            2. Critically evaluate: "Does this message add real value to the group, or am I just adding noise?"
                            3. If the message is just a test, a greeting, or trivial chatter -> call `doNotRespond`.
                            4. Only if it adds value -> call `sendAnswer`.
                            """)))
                .toList())
        .options(this.options)
        .call()
        .chatClientResponse();

    /*
    Optional.of(r)
        .map(ChatClientResponse::chatResponse)
        .map(ChatResponse::getResult)
        .map(Generation::getOutput)
        .map(AbstractMessage::getMetadata)
        .map(metadata -> metadata.get("thinking"))
        .ifPresent(System.out::println);
    */
  }
}
