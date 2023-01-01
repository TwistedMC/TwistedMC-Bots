package net.twistedmc.shield.mab.commands.mabadmin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.mab.MAB;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class EmbedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "embed")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            String title = event.getOption("title").getAsString();
            String desc = event.getOption("desc").getAsString();

            TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle(title, null);

            eb.setColor(new Color(47,49,54));

            eb.setDescription(desc);
            eb.setFooter("Embed from MAB  •  " + "© " + MAB.year + " TwistedMC Studios", "https://cdn.discordapp.com/emojis/1058317602050551838.png");

            channel.sendMessageEmbeds(eb.build()).setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request")).queue();

            event.reply("<:squarexmarksolid:1057753638329663598> Embed created!").setEphemeral(true).queue();
        }
    }

}
