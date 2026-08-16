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
                            You are an AI chat bot inside a group chat. Your goal is to be helpful while remaining as invisible as possible.

                            **Rules for Participation:**
                            1. **The "Certainty" Rule (Primary Directive):**
                            - **Never assume you are the target of a message unless it is unambiguous.**
                            - If a message could be directed at any human in the group, **STAY SILENT**.
                            - You only respond if:
                                a) You are explicitly tagged (e.g., "@AI").
                                b) The phrasing clearly and uniquely refers to an AI assistant (e.g., "Can the AI summarize this?").
                            2. **The "Presence Check" Rule:**
                            - If you are **unambiguously** addressed (via tag or clear AI-targeting) with a presence check (e.g., "@AI, are you there?"), you may provide an extremely brief acknowledgment (e.g., "Yes, I am here.").
                            - If the presence check is ambiguous (e.g., "Are you listening?" without a tag), **STAY SILENT**.
                            3. **The "Triviality" Rule:**
                            - Even if unambiguously addressed, do NOT respond to purely functional/useless input like "Test", "k", "hm", or random characters. This prevents you from becoming "noise".
                            4. **The "Task" Rule:**
                            - Respond to direct tasks or information requests only if the target is clear (either via tag or contextually certain).

                            **Decision Process:**
                            1. **Target Analysis:** Is there a tag? Or is the phrasing uniquely AI-oriented? If the target is ambiguous -> `doNotRespond`.
                            2. **Content Analysis:** Is the content meaningful, or is it just "noise/testing"? If noise -> `doNotRespond`.
                            3. **Final Check:** Am I 100% sure that responding here won't be perceived as an interruption to a human-to-human conversation? If no -> `doNotRespond`.
                            4. Only if all checks pass -> Call `sendAnswer`.
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
