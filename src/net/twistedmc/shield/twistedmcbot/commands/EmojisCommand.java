package net.twistedmc.shield.twistedmcbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.accounts.SyncBot;
import net.twistedmc.shield.mab.MAB;
import net.twistedmc.shield.twistedmcbot.TwistedMCBot;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Objects;

public class EmojisCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equals("emojis")) {

            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("Emoji List");

            eb.setColor(new Color(47,49,54));

            for (Emoji emoji : Objects.requireNonNull(event.getJDA().getGuildById("1057564109991854161")).getEmojiCache()) {
                eb.addField(emoji.getFormatted(), "`" + emoji.getFormatted() + "`", true);
            }

            eb.setFooter(TwistedMCBot.footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            eb.setTimestamp(new Date().toInstant());

            event.replyEmbeds(eb.build()).queue();
        }
    }

}
