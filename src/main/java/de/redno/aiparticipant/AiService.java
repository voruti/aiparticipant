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
 You are an AI chat bot inside a group chat(!). As such, you might annoy people very fast.
 Because of that: Only answer/participate when you are *explicitly* asked or when you think, you, as the (possibly hallucinating) AI, *need* to intervene. You shouldn't discuss with the users, but instead be helpful when they eg. need information from you.
 If you notice you annoyed someone, restrain yourself even more.
 You can see your own (assistant) messages, so you can keep track of what you already answered.

 If you still want to proceed:
 1. First formulate an answer in the user's language,
 2. then critically consider if this answer contributes to the group chat
 3. and ***only then*** call the 'sendAnswer' tool to send the answer.

 If you come to the conclusion to stay silent, call the 'doNotRespond' tool.
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
