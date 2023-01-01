package net.twistedmc.shield.twistedmcbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Objects;

public class LinkCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equalsIgnoreCase("link")) {

            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            if (!Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.reply("<:squareexclamationred:1058119075789803650> This command cannot be used in this guild!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.deferReply().setContent("<:sharefromsquareregular:1057956937871933480> Syncing your data, this may take a few minutes...").setEphemeral(true).queue();
                event.getHook().sendMessage("<:squarexmarksolid:1057753638329663598> Failed to sync your data.").setEphemeral(true).queue();
            }

        }
    }

}
