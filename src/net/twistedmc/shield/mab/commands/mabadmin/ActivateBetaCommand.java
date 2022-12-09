package net.twistedmc.shield.mab.commands.mabadmin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Objects;

public class ActivateBetaCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "beta")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (event.getUser().getIdLong() == 478410064919527437L || event.getUser().getIdLong() == 208757906428919808L) {
                String guildID = event.getOption("guildid").getAsString();

                Guild guild = event.getJDA().getGuildById(guildID);

                if (guild == null) {
                    event.deferReply().setContent("**HOLD UP!** There is no valid guild with the ID: `" + guildID + "`!").setEphemeral(true).queue();
                } else {

                    try {
                        if (Main.isBeta(guild.getIdLong())) {
                            event.deferReply().setContent("This guild already has BETA features activated!").queue();
                            return;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    event.deferReply().setContent("Successfully activated BETA features for guild with the ID: `" + guildID + "`!").queue();

                    guild.getOwner().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(
                            "**WOO HOO!** BETA features have been activated for your guild, **" + guild.getName() + "**!").queue());

                    try {
                        Main.activateBeta(guildID);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
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
