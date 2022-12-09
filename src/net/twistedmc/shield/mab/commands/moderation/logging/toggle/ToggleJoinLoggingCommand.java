package net.twistedmc.shield.mab.commands.moderation.logging.toggle;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Objects;

public class ToggleJoinLoggingCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "togglejoinlogs")) {

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
                if (!Main.isEnabled("privacypolicy", event.getGuild().getIdLong())) {
                    event.reply("Our privacy policy was recently updated.").addActionRow(Button.link("https://twistedmcstudios.com/mab/privacy-policy/", "View Privacy Policy")).queue();
                    Main.activateSetting("privacypolicy", event.getGuild().getId());
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
                if (Main.isEnabled("joinlogs", event.getGuild().getIdLong())) {
                    Main.disableSetting("joinlogs", event.getGuild().getId());
                    event.reply("Join Logs disabled!").setEphemeral(true)
                            .addActionRow(Button.link("https://twistedmcstudios.com/", "Help Center")
                                    .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                            .addActionRow(Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                    .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                            .queue();
                } else {
                    Main.activateSetting("joinlogs", event.getGuild().getId());
                    event.reply("Join Logs enabled!").setEphemeral(true)
                            .addActionRow(Button.link("https://twistedmcstudios.com/", "Help Center")
                                    .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                            .addActionRow(Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                    .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                            .queue();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            return;
        }
    }

}
