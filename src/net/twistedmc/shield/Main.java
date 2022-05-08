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

        // BedWars = new BedWars("OTYwODQ2MDA1Mjk5OTgyMzM2.YkwXkw.w9znnJrwHwuA-tiyF5ov57jRiEU");
        // BedWars.start();

        getProxy().getPluginManager().registerCommand(this, new MessageCommand());
    }

    @Override
    public void onDisable() {
        SHIELD.stop();
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

    public static int getSHIELDReportCount() {
        int Count = 0;
       try {
           MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
           Statement statement = MySQL.openConnection().createStatement();
           ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `shieldReports`");
           while(result.next()){
               Count = result.getInt("COUNT(*)");
           }
           result.close();
           statement.close();
           MySQL.getConnection().close();
       } catch (SQLException | ClassNotFoundException e) {
           e.printStackTrace();
       }
       return Count;
    }

    public static List<String> getSHIELDReportList(int maxList) {
        List<String> list = new ArrayList<>();
        try {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `shieldReports` ORDER BY `shieldReports`.`date` DESC LIMIT " + maxList);
            int c = 0;
            while (result.next()) {
                list.add("**#" + (c + 1) + "** | **Report ID:** `" + result.getString("id") + "` | **Date:** `"+ result.getString("date") + "` \n");
                c += 1;
            }
            result.close();
            statement.close();
            MySQL.getConnection().close();
            return list;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String convertDatestamp(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
        return format.format(new Date(timestamp));
    }

    public static int getStatisticCount(String identifier) {
        int Count = 0;
        if (identifier.equalsIgnoreCase("bans")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountbans`");
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("accounts")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accounts`");
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("mutes")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountmutes`");
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Count;
        }
        if (identifier.equalsIgnoreCase("blacklists")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `accountblacklists`");
                while(result.next()){
                    Count = result.getInt("COUNT(*)");
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return Count;
        }
    return 0;
    }

    public static String getUsername(String uuid) {
        try {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM `alts_users` WHERE uuid = '" + uuid + "'");
            while(result.next()){
                return result.getString("playername");
            }
            result.close();
            statement.close();
            MySQL.getConnection().close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getRecentAccInfo(String identifier) {
        List<String> info = new ArrayList<>();
        int IDCount = 0;
        if (identifier.equalsIgnoreCase("bans")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM `accountbans` ORDER BY `accountbans`.`id` DESC LIMIT 5");
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ "`) - **Reason:** `"+reason+"`"   + "\n";
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
            }
        }
        if (identifier.equalsIgnoreCase("accounts")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM `accounts` ORDER BY `accounts`.`id` DESC LIMIT 5");
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    long dateStamp = result.getLong("firstLogin");
                    StringBuilder entry = new StringBuilder();
                    entry.append("**#").append((IDCount +1)).append("** | **`").append(username).append("`** (**Date Joined:** `").append(Main.convertDatestamp(dateStamp)).append("`) ");
                    entry.append("\n");
                    info.add(String.valueOf(entry));
                    IDCount += 1;
                }
                result.close();
                statement.close();
                MySQL.getConnection().close();
                if (info.size() == 0) { info.add("*`There are no records in this category.`*"); }
                return info;
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (identifier.equalsIgnoreCase("mutes")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM `accountmutes` ORDER BY `accountmutes`.`id` DESC LIMIT 5");
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ "`) - **Reason:** `"+reason+"`"   + "\n";
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
            }
        }
        if (identifier.equalsIgnoreCase("blacklists")) {
            try {
                MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                Statement statement = MySQL.openConnection().createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM `accountblacklists` ORDER BY `accountblacklists`.`id` DESC LIMIT 5");
                while (result.next()) {
                    String username = Main.getUsername(result.getString("uuid"));
                    String pID = result.getString("punishmentID");
                    long dateStamp = result.getLong("date");
                    String reason = result.getString("reason");
                    String entry = "**#" + (IDCount +1) +"** | **`" + username + "`** (**ID:** `#" + pID+"` | **Date:** `"+Main.convertDatestamp(dateStamp)+ "`) - **Reason:** `"+reason+"`"   + "\n";
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
        return false;
    }

}
