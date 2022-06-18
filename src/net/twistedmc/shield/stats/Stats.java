package net.twistedmc.shield.stats;

import de.simonsator.partyandfriends.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Stats extends ListenerAdapter {

    private static final int MAX_LEVEL = 3000, BASE_XP = 10000;

    private String token;
    private JDA jda;

    public static HashMap<String, Long> userCooldown = new HashMap<String, Long>();
    public static HashMap<String, Long> guildCooldown = new HashMap<String, Long>();

    public Stats(String token) {
        this.token = token;
    }

    public void start() {
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(this);
            jda.getPresence().setPresence(Activity.playing("Play.TwistedMC.Net"), false);
            jda.upsertCommand("network", "View a player's Network stats.")
                    .addOption(OptionType.STRING, "player", "Player Username", true).queue();
            jda.upsertCommand("bedwars", "View a player's Bed Wars stats.")
                    .addOption(OptionType.STRING, "player", "Player Username", true).queue();
            jda.updateCommands().queue();
            System.out.println("[SHIELD] Starting Bed Wars bot..");
        } catch (LoginException err) {
            System.out.println("[SHIELD] Failed to start Bed Wars Bot!");

        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bedwars")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").setEphemeral(true).queue();
                return;
            }

            String player = event.getOption("player").getAsString();

            try {

                int userCooldownTime = 2;

                if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() != 478410064919527437L && event.getUser().getIdLong() != 478410064919527437L) {
                    event.reply("**HOLD UP!** This bot is currently under maintenance!\n\nFor More Information, click the button below:")
                            .addActionRow(Button.link(Main.getStatusLink("Statistics"), "View Status Updates"))
                            .queue();
                    return;
                }

                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("**HOLD UP!** This guild is currently suspended from using the Statistics bot due to abuse and/or spamming.\n\nIf you believe this was done in error, create a ticket using the button " +
                                    "below:")
                            .addActionRow(Button.link("https://twistedmc.net/tickets/create/?ticket_form_id=5", "Submit a request"))
                            .queue();
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

                        NumberFormat formatter = NumberFormat.getIntegerInstance();

                        final DecimalFormat df = new DecimalFormat("#.##");

                        final double fk = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "final_kills");
                        final double fd = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "final_deaths");
                        final double fkdr = fk / fd;

                        final double k = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "kills");
                        final double deaths = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "deaths");
                        final double kdr = k / deaths;

                        final double w = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "wins");
                        final double l = getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "looses");
                        final double wlr = w / l;

                        EmbedBuilder bedwars = new EmbedBuilder();
                        bedwars.setTitle("Bed Wars Statistics for " + Main.getRealUsername(Objects.requireNonNull(event.getOption("player")).getAsString()));
                        bedwars.setDescription(
                                "\nBed Wars Level: **" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "level") + "**"
                                        + "\nProgress: **" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "xp") + "**/**" + getLevel(Objects.requireNonNull(event.getOption("player")).getAsString(), "next_cost") + "**");
                        bedwars.addField("",
                                "\nCoins: **" + formatter.format(getCoins(Objects.requireNonNull(event.getOption("player")).getAsString())) + "**"
                                        + "\nSuper Votes: **" + formatter.format(getSuperVotes(Objects.requireNonNull(event.getOption("player")).getAsString())) + "**"
                                        + "\nCosmetic Crates: **" + "0" + "**"
                                        + "\nIron: **" + getBWStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "iron") + "**"
                                        + "\nGold: **" + getBWStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "gold") + "**"
                                        + "\nDiamonds: **" + getBWStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "diamonds") + "**"
                                        + "\nEmeralds: **" + getBWStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "emeralds") + "**", true);
                        bedwars.addField("",
                                "\nWins: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "wins") + "**"
                                        + "\nWinstreak: **" + formatter.format(getPlayerWinStreak(event.getOption("player").getAsString())) + "**"
                                        + "\nKills: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "kills") + "**"
                                        + "\nFinal Kills: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "final_kills") + "**"
                                        + "\nBeds Broken: **" + getGlobalStats(Objects.requireNonNull(event.getOption("player")).getAsString(), "beds_destroyed") + "**", true);
                        bedwars.addField("",
                                "\nFKDR: **" + df.format(fkdr).replace("�", "0") + "**"
                                        + "\nKDR: **" + df.format(kdr).replace("�", "0") + "**"
                                        + "\nWLR: **" + df.format(wlr).replace("�", "0") + "**"
                                        + "\nBBLR: **" + "..." + "**", true);
                        bedwars.addField("",
                                "\nLast played: **" + format.format(new Date(getLastLogin(Objects.requireNonNull(event.getOption("player")).getAsString()))) + "**", false);
                        bedwars.setColor(new Color(51, 153, 204));
                        bedwars.setFooter("Play.TwistedMC.Net Statistics");
                        bedwars.setTimestamp(new Date().toInstant());
                        event.replyEmbeds(bedwars.build()).queue();
                        bedwars.clear();
                        bedwars = null;

                        if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() == 478410064919527437L) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                            textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but this guild bypasses maintenances!").queue();
                        } else if (Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 478410064919527437L || Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 208757906428919808L) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                            textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but you bypass maintenances!").queue();
                        }

                        userCooldown.put(event.getUser().getId(), System.currentTimeMillis());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (!Main.hasJoined(player)) {
                    event.reply("We are sorry, but we cannot find a player with the username '**" + player + "**' in our database.").queue();
                    if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() == 478410064919527437L) {
                        TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                        textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but this guild bypasses maintenances!").queue();
                    } else if (Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 478410064919527437L || Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 208757906428919808L) {
                        TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                        textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but you bypass maintenances!").queue();
                    }

                }

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        // NETWORK STATS //

        if (event.getName().equals("network")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").setEphemeral(true).queue();
                return;
            }

            String player = event.getOption("player").getAsString();

            try {

                int userCooldownTime = 2;

                if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() != 478410064919527437L && event.getUser().getIdLong() != 478410064919527437L) {
                    event.reply("**HOLD UP!** This bot is currently under maintenance!\n\nFor More Information, click the button below:")
                            .addActionRow(Button.link(Main.getStatusLink("Statistics"), "View Status Updates"))
                            .queue();
                    return;
                }

                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("**HOLD UP!** This guild is currently suspended from using the Statistics bot due to abuse and/or spamming.\n\nIf you believe this was done in error, create a ticket using the button " +
                                    "below:")
                            .addActionRow(Button.link("https://twistedmc.net/tickets/create/?ticket_form_id=5", "Submit a request"))
                            .queue();
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

                        NumberFormat formatter = NumberFormat.getIntegerInstance();

                        final DecimalFormat df = new DecimalFormat("#.##");


                        EmbedBuilder bedwars = new EmbedBuilder();
                        bedwars.setTitle("Network Statistics for " + Main.getRealUsername(Objects.requireNonNull(event.getOption("player")).getAsString()));
                        bedwars.setDescription(
                                "\nNetwork Level: **" + getLevel(Main.getUUID(player)) + "**"
                                + "\nBattle Pass Level: **" + "0" + "**"
                                + "\nAchievement Points: **" + "0" + "**"
                                + "\nStatus: **" + "..." + "**"

                        );
                        bedwars.addField("",
                                "\nFirst Login: **" + getLoginInfo(player, "firstLogin") + "**"
                                        + "\nLast Login: **" + getLoginInfo(player, "lastLogin") + "**"
                                        + "\nFriends: **" + formatter.format(getFriendCount(Main.getUUID(player))) + "**"
                                        + "\nGifts Given: **" + "0" + "**"
                                        + "\nRanks Gifted: **" + "0" + "**", true);
                        bedwars.setColor(new Color(51, 153, 204));
                        bedwars.setFooter("Play.TwistedMC.Net Statistics");
                        bedwars.setTimestamp(new Date().toInstant());
                        event.replyEmbeds(bedwars.build()).queue();
                        bedwars.clear();
                        bedwars = null;

                        if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() == 478410064919527437L) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                            textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but this guild bypasses maintenances!").queue();
                        } else if (Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 478410064919527437L || Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 208757906428919808L) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                            textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but you bypass maintenances!").queue();
                        }

                        userCooldown.put(event.getUser().getId(), System.currentTimeMillis());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (!Main.hasJoined(player)) {
                    event.reply("We are sorry, but we cannot find a player with the username '**" + player + "**' in our database.").queue();
                    if (Main.isMaintenance("Statistics") && event.getGuild().getOwnerIdLong() == 478410064919527437L) {
                        TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                        textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but this guild bypasses maintenances!").queue();
                    } else if (Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 478410064919527437L || Main.isMaintenance("Statistics") && event.getUser().getIdLong() == 208757906428919808L) {
                        TextChannel textChannel = event.getGuild().getTextChannelById(event.getChannel().getId());
                        textChannel.sendMessage("**ALERT!** This bot is currently under maintenance, but you bypass maintenances!").queue();
                    }

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

    public static long getLastLogin(String name) {
        try {
            MySQL MySQL = new MySQL("173.44.44.253", "3306", "bedwars_stats", "bedwars_stats", "zDVUd6oi6XcqYV1H");
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `stats` WHERE uuid = '" +  Main.getUUID(name) + "'");
            while (result.next()) {
                return result.getLong("lastPlayed");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getBWStats(String name, String stat) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.253", "3306", "bedwars_stats", "bedwars_stats", "zDVUd6oi6XcqYV1H");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT " + stat + " VALUE FROM stats WHERE uuid = '" + Main.getUUID(name) + "'");
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

    // Network Stats //

    public static String getLoginInfo(String name, String stat) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "accounts", "accountsDB", "epQvHtVoAnUDNJyh");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT " + stat + " VALUE FROM accounts WHERE uuid = '" + Main.getUUID(name) + "'");
        try{
            while(result.next()){

                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
                TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");

                format.setTimeZone(etTimeZone);

                if (getPreference(String.valueOf(Main.getUUID(name)), "apiUsage", 1) || stat.equals("firstLogin")) {
                    return format.format(new Date(Long.parseLong(result.getString("VALUE"))));
                } else if (getPreference(String.valueOf(Main.getUUID(name)), "apiUsage", 0) && stat.equals("lastLogin")) {
                    return "N/A";
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return "...";
    }

    public static Integer getFriendCount(UUID pPlayer) {
        return getFriendCount(PAFPlayerManager.getInstance().getPlayer(pPlayer));
    }

    public static Integer getFriendCount(String pPlayer) {
        return getFriendCount(PAFPlayerManager.getInstance().getPlayer(pPlayer));
    }

    public static Integer getFriendCount(PAFPlayer pPlayer) {
        return pPlayer.getFriends().size();
    }

    /**
     * Gets the current TwistedMC level of the player
     *
     * @return TwistedMC level of player
     */
    public static String getLevel(UUID uuid) throws SQLException, ClassNotFoundException {
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        for (int level = 1; level <= MAX_LEVEL; level++)
            if (getStat(uuid, "EXPERIENCE") < (level * Math.log10(level) * BASE_XP))
                return formatter.format((level - 1));

        return formatter.format(MAX_LEVEL);
    }

    public static int getStat(UUID uuid, String stat) throws SQLException, ClassNotFoundException {
        int value = 0;
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "accounts", "accountsDB", "epQvHtVoAnUDNJyh");
        Statement statement = MySQL.openConnection().createStatement();

        ResultSet result = null;

        if (stat.equals("EXPERIENCE")) {
            result = statement.executeQuery("SELECT networkExperience VALUE FROM accountstats WHERE uuid = '" + uuid + "'");
        } else if (stat.equals("TOTAL_COINS")) {
            result = statement.executeQuery("SELECT totalCoins VALUE FROM accountstats WHERE uuid = '" + uuid + "'");
        } else if (stat.equals("TOTAL_PLAYTIME")) {
            result = statement.executeQuery("SELECT totalPlaytime VALUE FROM accountstats WHERE uuid = '" + uuid + "'");
        }
        try {
            while (result.next()) {
                value = result.getInt("VALUE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) { }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { }
            }
            MySQL.closeConnection();
        }
        return value;
    }

    public static boolean getPreference(String player, String preference, int number){
        try {
            MySQL MySQL = new MySQL("173.44.44.251", "3306", "accounts", "accountsDB", "epQvHtVoAnUDNJyh");
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM accountpreferences WHERE UUID = '" + player + "' AND " + preference + " = '" + number + "'");
            while(res.next()){
                return res.getString(preference) != null;
            }
            return false;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
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



    public static int getSuperVotes(String name) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "accounts", "accountsDB", "epQvHtVoAnUDNJyh");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM superVotes WHERE name = '" + name + "'");
        try{
            while(result.next()){
                return result.getInt("superVotes");
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

    public static int getCoins(String name) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "accounts", "accountsDB", "epQvHtVoAnUDNJyh");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM coins WHERE name = '" + name + "'");
        try{
            while(result.next()){
                return result.getInt("bedwarsCoins");
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

    public static int getPlayerWinStreak(String name) throws SQLException, ClassNotFoundException {
        int streak = 0;
        MySQL MySQL = new MySQL("173.44.44.253", "3306", "bedwars", "bedwars", "uYjo8e_qgD69-Noy");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT streak FROM `winstreaks` WHERE uuid = '" + Main.getUUID(name) + "'");
        try {
            while (result.next()) {
                streak = result.getInt("streak");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            MySQL.closeConnection();
        }

        return streak;
    }

}