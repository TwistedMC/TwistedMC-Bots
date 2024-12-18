package net.twistedmc.shield.mab.music.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.twistedmc.shield.Main;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Objects;

public class JoinCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "join")) {

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

            final GuildVoiceState memberVoiceState = event.getMember().getVoiceState();

            assert memberVoiceState != null;

            if (event.getChannelType().isThread() || event.getChannelType().isAudio()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in text channels!").setEphemeral(true).queue();
                return;
            }

            if (!Objects.requireNonNull(event.getMember().getVoiceState()).inAudioChannel()) {
                event.reply("<:squareexclamationred:1058119075789803650> You need to be in a voice channel!").setEphemeral(true).queue();
                return;
            }

            if (event.getGuild().getAudioManager().isConnected()) {
                event.reply("<:squareexclamationred:1058119075789803650> I'm already in a voice channel!").setEphemeral(true).queue();
                return;
            }

            try {
                final AudioManager audioManager = event.getGuild().getAudioManager();
                AudioChannel memberChannel = memberVoiceState.getChannel();

                audioManager.openAudioConnection(memberChannel);
                assert memberChannel != null;

                event.reply(":musical_note:  Connecting to " + memberChannel.getName() + "...").queue();
            } catch (NullPointerException e) {
                event.reply("**ERROR!** An error occurred. Please report this to us! (" + e.getCause() + ")")
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/", "Help Center")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } catch (InsufficientPermissionException e) {
                event.reply("**ERROR!** Insufficient Permissions for channel " + e.getChannel(event.getJDA()).getAsMention() + "! Missing Permission: " + e.getPermission()).queue();
            }

        }


    }


}
