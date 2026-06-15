package de.redno.aiparticipant;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiscordService extends ListenerAdapter {

  /** TODO: WIP: locks the bot down to only using a single channel at a time */
  @Nullable private static MessageChannel channel;

  private final AiService aiService;

  @Value("${discord.message_history_size:20}")
  private int messageHistorySize;

  @Autowired
  public DiscordService(AiService aiService, @Value("${discord.bot_token}") String botToken) {
    this.aiService = aiService;

    JDABuilder.createLight(
            botToken, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
        .addEventListeners(this)
        .build();
  }

  public static void sendMessage(final String message) {
    channel.sendMessage(message).queue();
  }

  private static List<org.springframework.ai.chat.messages.Message> convertToAiMessages(
      final MessageHistory history, final Message message) {
    return Stream.<org.springframework.ai.chat.messages.Message>concat(
            history.getRetrievedHistory().stream()
                .map(
                    discordMessage -> {
                      if (discordMessage.getAuthor().isBot()) {
                        return new AssistantMessage(discordMessage.getContentDisplay());
                      } else {
                        return convertToUserMessage(discordMessage);
                      }
                    })
                .toList()
                .reversed()
                .stream(),
            Stream.of(convertToUserMessage(message)))
        .toList();
  }

  private static UserMessage convertToUserMessage(final Message message) {
    return UserMessage.builder()
        .text(
            String.format(
                "[%s] %s: %s",
                message.getTimeCreated(),
                message.getAuthor().getEffectiveName(),
                message.getContentDisplay()))
        .metadata(
            Map.of(
                "timestamp", message.getTimeCreated(),
                "guild", message.getGuild().getName(),
                "channel", message.getChannel().getName(),
                "author", message.getAuthor().getEffectiveName()))
        .build();
  }

  @Override
  public void onMessageReceived(final MessageReceivedEvent event) {
    // We don't want to respond to other bot accounts, including ourselves:
    if (event.getAuthor().isBot()) return;

    final Message message = event.getMessage();
    final MessageChannel channel = event.getChannel();

    channel
        .getHistoryBefore(message, this.messageHistorySize)
        .queue(
            messages -> {
              DiscordService.channel = channel;

              aiService.handle(DiscordService.convertToAiMessages(messages, message));
            });
  }
}
