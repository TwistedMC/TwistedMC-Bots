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
import java.sql.SQLException;
import java.util.Date;

public class HelpCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("help")) {

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

            if (event.getGuild().getIdLong() != 1001147856687742996L) {
                EmbedBuilder help = new EmbedBuilder();
                help.setTitle("MODERATION ASSISTANCE BOT", "https://twistedmcstudios.com/mab/");
                help.setDescription("");
                help.addField("Command List:", "`/help`: Show help menu" +
                                "\n`/mab info`: Show information about MAB" +
                                "\n`/music`: Play music!" +
                                "\n`/mabsettings`: Different settings for your server *(ADMINISTRATOR permission required)*" +
                                "\n`/moderate`: Moderate a user with different options *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/timeout`: Timeout a user *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/searchcase`: View a discord punishment case *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/purge`: Purge messages *(MANAGE MESSAGES permission required)*",
                        false);
                help.setColor(new Color(47,49,54));
                help.setFooter(MAB.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                help.setTimestamp(new Date().toInstant());
                event.replyEmbeds(help.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/privacy-policy/", "Privacy Policy")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/tickets/create/", "Support Ticket")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
                help.clear();
                help = null;

            } else if (event.getGuild().getIdLong() == 1001147856687742996L) {
                EmbedBuilder help = new EmbedBuilder();
                help.setTitle("MODERATION ASSISTANCE BOT", "https://twistedmcstudios.com/mab/");
                help.setDescription("");
                help.addField("Command List:", "`/help`: Show help menu" +
                                "\n`/mab info`: Show information about MAB" +
                                "\n`/music`: Play music!" +
                                "\n`/mabsettings`: Different settings for your server *(ADMINISTRATOR permission required)*" +
                                "\n`/moderate`: Moderate a user with different options *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/timeout`: Timeout a user *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/searchcase`: View a discord punishment case *(TIMEOUT MEMBERS permission required)*" +
                                "\n`/purge`: Purge messages *(MANAGE MESSAGES permission required)*" +
                                "\n:sparkles: `/bwmarketplace embedadvertisement`: Create an embed advertisement",
                        false);
                help.setColor(new Color(47,49,54));
                help.setFooter(MAB.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                help.setTimestamp(new Date().toInstant());
                event.replyEmbeds(help.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/privacy-policy/", "Privacy Policy")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/tickets/create/", "Support Ticket")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .addActionRow(Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
                help.clear();
                help = null;
            }
            return;
        }
    }

}
