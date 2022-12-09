package net.twistedmc.shield.mab.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;

public class TimeoutCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("timeout")) {

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
                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && Main.logChannelSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("**HOLD UP!** Appeal URL is not set! An admin must set it with `/mabsettings`! Appeal URL can be toggled on/off with `/mabsettings toggleappeal`!").setEphemeral(true).queue();
                    return;
                }

                if (!Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) ||
                        Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("**HOLD UP!** Mod Log Channel is not set! An admin must set it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("**HOLD UP!** Mod Log Channel & Appeal URL are not set! An admin must set them with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && !MAB.isUrl(Main.getAppealLink(String.valueOf(event.getGuild().getIdLong())))) {
                    event.deferReply().setContent("**HOLD UP!** Your Appeal URL is not a URL! Please fix it with `/mabsettings`!").setEphemeral(true).queue();
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
                event.reply("**ERROR!** You do not have permission to use **/timeout**!").setEphemeral(true).queue();
                return;
            }

            if (Objects.requireNonNull(event.getOption("user")).getAsUser().isBot()) {
                event.reply("**ERROR!** Cannot use on bot!").setEphemeral(true).queue();
                return;
            }

            MAB.timeoutMember.put(event.getUser(), Objects.requireNonNull(event.getOption("user")).getAsUser());
            MAB.guildID.put(event.getUser(), event.getGuild().getId());

            TextInput reason = TextInput.create("timeout:reason","Reason for Timeout", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Insert reason for moderating this user")
                    .setRequiredRange(2,2000)
                    .build();
            TextInput duration = TextInput.create("timeout:duration","Duration of Timeout", TextInputStyle.SHORT)
                    .setPlaceholder("Insert timeout duration (Max 28 days)")
                    .setRequiredRange(1, 10)
                    .build();
            TextInput timeunit = TextInput.create("timeout:timeunit","TimeUnit.valueOf() Unit.", TextInputStyle.SHORT)
                    .setPlaceholder("Units: DAYS | HOURS | MINUTES | SECONDS")
                    .setRequiredRange(1, 50)
                    .build();

            Modal m = Modal.create("timeout:timeout","Timeout a user")
                    .addActionRows(ActionRow.of(reason),ActionRow.of(duration),ActionRow.of(timeunit))
                    .build();

            event.replyModal(m).queue();
            return;
        }
    }

}
