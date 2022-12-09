package net.twistedmc.shield.mab.bungeecommands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.mab.MAB;

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

        MAB.stop();
    }
}









