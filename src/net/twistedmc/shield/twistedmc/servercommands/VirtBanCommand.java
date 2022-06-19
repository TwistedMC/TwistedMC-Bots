package net.twistedmc.shield.twistedmc.servercommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.twistedmc.TwistedMC;

import java.awt.*;
import java.util.Calendar;
import java.util.TimeZone;

public class VirtBanCommand extends Command {

    public VirtBanCommand() {
        super("sendvirtban"); }

    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    int year = calendar.get(Calendar.YEAR);
    String footer = "© " + year + " TwistedMC Studios";

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rank.admin")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to do that.");
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Uh oh. Looks like you've been virtually banned.", null);

        eb.setColor(new Color(35, 35, 35));

        eb.setDescription("Sorry about that, maybe you can make an appeal. Otherwise, you'll no longer be able to participate in the TwistedMC Discord server.");
        eb.setFooter(footer);

        TextChannel textChannel = TwistedMC.jda.getTextChannelById("837173424644161567");
        textChannel.sendMessageEmbeds(eb.build()).setActionRow(Button.link("https://twistedmc.net/tickets/create?ticket_form_id=6&punishment_type=3", "Submit a request")).queue();
    }
}









