package net.twistedmc.shield.mab.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Objects;

public class InfoCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "info")) {

            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            try {
                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("<:squareexclamationred:1058119075789803650> This guild is currently suspended from using the MAB bot due to abuse and/or spamming." +
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
                        event.reply("<:squareexclamationred:1058119075789803650> MAB is currently undergoing maintenance!\n\nFor More Information, click the button below:")
                                .addActionRow(Button.link(Main.getStatusLink("MAB"), "View Status Updates"))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                        .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                                .queue();
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            final long duration = ManagementFactory.getRuntimeMXBean().getUptime();

            final long years = duration / 31104000000L;
            final long months = duration / 2592000000L % 12;
            final long days = duration / 86400000L % 30;
            final long hours = duration / 3600000L % 24;
            final long minutes = duration / 60000L % 60;
            final long seconds = duration / 1000L % 60;
            // final long milliseconds = duration % 1000;

            String uptime = (years == 0 ? "" : "" + years + " years, ") + (months == 0 ? "" : "" + months + " months, ") + (days == 0 ? "" : "" + days + " days, ") + (hours == 0 ? "" : "" + hours + " hours, ")
                    + (minutes == 0 ? "" : "" + minutes + " minutes, ") + (seconds == 0 ? "" : "" + seconds + " seconds, ") /* + (milliseconds == 0 ? "" : milliseconds + " milliseconds, ") */;

            uptime = MAB.replaceLast(uptime, ", ", "");
            uptime = MAB.replaceLast(uptime, ",", " and");

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("MODERATION ASSISTANCE BOT", "https://twistedmcstudios.com/mab/");

            eb.setColor(new Color(47,49,54));

            eb.addField("Stats:", "" + NumberFormat.getInstance().format(MAB.jda.getShardManager().getGuilds().size()) + " Guilds\nShards: " + event.getJDA().getShardInfo().getShardTotal(), true);
            eb.addField("This Shard (" + (event.getJDA().getShardInfo().getShardId() + 1) + "):", "" + NumberFormat.getInstance().format(event.getJDA().getUsers().size()) + " Users\n" + event.getJDA().getGuilds().size() + " Guilds", true);

            eb.addField("Bot Uptime:", uptime, true);

            eb.setFooter("Embed from MAB  •  " + MAB.footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
            eb.setTimestamp(new Date().toInstant());

            event.replyEmbeds(eb.build())
                    .addActionRow(Button.link("https://twistedmcstudios.com/mab/privacy-policy/", "Privacy Policy")
                            .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                    .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/tickets/create/", "Support Ticket")
                            .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                    .addActionRow(Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                            .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                    .queue();
            return;
        }
    }

}
