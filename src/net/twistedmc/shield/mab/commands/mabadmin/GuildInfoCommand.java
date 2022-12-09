package net.twistedmc.shield.mab.commands.mabadmin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
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
import java.util.concurrent.TimeUnit;

public class GuildInfoCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "guildinfo")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (event.getUser().getIdLong() == 478410064919527437L || event.getUser().getIdLong() == 208757906428919808L) {

                String guildID = event.getOption("guildid").getAsString();

                Guild guild = MAB.shardManager.getGuildById(guildID);

                assert guild != null;
                OffsetDateTime dt = guild.getTimeCreated();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ssa 'ET'");

                EmbedBuilder eb = new EmbedBuilder();

                eb.setTitle(guild.getName() + " Info", null);

                eb.setColor(new Color(52, 152, 219));

                eb.addField("Owner:", guild.getOwner().getUser().getAsTag() + "\n" + guild.getOwner().getAsMention(), true);

                try {
                    eb.addField("Invite URL:", guild.retrieveInvites().complete().get(0).getUrl(), true);
                } catch (InsufficientPermissionException e) {
                    event.reply("**HOLD UP!** MAB does not have MANAGE_SERVER Permission in this Guild!").setEphemeral(true)
                            .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("sendmanageserverdm", "Notify Guild Owner Missing Permission"))
                            .queue();
                    return;
                }

                eb.addField("Date Created:", fmt.format(dt), true);

                eb.addField("Member Count:", NumberFormat.getInstance().format(guild.getMemberCount()), true);

                try {
                    if (Main.isBeta(Long.parseLong(guildID))) {
                        eb.addField("Beta Features:", "Yes", true);
                    } else if (!Main.isBeta(Long.parseLong(guildID))) {
                        eb.addField("Beta Features:", "No", true);
                    }

                    if (Main.isBanned(Long.parseLong(guildID))) {
                        eb.addField("Banned:", "Yes", true);
                    } else if (!Main.isBanned(Long.parseLong(guildID))) {
                        eb.addField("Banned:", "No", true);
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }


                eb.setFooter(MAB.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                eb.setTimestamp(new Date().toInstant());

                event.replyEmbeds(eb.build()).queue();


            } else {
                event.reply("**HOLD UP!** You do not have permission to do this!").queue();
                return;
            }
            return;
        }
    }

}
