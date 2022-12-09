package net.twistedmc.shield.mab.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;
import net.twistedmc.shield.mab.music.PlayerManager;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "play")) {

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

            String url = event.getOption("url").getAsString();

            if (event.getChannelType().isThread() || event.getChannelType().isAudio()) {
                event.reply("**HOLD UP!** This command can only be done in text channels!").setEphemeral(true).queue();
                return;
            }

            if (!Objects.requireNonNull(event.getMember().getVoiceState()).inAudioChannel()) {
                event.reply("**HOLD UP!** You need to be in a voice channel!").setEphemeral(true).queue();
                return;
            }

            if (event.getGuild().getAudioManager().isConnected()) {
                if (event.getGuild().getAudioManager().getConnectedChannel().getIdLong() != event.getMember().getVoiceState().getChannel().getIdLong()) {
                    event.reply("**HOLD UP!** You are not in the same voice channel as me!").setEphemeral(true).queue();
                    return;
                }
            }

            String link = url;

            if (!url.contains("http")) {
                link = "ytsearch:" + link;
            }

            try {
                PlayerManager.getInstance().audioPlayerManager.loadItemOrdered(PlayerManager.getInstance().getMusicManager(event.getGuild()), link, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue(audioTrack, event.getChannel().asTextChannel());

                        EmbedBuilder eb = new EmbedBuilder();

                        eb.setTitle(":musical_note:  MAB Music Player");
                        eb.setColor(new Color(47,49,54));
                        eb.setDescription("Adding to queue: `" + audioTrack.getInfo().title + "` by `" + audioTrack.getInfo().author + "`");
                        eb.addField("Track length:", String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes(audioTrack.getInfo().length),
                                TimeUnit.MILLISECONDS.toSeconds(audioTrack.getInfo().length) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioTrack.getInfo().length))
                        ), true);
                        eb.setFooter(MAB.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        eb.setTimestamp(new Date().toInstant());

                        event.replyEmbeds(eb.build()).queue();

                        final AudioManager audioManager = event.getGuild().getAudioManager();
                        AudioChannel memberChannel = event.getMember().getVoiceState().getChannel();

                        if (!event.getGuild().getAudioManager().isConnected()) {
                            audioManager.openAudioConnection(memberChannel);
                            assert memberChannel != null;
                        }

                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        List<AudioTrack> tracks = audioPlaylist.getTracks();

                        EmbedBuilder eb = new EmbedBuilder();

                        eb.setTitle(":musical_note:  MAB Music Player");
                        eb.setColor(new Color(47,49,54));
                        eb.setDescription("Adding to queue: " + String.valueOf(tracks.size()) + " tracks from playlist `" + audioPlaylist.getName() + "`");
                        eb.setFooter(MAB.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        event.replyEmbeds(eb.build()).queue();

                        TextChannel channel = event.getChannel().asTextChannel();

                        for (final AudioTrack track : tracks) {
                            PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue(track, channel);

                        }

                        final AudioManager audioManager = event.getGuild().getAudioManager();
                        AudioChannel memberChannel = event.getMember().getVoiceState().getChannel();

                        if (!event.getGuild().getAudioManager().isConnected()) {
                            audioManager.openAudioConnection(memberChannel);
                            assert memberChannel != null;
                        }
                    }

                    @Override
                    public void noMatches() {

                    }

                    @Override
                    public void loadFailed(FriendlyException e) {

                    }
                });
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
