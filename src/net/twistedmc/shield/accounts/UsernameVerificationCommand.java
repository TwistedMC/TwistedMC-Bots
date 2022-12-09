package net.twistedmc.shield.accounts;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.twistedmc.shield.mab.MAB;

import java.awt.*;
import java.util.Calendar;
import java.util.TimeZone;

public class UsernameVerificationCommand extends Command {

    public UsernameVerificationCommand() {
        super("usernameverification"); }

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

        eb.setTitle(":no_entry: USERNAME VERIFICATION", null);

        eb.setColor(new Color(47,49,54));

        eb.setDescription("By connecting your Play.TwistedMC.Net account to your Discord account, your rank and username will be automatically synced and you may even earn in-game rewards!\n\nTo link your accounts, make sure you toggle on " +
                "\"Direct Messages\" in this " +
                "server's Discord " +
                "privacy settings, then " +
                "use the slash command **/link** in this channel. You will receive a DM from me that includes a command along with a code you will need to enter on the Minecraft server. After entering the command on the Minecraft server your " +
                "accounts should be successfully linked!");
        //eb.setDescription("To interact with the Play.TwistedMC.Net Discord server, TwistedMC requires that you verify your Minecraft account.\n\nMake sure your private messages on your Discord account are set to enabled. Type **!link**, and then
        // copy the command from the Accounts bot and login to play.twistedmc.net on Minecraft then execute the command!\n\nIf you have any trouble, let us know in <#912265941277106207>. Please note that we will not manually link your account under any circumstances.");
        eb.setFooter(footer);

        TextChannel textChannel = Accounts.jda.getTextChannelById("797766853723684924");
        textChannel.sendMessageEmbeds(eb.build()).queue();
    }
}









