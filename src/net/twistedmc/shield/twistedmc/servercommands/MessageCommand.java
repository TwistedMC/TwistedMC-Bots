package net.twistedmc.shield.twistedmc.servercommands;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.twistedmc.TwistedMC;

public class MessageCommand extends Command {

    public MessageCommand() {
        super("msgd"); }


    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("rank.admin")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to do that.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /msgd <player> <message...>");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int str = 1; str < args.length; str++) {
            builder.append(args[str]).append(" ");
        }
        String message = builder.toString();
        message = message.replaceAll("NEWLINE", "\n");

        String userId = args[0];

        User user = TwistedMC.getJDA().retrieveUserById(userId).complete();

        TwistedMC.sendMessage(user, message);

        sender.sendMessage(ChatColor.GREEN + "Successfully sent message to " + ChatColor.GREEN + ChatColor.BOLD + args[0] + ChatColor.GREEN + ".");

        TextChannel textChannel = TwistedMC.getJDA().getTextChannelById("859703083038801920");
        if (textChannel != null) {
            textChannel.sendMessage("User: " + user.getName() + "\nMessage sent: " + message).queue();
        }


    }
}
