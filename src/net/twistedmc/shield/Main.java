package net.twistedmc.shield;

import net.md_5.bungee.api.plugin.Plugin;
import net.twistedmc.shield.twistedmc.servercommands.MessageCommand;
import net.twistedmc.shield.twistedmc.TwistedMC;
import net.twistedmc.shield.twistedmc.servercommands.UsernameVerificationCommand;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
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
           ResultSet result = statement.executeQuery("SELECT COUNTS(id) FROM shieldReports");
           while(result.next()){
               Count = result.getInt(1);
           }
           statement.close();
           MySQL.getConnection().close();
       } catch (SQLException | ClassNotFoundException e) {
           e.printStackTrace();
       }
       return Count;
    }

    /*public static String[] getSHIELDReportList(int maxList) {
        int count = getSHIELDReportCount();
        if (count - maxList < 1) { maxList = count; }
        List<String> list = new ArrayList<>();
        try {
            MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
            Statement statement = MySQL.openConnection().createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM shieldReports WHERE 'id' ");
            while (result.next()) {
                Count = result.getInt(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }*/



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
