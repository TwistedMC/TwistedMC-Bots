package net.twistedmc.shield.mab.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;
import net.twistedmc.shield.mab.music.GuildMusicManager;
import net.twistedmc.shield.mab.music.PlayerManager;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "queue")) {

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

            final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
            BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;

            if (queue.isEmpty()) {
                event.reply(":frowning2:  Looks like the music queue is empty!").setEphemeral(true).queue();
                return;
            }

            try {

                final int trackCount = Math.min(queue.size(), 20);
                final List<AudioTrack> trackList = new ArrayList<>(queue);

                EmbedBuilder eb = new EmbedBuilder();

                StringBuilder end = new StringBuilder("");

                for (int i = 0; i < trackCount; i++) {

                    final AudioTrack track = trackList.get(i);
                    final AudioTrackInfo info = track.getInfo();

                    end.append("\n").append("`#" + String.valueOf(i + 1) + "` ```" + track.getInfo().title + "``` `(" + String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length), TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length))) + ")`");
                }

                if (trackList.size() > trackCount) {
                    eb.setFooter("Embed from MAB  •  " + "And " + (String.valueOf(trackList.size() - trackCount) + " more..."), "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    eb.setTimestamp(new Date().toInstant());
                } else {
                    eb.setFooter("Embed from MAB", "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    eb.setTimestamp(new Date().toInstant());
                }

                eb.setTitle(":musical_note:  MAB Music Player");
                eb.setColor(new Color(47,49,54));
                eb.setDescription(queue.isEmpty() ? "No Song in the Queue" : (end.length() > 4096 ? "Longer than 4096 characters" : "Queue: " + end));

                event.deferReply().addEmbeds(eb.build()).queue();
            } catch (NullPointerException e) {
                event.reply("**ERROR!** An error occurred. Please report this to us! (" + e.getCause() + ")")
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/tickets/create/", "Support Ticket")
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
