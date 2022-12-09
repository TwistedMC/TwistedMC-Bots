package net.twistedmc.shield.mab.commands.mabadmin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ShutdownCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "shutdown")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (event.getUser().getIdLong() == 478410064919527437L || event.getUser().getIdLong() == 208757906428919808L) {

                event.reply("Shutting down MAB..").queue();

                ProxyServer.getInstance().getScheduler().schedule(BungeeCord.getInstance().getPluginManager().getPlugin("SHIELD"), new Runnable() {
                    public void run() {
                        event.getChannel().asTextChannel().sendMessage("Shutdown complete!").queue();

                        ProxyServer.getInstance().getScheduler().schedule(BungeeCord.getInstance().getPluginManager().getPlugin("SHIELD"), new Runnable() {
                            public void run() {
                                MAB.stop();
                            }
                        }, 10, TimeUnit.MILLISECONDS);
                    }
                }, 1, TimeUnit.SECONDS);

            } else {
                event.reply("**HOLD UP!** You do not have permission to do this!").queue();
                return;
            }
            return;
        }
    }

}
