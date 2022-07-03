package net.twistedmc.shield;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.plugin.Plugin;
import net.twistedmc.shield.Util.ModerationCommandAction;
import net.twistedmc.shield.bedwars.BedWars;
import net.twistedmc.shield.stats.Stats;
import net.twistedmc.shield.twistedmc.servercommands.MessageCommand;
import net.twistedmc.shield.twistedmc.TwistedMC;
import net.twistedmc.shield.twistedmc.servercommands.UsernameVerificationCommand;
import net.twistedmc.shield.twistedmc.servercommands.VirtBanCommand;

import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public final class Main extends Plugin {

    private static net.twistedmc.shield.SHIELD.SHIELD SHIELD = null;
    private static net.twistedmc.shield.twistedmc.TwistedMC TwistedMC = null;
    private static Stats Stats = null;
    private static BedWars BedWars = null;

    public static String sqlHost = "173.44.44.251";
    public static String sqlPort = "3306";
    public static String sqlDb = "accounts";
    public static String sqlUser = "accounts";
    public static String sqlPw = "1b5m6-aKy*3d]3Z7l1ly8!5eld-hx5ZR(HFYreATsUj5J";

    public static String sqlHostDM = "173.44.44.251";
    public static String sqlPortDM = "3306";
    public static String sqlDbDM = "discordModeration";
    public static String sqlUserDM = "discordModeration";
    public static String sqlPwDM = "00za3/S9I4V0.Xd[gHKwT3@w3G4r_iAj6Bc.7P";

    public static Connection connection = null;
    public static Statement statement;

    static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    static int year = calendar.get(Calendar.YEAR);
    static String footer = "© " + year + " TwistedMC Studios";

    @Override
    public void onEnable() {

        try {
            openConnection();
            statement = connection.createStatement();
            getLogger().log(Level.INFO, "Database connected!");
        } catch (SQLException | ClassNotFoundException e) {
            getLogger().log(Level.WARNING, "Error while connecting to database! (" + e.getMessage() + ")");
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new UsernameVerificationCommand());
        getProxy().getPluginManager().registerCommand(this, new VirtBanCommand());

        BedWars = new BedWars("OTc1MzM5ODczOTQ5MDg1NzE2.GVt7cU.MD6gWVD-lQysPAdnW-MvX2tQgdwXrO0tDe4Upw");
        BedWars.start();

        TwistedMC = new TwistedMC("ODU5NjgyOTU4OTk3OTc5MTQ2.YNwQJQ.S3E4_VZh2VkHKUn20MKYmDvG57E");
        TwistedMC.start();

        Stats = new Stats("OTYwODQ2MDA1Mjk5OTgyMzM2.YkwXkw.w9znnJrwHwuA-tiyF5ov57jRiEU");
        Stats.start();

        getProxy().getPluginManager().registerCommand(this, new MessageCommand());
    }

    @Override
    public void onDisable() {
        //SHIELD.stop();
        TwistedMC.stop();
        Stats.stop();
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + sqlHost + ":" + sqlPort + "/" + sqlDb, sqlUser, sqlPw);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static int getSHIELDReportCount() throws SQLException, ClassNotFoundException {
        int Count = 0;
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `shieldReports`");
        try {
            while(result.next()){
                Count = result.getInt("COUNT(*)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return Count;
    }

    public static List<String> getSHIELDReportList(int maxList) throws SQLException, ClassNotFoundException {
        List<String> list = new ArrayList<>();
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `shieldReports` ORDER BY `shieldReports`.`date` DESC LIMIT " + maxList);
        try {
            int c = 0;
            while (result.next()) {
                list.add("**#" + (c + 1) + "** | **Report ID:** `" + result.getString("id") + "` | **Date:** `"+ result.getString("date") + "` \n");
                c += 1;
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return list;
    }

    public static String convertDatestamp(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
        return format.format(new Date(timestamp));
    }

    public static int getStatisticCount(String identifier) throws SQLException, ClassNotFoundException {
        int Count = 0;
        if (identifier.equalsIgnoreCase("bans")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountbans`");
            try {
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("accounts")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accounts`");
            try {
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("mutes")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountmutes`");
            try {
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("blacklists")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountblacklists`");
            try {
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
            return Count;
        }
    return 0;
    }

    public static String getUsername(String uuid) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `alts_users` WHERE uuid = '" + uuid + "'");
        try {
            while(result.next()){
                return result.getString("playername");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return null;
    }

    public static UUID getUUID(String username) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `alts_users` WHERE playername = '" + username + "'");
        try {
            while(result.next()){
                return UUID.fromString(result.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return null;
    }

    public static String getRealUsername(String username) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `alts_users` WHERE playername = '" + username + "'");
        try {
            while(result.next()){
                return result.getString("playername");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return null;
    }

    public static List<String> getRecentAccInfo(String identifier) throws SQLException, ClassNotFoundException {
        List<String> info = new ArrayList<>();
        int IDCount = 0;
        if (identifier.equalsIgnoreCase("bans")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `accountbans` ORDER BY `accountbans`.`id` DESC LIMIT 5");
            try {
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ " EST`) - **Reason:** `"+reason+"`"   + "\n";
                    info.add(entry);
                    IDCount += 1;
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
                if (info.size() == 0) { info.add("*`There are no records in this category.`*"); }
                return info;
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
        }
        if (identifier.equalsIgnoreCase("accounts")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `accounts` ORDER BY `accounts`.`id` DESC LIMIT 5");
            try {
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    long dateStamp = result.getLong("firstLogin");
                    StringBuilder entry = new StringBuilder();
                    entry.append("**#").append((IDCount +1)).append("** | **`").append(username).append("`** (**Date Joined:** `").append(Main.convertDatestamp(dateStamp)).append(" EST`) ");
                    entry.append("\n");
                    info.add(String.valueOf(entry));
                    IDCount += 1;
                }
                if (info.size() == 0) { info.add("*`There are no records in this category.`*"); }
                return info;
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
        }
        if (identifier.equalsIgnoreCase("mutes")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `accountmutes` ORDER BY `accountmutes`.`id` DESC LIMIT 5");
            try {
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ " EST`) - **Reason:** `"+reason+"`"   + "\n";
                    info.add(entry);
                    IDCount += 1;
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
                if (info.size() == 0) { info.add("*`There are no records in this category.`*"); }
                return info;
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
        }
        if (identifier.equalsIgnoreCase("blacklists")) {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `accountblacklists` ORDER BY `accountblacklists`.`id` DESC LIMIT 5");
            try {
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ " EST`) - **Reason:** `"+reason+"`"   + "\n";
                    info.add(entry);
                    IDCount += 1;
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
                if (info.size() == 0) { info.add("*`There are no records in this category.`*"); }
                return info;
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                result.close();
                statement.close();
                MySQL.getConnection().close();
            }
        }
        return null;
    }

    public static boolean idExists(String id) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM shieldReports WHERE id = '" + id + "'");
        try {
            while (result.next()) {
                return result.getString("id") != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return false;
    }

    public static boolean hasJoined(String name) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM alts_users WHERE playername = '" + name + "'");
        try {
            while (result.next()) {
                return result.getString("playername") != null;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return false;
    }

    public static boolean isBanned(long guild) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "publicDiscordBots", "publicDiscordBots", "OuyvDusfYONFIxuR");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM guildBan WHERE guildID = '" + guild + "'");
        try {
            while (result.next()) {
                return result.getString("guildID") != null;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return false;
    }

    public static boolean isMaintenance(String botName) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "publicDiscordBots", "publicDiscordBots", "OuyvDusfYONFIxuR");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM settings WHERE botName = '" + botName + "' AND maintenance = '1'");
        try {
            while (result.next()) {
                return result.getString("botName") != null;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return false;
    }

    public static String getStatusLink(String botName) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "publicDiscordBots", "publicDiscordBots", "OuyvDusfYONFIxuR");
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `settings` WHERE botName = '" + botName + "'");
        try {
            while(result.next()){
                return result.getString("statusLink");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            result.close();
            statement.close();
            MySQL.getConnection().close();
        }
        return null;
    }

    public static MessageEmbed generateModlog(User author, User moderated, ModerationCommandAction action, String reason,String caseID) throws SQLException, ClassNotFoundException{
        if (reason.equals("")) { reason = action.getDefaultReason(); }
        EmbedBuilder log = new EmbedBuilder();
        String Cases = "";
        int cases = Main.getCases(); if (cases == -1) { Cases += "?`";} else { Cases += (cases + 1) + "`"; }
        log.setTitle("Moderation Log | `Case #" + Cases);
        log.setColor(new Color(253, 216, 1));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("**Moderator**","**`" + author.getAsTag() + "`** (**`" + author.getId() + "`**)",true);
        log.addField("**Moderated User**","**`" + moderated.getAsTag() + "`** (**`" + moderated.getId() + "`**)",true);
        log.addField("**Case ID**","**`" + caseID + "`**",true);
        log.addBlankField(true);
        log.addField("**Action**","**`" + action.getActionLabel() + "`**",false);
        log.addField("**Reason**","**`" + reason + "`**",false);
        return log.build();
    }

    public static int getCases() throws SQLException, ClassNotFoundException {
        int Cases = 0;
        MySQL MySQL = new MySQL(sqlHostDM,sqlPortDM,sqlDbDM,sqlUserDM,sqlPwDM);
        Statement st = MySQL.openConnection().createStatement();
        ResultSet set = null;
        try {
            set = st.executeQuery("SELECT * FROM `discord_punishments`");
            while(set.next()) {
                Cases += 1;
            }
            // set.close();
            // st.close();
            // st.getConnection().close();

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }  finally {
            try {
                set.close();
                st.close();
                MySQL.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return Cases;
    }
    public static void insertCase(User target, ModerationCommandAction action, String reason, User moderator,String caseID) throws SQLException, ClassNotFoundException {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
        TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
        format.setTimeZone(etTimeZone);
        MySQL MySQL = new MySQL(sqlHostDM,sqlPortDM,sqlDbDM,sqlUserDM,sqlPwDM);
        Statement st = MySQL.openConnection().createStatement();
        try {
            st.executeUpdate("INSERT INTO `discord_punishments`(`id`, `user`,`action`,`reason`,`moderator`,`timestamp`,`caseID`) " +
                    "VALUES (0,'" + target.getAsTag() + "(" + target.getId() + ")','" + action.getActionLabel() + "','"
                    + reason + "','" + moderator.getAsTag() + "(" + moderator.getId() + ")','" + format.format(new java.util.Date()) + "','" + caseID +"')");
            //st.close();
            //st.getConnection().close();
        } catch (SQLException | NullPointerException e){
            e.printStackTrace();
        }  finally {
            try {
                st.close();
                // Main.mySQL.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static MessageEmbed generateBanEmbed(String reason,String caseID) {
        if (reason.equals("")) { reason = "Banned by a Moderator+"; }
        EmbedBuilder log = new EmbedBuilder();
        log.setTitle("You've been banned!");
        log.setDescription("You have been banned from the TwistedMC Discord server!");
        log.setColor(new Color(255, 0, 0));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("**Case ID**",caseID,false);
        log.addField("**Reason**",reason,false);
        log.setFooter(footer);
        return log.build();
    }

    public static MessageEmbed generateTimeoutEmbed(String reason,String caseID) {
        if (reason.equals("")) { reason = "Timed out by a Moderator+"; }
        EmbedBuilder log = new EmbedBuilder();
        log.setTitle("You've been timed out!");
        log.setDescription("You have been timed in the TwistedMC Discord server!");
        log.setColor(new Color(175, 66, 0));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("**Case ID**",caseID,false);
        log.addField("**Reason**",reason,false);
        log.setFooter(footer);
        return log.build();
    }

    public static MessageEmbed generateKickEmbed(String reason,String caseID) {
        if (reason.equals("")) { reason = "Kicked by a Moderator+"; }
        EmbedBuilder log = new EmbedBuilder();
        log.setTitle("You've been kicked!");
        log.setDescription("You have been kicked from the TwistedMC Discord server!");
        log.setColor(new Color(255, 97, 0));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("**Reason**",reason,false);
        log.setFooter(footer);
        return log.build();
    }


    public static MessageEmbed generatewarnEmbed(String reason,String caseID) {
        if (reason.equals("")) { reason = "Warned by a Moderator+"; }
        EmbedBuilder log = new EmbedBuilder();
        log.setTitle("You've received a warning!");
        log.setDescription("**This is just a warning to inform you the behavior below is not allowed in the TwistedMC Discord server.**\nIf you continue this behavior, you may be moderated further.");
        log.setColor(new Color(253, 216, 1));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("**Case ID**",caseID,false);
        log.addField("**Reason**",reason,false);
        log.setFooter(footer);
        return log.build();
    }

    public static MessageEmbed generateVirtualBanEmbed(String reason,String caseID) {
        if (reason.equalsIgnoreCase("")) { reason = ModerationCommandAction.VIRTUALBAN.getDefaultReason(); }
        EmbedBuilder vb = new EmbedBuilder();

        vb.setTitle("Uh oh. Looks like you've been virtually banned.", null);

        vb.setColor(new Color(35, 35, 35));

        vb.setDescription("Sorry about that, maybe you can make an appeal. Otherwise, you'll no longer be able to participate in the TwistedMC Discord server."
                + "\n\n__**Case ID:**__\n*`" + reason + "`*"
                + "\n\n__**Reason for Virtual Ban:**__\n*`" + reason + "`*");
        vb.setFooter(footer);

        return vb.build();
    }
    public static String generateRandomID() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int length = 8;

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        String randomString = sb.toString();

        return randomString;
    }
    public static String generateRandomID(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        String randomString = sb.toString();

        return randomString;
    }


    public static void clearDiscordSync(String identifier) throws SQLException,
            ClassNotFoundException {
        MySQL MySQL = new MySQL("173.44.44.251", "3306", "network_sync", "network_sync",
                "FJWUqCH5Auz9j2Wo");
        Statement statement = MySQL.openConnection().createStatement();
        try {
            statement.executeUpdate("DELETE FROM `synced_players` WHERE identifier = '" + identifier + "'");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { }
            }
            MySQL.closeConnection();
        }
    }

}
