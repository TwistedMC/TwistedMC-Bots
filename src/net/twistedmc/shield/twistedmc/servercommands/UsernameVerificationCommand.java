package net.twistedmc.shield.twistedmc.servercommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.twistedmc.TwistedMC;

import java.awt.*;

public class UsernameVerificationCommand extends Command {

    public UsernameVerificationCommand() {
        super("usernameverification"); }


    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rank.admin")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to do that.");
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(":no_entry: USERNAME VERIFICATION", null);

        eb.setColor(new Color(0, 148, 255));

        eb.setDescription("To interact with the Play.TwistedMC.Net Discord server, TwistedMC requires that you verify your Minecraft account.\n\nMake sure your private messages on your Discord account are set to enabled. Type **!link**, and then copy the command from the Accounts bot and login to play.twistedmc.net on Minecraft then execute the command!\n\nIf you have any trouble, let us know in <#912265941277106207>. Please note that we will not manually link your account under any circumstances.");

        TextChannel textChannel = TwistedMC.jda.getTextChannelById("797766853723684924");
        textChannel.sendMessage((CharSequence) eb.build()).queue();
    }
}









