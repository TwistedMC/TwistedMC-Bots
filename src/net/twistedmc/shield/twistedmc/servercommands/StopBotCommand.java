package net.twistedmc.shield.twistedmc.servercommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.twistedmc.TwistedMC;

import java.awt.*;
import java.util.Calendar;
import java.util.TimeZone;

public class StopBotCommand extends Command {

    public StopBotCommand() {
        super("stopbot"); }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rank.admin")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to do that.");
            return;
        }

        TwistedMC.stop();
    }
}









