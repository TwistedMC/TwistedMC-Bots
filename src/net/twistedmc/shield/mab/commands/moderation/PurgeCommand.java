package net.twistedmc.shield.mab.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class PurgeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("purge")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            try {
                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("**HOLD UP!** This guild is currently suspended from using the MAB bot due to abuse and/or spamming." +
                                    "\n\nIf you believe this was done in error, create a ticket using the button below:")
                            .addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                            .queue();
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (Main.isMaintenance("MAB")) {
                    try {
                        event.reply("**HOLD UP!** This bot is currently under maintenance!\n\nFor More Information, click the button below:")
                                .addActionRow(Button.link(Main.getStatusLink("MAB"), "View Status Updates"))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                        .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                                .queue();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (!Main.logChannelSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("**HOLD UP!** Mod Log Channel is not set! An admin must set it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            OffsetDateTime fiveWeeksAgo = OffsetDateTime.now().minus(5, ChronoUnit.WEEKS);

            int num_messages = (int) event.getOption("number").getAsLong();

            if (num_messages <= 100 && num_messages >= 5) {
                List<Message> messages = event.getChannel().getHistory().retrievePast(num_messages).complete();

                messages.removeIf(m -> m.getTimeCreated().isBefore(fiveWeeksAgo));

                event.getGuild().getTextChannelById(event.getChannel().getId()).deleteMessages(messages).complete();
                event.deferReply().setContent(":wastebasket:  Deleted `" + num_messages + "` messages!").setEphemeral(true).queue();

                try {
                    event.getGuild().getTextChannelById(Main.getLogChannel(String.valueOf(event.getGuild().getIdLong()))).sendMessage(":wastebasket:  Deleted `" + num_messages
                            + "` messages in " + event.getGuild().getTextChannelById(event.getChannel().getId()).getAsMention()
                            + " by " + event.getMember().getAsMention()).queue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                event.deferReply().setContent("**ERROR!** " + num_messages + " is not within the range of `5-100`!").setEphemeral(true).queue();
            }

            return;
        }
    }

}
