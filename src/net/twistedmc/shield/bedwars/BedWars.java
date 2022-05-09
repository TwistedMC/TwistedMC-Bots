package net.twistedmc.shield.bedwars;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class BedWars extends ListenerAdapter {

    private String token;
    private JDA jda;

    public static HashMap<String, Long> userCooldown = new HashMap<String, Long>();
    public static HashMap<String, Long> guildCooldown = new HashMap<String, Long>();

    public BedWars(String token) {
        this.token = token;
    }

    public void start() {
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(this);
            jda.getPresence().setPresence(Activity.playing("BedWars.TwistedMC.Net"), false);
            jda.upsertCommand("bedwars", "View a player's Bed Wars stats.")
                    .addOption(OptionType.STRING, "player", "Player Username", true).queue();
            System.out.println("[SHIELD] Starting Bed Wars bot..");
        } catch (LoginException err) {
            System.out.println("[SHIELD] Failed to start Bed Wars Bot!");
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bedwars")) {

            String player = event.getOption("player").getAsString();

            try {

                int userCooldownTime = 2;

                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("<:danger:869367070591189014> **HOLD UP!** Your guild is currently suspended from our Statistics bot due to abuse or spamming.\n\nIf you believe this was done in error, create a ticket below:\nhttps://twistedmc.net/tickets/create/").queue();
                    return;
                }

                if (userCooldown.containsKey(event.getUser().getId())) {
                    long userSecondsLeft = ((userCooldown.get(event.getUser().getId()) / 1000) + userCooldownTime) - (System.currentTimeMillis() / 1000);
                    if (userSecondsLeft > 0) {
                        event.reply("You cannot use this command for another **" + formatSeconds(userSecondsLeft) + "**!").setEphemeral(true).queue();
                        return;
                    }
                }

                if (Main.hasJoined(Objects.requireNonNull(event.getOption("player")).getAsString())) {

                    Date d = new Date();

                    SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
                    TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");

                    format.setTimeZone(etTimeZone);

                    addLog(event.getGuild().getIdLong(), event.getUser().getIdLong(), event.getCommandString(), format.format(d));

                    try {
                        EmbedBuilder bedwars = new EmbedBuilder();
                        bedwars.setTitle("Bed Wars Statistics for " + Main.getRealUsername(Objects.requireNonNull(event.getOption("player")).getAsString()));
                        bedwars.setDescription(
                                "\nBed Wars Level: **" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "level") + "**"
                                        + "\nProgress: **" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "xp") + "**/**" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "next_cost") + "**");
                        bedwars.addField("",
                                "\nCoins: **" + "0" + "**"
                                        + "\nSuper Votes: **" + "0" + "**"
                                        + "\nCosmetic Crates: **" + "0" + "**"
                                        + "\nIron: **" + "0" + "**"
                                        + "\nGold: **" + "0" + "**"
                                        + "\nDiamonds: **" + "0" + "**"
                                        + "\nEmeralds: **" + "0" + "**", true);
                        bedwars.addField("",
                                "\nWins: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "wins") + "**"
                                        + "\nWinstreak: **" + "0" + "**"
                                        + "\nKills: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "kills") + "**"
                                        + "\nFinal Kills: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "final_kills") + "**"
                                        + "\nBeds Broken: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "beds_destroyed") + "**", true);
                        bedwars.addField("",
                                "\nFKDR: **" + "0.00" + "**"
                                        + "\nWLR: **" + "0.00" + "**"
                                        + "\nBBLR: **" + "0.00" + "**", true);
                        bedwars.setColor(new Color(51, 153, 204));
                        bedwars.setFooter("Play.TwistedMC.Net Statistics");
                        bedwars.setTimestamp(new Date().toInstant());
                        event.replyEmbeds(bedwars.build()).queue();
                        bedwars.clear();
                        bedwars = null;

                        userCooldown.put(event.getUser().getId(), System.currentTimeMillis());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (!Main.hasJoined(player)) {
                    event.reply("We are sorry, but we cannot find a player with the username '**" + player + "**' in our database.").setEphemeral(true).queue();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



    public void stop(){
        this.jda.shutdown();
    }



    // Bed Wars API

    public static int getGlobalStats(String name, String stat) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.253", "3306", "bedwars", "bedwars", "uYjo8e_qgD69-Noy");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT " + stat + " VALUE FROM global_stats WHERE name = '" + name + "'");
        try{
            while(result.next()){
                return result.getInt("VALUE");
            }
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return 0;
    }

    public static int getLevel(String name, String stat) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.253", "3306", "bedwars", "bedwars", "uYjo8e_qgD69-Noy");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT " + stat + " VALUE FROM player_levels WHERE uuid = '" + Main.getUUID(name) + "'");
        try{
            while(result.next()){
                return result.getInt("VALUE");
            }
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return 0;
    }

    public static String formatSeconds(long timeInSeconds) {
        long hours = timeInSeconds / 3600;
        long secondsLeft = timeInSeconds - hours * 3600;
        long minutes = secondsLeft / 60;
        long seconds = secondsLeft - minutes * 60;

        String formattedTime = "";
        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + " hours, ";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + " minutes, ";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds + " seconds";

        return formattedTime;
    }

    public static void addLog(long guild, long sender, String message, String date) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "publicDiscordBots", "publicDiscordBots", "OuyvDusfYONFIxuR");
        Statement statement = MySQL.openConnection().createStatement();
        try{
            statement.executeUpdate("INSERT INTO logs(guild, sender, command, date) VALUES ('" + guild + "','" + sender + "','" + message + "','" + date + "')");
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            statement.close();
            MySQL.getConnection().close();
        }
    }

}