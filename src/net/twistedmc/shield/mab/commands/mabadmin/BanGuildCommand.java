package net.twistedmc.shield.mab.commands.mabadmin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class BanGuildCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "banguild")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (event.getUser().getIdLong() == 478410064919527437L || event.getUser().getIdLong() == 208757906428919808L) {

                String guildID = event.getOption("guildid").getAsString();
                String reason = event.getOption("reason").getAsString();

                Guild guild = event.getJDA().getGuildById(guildID);

                if (guild == null) {
                    event.deferReply().setContent("**HOLD UP!** There is no valid guild with the ID: `" + guildID + "`!").setEphemeral(true).queue();
                } else {
                    event.deferReply().setContent("<:squarexmarksolid:1057753638329663598> Banned guild with the ID: `" + guildID + "` from using MAB!").queue();

                    try {
                        Objects.requireNonNull(guild.getDefaultChannel()).asTextChannel().sendMessage(Objects.requireNonNull(guild.getOwner()).getAsMention() + ", Your guild, **" + guild.getName() + "** has been suspended from using the MAB bot due to " +
                                        "abuse and/or spamming." +
                                        "\n\nIf you believe this was done in error, create a ticket using the button below:")
                                .setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                                .queue();
                    } catch (InsufficientPermissionException e) {
                        e.printStackTrace();
                    }

                    guild.getOwner().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(
                                    "Your guild, **" + guild.getName() + "** has been suspended from using the MAB bot due to abuse and/or spamming." +
                                            "\n\nIf you believe this was done in error, create a ticket using the button below:")
                            .setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                            .queue());

                    try {
                        Main.insertBan(guildID, guild.getOwnerId(), reason);
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                }
            } else {
                event.reply("**HOLD UP!** You do not have permission to do this!").queue();
                return;
            }
            return;
        }
    }

}
