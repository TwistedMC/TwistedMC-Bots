package net.twistedmc.shield;

import net.md_5.bungee.api.plugin.Plugin;
import net.twistedmc.shield.twistedmc.servercommands.MessageCommand;
import net.twistedmc.shield.twistedmc.TwistedMC;
import net.twistedmc.shield.twistedmc.servercommands.UsernameVerificationCommand;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Main extends Plugin {

    private static net.twistedmc.shield.SHIELD.SHIELD SHIELD = null;
    private static net.twistedmc.shield.twistedmc.TwistedMC TwistedMC = null;
    private static net.twistedmc.shield.bedwars.BedWars BedWars = null;

    public static String sqlHost = "173.44.44.251";
    public static String sqlPort = "3306";
    public static String sqlDb = "accounts";
    public static String sqlUser = "accountsDB";
    public static String sqlPw = "epQvHtVoAnUDNJyh";

    public static Connection connection = null;
    public static Statement statement;

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

        // SHIELD = new SHIELD("ODgwMDIyMDQzNjkxNzE2NjA4.YSYOZQ.nS_OpE0HyBXnliH8x7ZqvSb0OL8");
        // SHIELD.start();

        TwistedMC = new TwistedMC("ODU5NjgyOTU4OTk3OTc5MTQ2.YNwQJQ.S3E4_VZh2VkHKUn20MKYmDvG57E");
        TwistedMC.start();

        BedWars = new net.twistedmc.shield.bedwars.BedWars("OTYwODQ2MDA1Mjk5OTgyMzM2.YkwXkw.w9znnJrwHwuA-tiyF5ov57jRiEU");
        BedWars.start();

        getProxy().getPluginManager().registerCommand(this, new MessageCommand());
    }

    @Override
    public void onDisable() {
        //SHIELD.stop();
        TwistedMC.stop();
        BedWars.stop();
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

    public static String getUUID(String username) throws SQLException, ClassNotFoundException {
        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
        Statement statement = MySQL.openConnection().createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `alts_users` WHERE playername = '" + username + "'");
        try {
            while(result.next()){
                return result.getString("uuid");
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

}
