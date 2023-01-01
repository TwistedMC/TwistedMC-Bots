package net.twistedmc.shield.mab.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class SearchCaseCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("searchcase")) {

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
                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && Main.logChannelSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Appeal URL is not set! An admin must set it with `/mabsettings`! Appeal URL can be toggled on/off with `/mabsettings toggleappeal`!").setEphemeral(true).queue();
                    return;
                }

                if (!Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) ||
                        Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Mod Log Channel is not set! An admin must set it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Mod Log Channel & Appeal URL are not set! An admin must set them with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && !MAB.isUrl(Main.getAppealLink(String.valueOf(event.getGuild().getIdLong())))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Your Appeal URL is not a URL! Please fix it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            String id = event.getOption("caseid").getAsString();

            event.deferReply(true).queue(hook -> {
                try {
                    if (Main.caseExists(id, event.getGuild().getId())) {
                        MySQL MySQL_rs = new MySQL(Main.sqlHostDM, Main.sqlPortDM, Main.sqlDbDM, Main.sqlUserDM, Main.sqlPwDM);
                        Statement statement1_rs = MySQL_rs.openConnection().createStatement();
                        ResultSet resultSet = statement1_rs.executeQuery("SELECT * FROM `discord_punishments` WHERE `caseID`='" + id + "' AND `guildID`='" + event.getGuild().getId() + "'");
                        try {
                            while (resultSet.next()) {
                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setFooter("Embed from MAB  •  " + MAB.footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                                embedBuilder.setTimestamp(new Date().toInstant());
                                embedBuilder.setColor(new Color(115, 192, 195));
                                embedBuilder.setTitle("Viewing Case: " + id);
                                embedBuilder.addField("User", resultSet.getString("user"), true);
                                embedBuilder.addField("Moderator", resultSet.getString("moderator"), true);
                                embedBuilder.addBlankField(true);
                                embedBuilder.addField("Action", resultSet.getString("action"), true);
                                embedBuilder.addField("Timestamp", resultSet.getString("timestamp"), true);
                                embedBuilder.addField("Reason", resultSet.getString("reason"), false);
                                hook.editOriginalEmbeds().setEmbeds(embedBuilder.build()).queue();
                                embedBuilder.clear();
                                embedBuilder = null;
                                return;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            resultSet.close();
                            statement1_rs.close();
                            MySQL_rs.getConnection().close();
                        }
                    } else {
                        EmbedBuilder emb = new EmbedBuilder();
                        emb.setColor(new Color(255, 89, 89));
                        emb.setDescription("Case with ID `" + id + "` could not be found!");
                        emb.setTimestamp(new Date().toInstant());
                        emb.setFooter("Embed from MAB  •  " + MAB.footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                        hook.editOriginalEmbeds().setEmbeds(emb.build()).queue();
                        emb.clear();
                        emb = null;
                        return;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    event.reply("**ERROR!** Something went wrong!").setEphemeral(true).queue();
                }
            });
        }
    }

}
