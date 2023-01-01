package net.twistedmc.shield.mab.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class ModerateCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("moderate")) {
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

            Member member = event.getMember();

            EnumSet<Permission> memPerms = member.getPermissions();
            boolean memCanMute = memPerms.toString().contains(Permission.MODERATE_MEMBERS.name());

            if (!memCanMute) {
                event.reply("<:squareexclamationred:1058119075789803650> You do not have permission to use **/moderate**!").setEphemeral(true).queue();
                return;
            }

            User user = event.getOption("user").getAsUser();

            if (user.isBot()) {
                event.reply("<:squareexclamationred:1058119075789803650> Cannot use on bot!").setEphemeral(true).queue();
                return;
            }

            if (MAB.modConfirmBypass.containsKey(event.getUser().getId())) {
                MAB.modConfirmBypass.remove(event.getUser().getId());
            }
            if (event.getOption("bypass") != null) {
                if (event.getOption("bypass").getAsBoolean()) {
                    MAB.modConfirmBypass.put(event.getUser().getId(),Boolean.TRUE);
                } else {
                    MAB.modConfirmBypass.put(event.getUser().getId(),Boolean.FALSE);
                }
            } else {
                MAB.modConfirmBypass.put(event.getUser().getId(),Boolean.FALSE);
            }
            if (MAB.modMap.containsKey(event.getUser().getId())) {
                MAB.modMap.remove(event.getUser().getId());
            }
            if (MAB.modMapUser.containsKey(event.getUser().getId())) {
                MAB.modMapUser.remove(event.getUser().getId());
            }

            String[] data = {user.getId(),"",""};
            MAB.modMapUser.put(event.getUser().getId(),user);
            MAB.modMap.put(event.getUser().getId(),data);
            MAB.timeoutMember.put(event.getUser(), Objects.requireNonNull(event.getOption("user")).getAsUser());
            MAB.guildID.put(event.getUser(), event.getGuild().getId());
            if (event.getGuild().getIdLong() == 549595806009786388L) {
                SelectMenu menu = SelectMenu.create("menu:modaction")
                        .setPlaceholder("Select Moderation Action Command")
                        .setRequiredRange(1, 1)
                        .addOption("Compromised Account", "mac-ca", "Auto-Ban the user with reason: \"Compromised Account\"")
                        .addOption("Underage User", "mac-uau", "User age < 13 years old. (Req: 13+ according to Discord TOS)")
                        .addOption("Warn", "mac-w", "Warn the user. Sends them a DM with the reason.")
                        .addOption("Kick", "mac-k", "Kicks the user.")
                        .addOption("Ban", "mac-b", "Ban the user.")
                        .addOption("Virtual Ban", "mac-vb", "Blocks user from interacting with the discord")
                        .addOption("Timeout", "mac-t", "Timeout the user.")
                        .addOption("Cancel Moderation", "mac-cancel", "Cancel the current moderation command.")
                        .build();

                EmbedBuilder emb = new EmbedBuilder();
                emb.setColor(new Color(253, 216, 1));
                emb.setDescription("**Please select the moderation action command!**");
                emb.setTimestamp(new Date().toInstant());
                emb.setFooter("Embed from MAB  •  " + MAB.footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
            } else if (event.getGuild().getIdLong() != 549595806009786388L) {
                SelectMenu menu = SelectMenu.create("menu:modaction")
                        .setPlaceholder("Select Moderation Action Command")
                        .setRequiredRange(1, 1)
                        .addOption("Compromised Account", "mac-ca", "Auto-Ban the user with reason: \"Compromised Account\"")
                        .addOption("Underage User", "mac-uau", "User age < 13 years old. (Req: 13+ according to Discord TOS)")
                        .addOption("Warn", "mac-w", "Warn the user. Sends them a DM with the reason.")
                        .addOption("Kick", "mac-k", "Kicks the user.")
                        .addOption("Ban", "mac-b", "Ban the user.")
                        .addOption("Timeout", "mac-t", "Timeout the user.")
                        .addOption("Cancel Moderation", "mac-cancel", "Cancel the current moderation command.")
                        .build();

                EmbedBuilder emb = new EmbedBuilder();
                emb.setColor(new Color(253, 216, 1));
                emb.setDescription("**Please select the moderation action command!**");
                emb.setTimestamp(new Date().toInstant());
                emb.setFooter("Embed from MAB  •  " + MAB.footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
            }
        }
    }

}
