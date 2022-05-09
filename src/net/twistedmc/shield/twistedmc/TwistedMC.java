package net.twistedmc.shield.twistedmc;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TwistedMC extends ListenerAdapter {

    private String token;
    public static JDA jda;

    public TwistedMC(String token) {
        this.token = token;
    }

    public void start(){
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.awaitReady();
            jda.addEventListener(this);
            //jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            //jda.getPresence().setPresence(Activity.playing("Under Maintenance"), false);
            jda.upsertCommand("shieldreport", "View a SHIELD report.")
                    .addOption(OptionType.STRING, "id", "id of shield report", true).queue();
            jda.upsertCommand("staffstatistics","View different network statistics!").queue();
            System.out.println("[SHIELD] Starting TwistedMC bot..");
        } catch (LoginException | InterruptedException err) {
            System.out.println("[SHIELD] Failed to start TwistedMC Bot!");
        }
    }

    public void stop(){
        this.jda.shutdown();
    }

    public static JDA getJDA(){
        return jda;
    }

    public static void sendMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }

    public static void sendMessageDelete(User user, int delay, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue(m -> m.delete().queueAfter(delay, TimeUnit.SECONDS));
    }

    public void onPrivateMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (!event.getAuthor().getId().equals("859682958997979146")) {
            TextChannel textChannel = TwistedMC.getJDA().getTextChannelById("963511647715094598");
            if (textChannel != null) {
                textChannel.sendMessage("User: " + event.getAuthor().getName() + "\nMessage received: " + message).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("shieldreport")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            String id = event.getOption("id").getAsString();

            try {
                if (!event.getGuild().getId().equals("549595806009786388")) {
                    event.reply("You cannot use **/shieldreport* in this guild!").setEphemeral(true).queue();
                    return;
                } else if (!Main.idExists(id)) {
                    event.reply("SHIELD Report with id **" + id + "** does not exist!").queue();
                    return;
                } else {

                    try {
                        MySQL MySQL = new MySQL(Main.sqlHost, Main.sqlPort, Main.sqlDb, Main.sqlUser, Main.sqlPw);
                        Statement statement = MySQL.openConnection().createStatement();
                        ResultSet result = statement.executeQuery("SELECT * FROM shieldReports WHERE id = '" + id + "'");

                        while (result.next()) {


                            String uuid = result.getString("uuid");
                            String serverName = result.getString("serverName");
                            int ping = result.getInt("ping");
                            String tps = result.getString("tps");
                            String version = result.getString("version");
                            String brand = result.getString("brand");
                            int totalViolations = result.getInt("totalViolations");
                            int combatViolations = result.getInt("combatViolations");
                            int movementViolations = result.getInt("movementViolations");
                            int playerViolations = result.getInt("playerViolations");
                            String world = result.getString("world");
                            String location = result.getString("location");
                            String checkName = result.getString("checkName");
                            String type = result.getString("type");
                            String description = result.getString("description");
                            int violations = result.getInt("violations");
                            int maxViolations = result.getInt("maxViolations");
                            String date = result.getString("date");

                            EmbedBuilder shieldReport = new EmbedBuilder();
                            shieldReport.setTitle("Viewing SHIELD Report: " + id);
                            shieldReport.setDescription(
                                    "UUID: `" + uuid + "`"
                                            + "\nServer created on: `" + serverName + "`"
                                            + "\nDate created on: `" + date + "`"
                                            + "\nPlayer Ping: `" + ping + "`"
                                            + "\nServer TPS: `" + tps + "`"
                                            + "\nPlayer Version: `" + version + "`"
                                            + "\nBrand Info: `" + brand + "`"
                                            + "\n------------------"
                                            + "\nTotal Violations: `" + totalViolations + "`"
                                            + "\nCombat Violations: `" + combatViolations + "`"
                                            + "\nMovement Violations: `" + movementViolations + "`"
                                            + "\nPlayer Violations: `" + playerViolations + "`"
                                            + "\n------------------"
                                            + "\nPlayer World: `" + world + "`"
                                            + "\nLocation: `" + location + "`"
                                            + "\n------------------"
                                            + "\nBanned for: `" + checkName + " (Type: " + type + ")" + "`"
                                            + "\nDescription: `" + description + " " + "`"
                                            + "\nViolations: `" + violations + "`"
                                            + "\nCheck Max Violations: `" + maxViolations + "`"
                                            + "\n------------------");
                            shieldReport.setColor(new Color(51, 153, 204));
                            shieldReport.setFooter("SHIELD Protection");
                            shieldReport.setTimestamp(new Date().toInstant());
                            event.replyEmbeds(shieldReport.build()).queue();
                            shieldReport.clear();
                            shieldReport = null;


                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (event.getName().equalsIgnoreCase("staffstatistics")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (!event.getGuild().getId().equals("549595806009786388")) {
                event.reply("You cannot use **/staffstatistics** in this guild!").setEphemeral(true).queue();
                return;
            } else {

                SelectMenu menu = SelectMenu.create("menu:stats")
                        .setPlaceholder("Select Statistic Choice")
                        .setRequiredRange(1, 1)
                        .addOption("Accounts", "stat-accs", "Member Account Stats", Emoji.fromEmote("TwistedRank", Long.parseLong("970626693322641418"), false))
                        .addOption("Bans", "stat-bans", "Banned Account Stats", Emoji.fromEmote("AdminRank", Long.parseLong("970626971052687370"), false))
                        .addOption("Blacklists", "stat-bl", "Blacklisted Account Stats", Emoji.fromEmote("Blacklists", Long.parseLong("972955708016427039"), false))
                        .addOption("SHIELD Reports", "stat-sr", "SHIELD Report Stats", Emoji.fromEmote("SHIELD", Long.parseLong("926049519538409492"), false))
                        .addOption("Mutes", "stat-mutes", "Muted Account Stats", Emoji.fromEmote("ModRank", Long.parseLong("970626971228848149"), false))
                        .build();
                EmbedBuilder emb = new EmbedBuilder();
                emb.setColor(new Color(51, 153, 204));
                emb.setDescription("**Please select the statistic you wish to view!**");
                emb.setTimestamp(new Date().toInstant());
                emb.setFooter("TwistedMC");
                event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
            }
        }
    }



    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (event.getSelectMenu().getId().equalsIgnoreCase("menu:stats")) {
            SelectMenu fakemenu = event.getSelectMenu().createCopy().setDisabled(true).setPlaceholder(event.getSelectedOptions().get(0).getDescription() + " Selected").build();
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("stat-sr")) {
                int SRs = 0;
                try {
                    SRs = Main.getSHIELDReportCount();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("**Total SHIELD Reports:** **`").append(SRs).append("`**").append("\n\n__**Most Recent SHIELD Reports**__ \n");
                List<String> reports = null;
                try {
                    reports = Main.getSHIELDReportList(10);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                reports.stream().forEach(entry -> sb.append(entry));
                EmbedBuilder emb = new EmbedBuilder();
                emb.setTimestamp(new Date().toInstant());
                emb.setTitle("<:SHIELD:926049519538409492> Viewing SHIELD Report Statistics");
                emb.setColor(new Color(51, 153, 204));
                emb.setFooter("SHIELD Protection");
                emb.setDescription(sb);
                event.replyEmbeds(emb.build()).setContent("Requested By: " + event.getUser().getAsMention()).queue();
                event.editSelectMenu(fakemenu).queue();
                return;
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("stat-accs")) {
                int Accs = 0;
                try {
                    Accs = Main.getStatisticCount("accounts");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> AccountsData = null;
                try {
                    AccountsData = Main.getRecentAccInfo("accounts");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder accountsSB = new StringBuilder();
                accountsSB.append("**Total Player Accounts:** **`").append(Accs).append("`**").append("\n\n__**Most Recent Player Accounts**__ \n");
                EmbedBuilder accEmb = new EmbedBuilder();
                AccountsData.stream().forEach(entry -> accountsSB.append(entry));
                accEmb.setTimestamp(new Date().toInstant());
                accEmb.setTitle("<:TwistedRank:970626693322641418> Viewing Player Account Statistics");
                accEmb.setColor(new Color(51, 153, 204));
                accEmb.setFooter("TwistedMC");
                accEmb.setDescription(accountsSB);
                event.replyEmbeds(accEmb.build()).setContent("Requested By: " + event.getUser().getAsMention()).queue();
                event.editSelectMenu(fakemenu).queue();
                return;
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("stat-bans")) {
                int Bans = 0;
                try {
                    Bans = Main.getStatisticCount("bans");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> BansData = null;
                try {
                    BansData = Main.getRecentAccInfo("bans");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder bansSB = new StringBuilder();
                bansSB.append("**Total Banned Players:** **`").append(Bans).append("`**").append("\n\n__**Most Recent Player Bans**__ \n");
                EmbedBuilder banEmb = new EmbedBuilder();
                BansData.stream().forEach(entry -> bansSB.append(entry));
                banEmb.setTimestamp(new Date().toInstant());
                banEmb.setTitle("<:AdminRank:970626971052687370> Viewing Banned Player Statistics");
                banEmb.setColor(new Color(51, 153, 204));
                banEmb.setFooter("TwistedMC");
                banEmb.setDescription(bansSB);
                event.replyEmbeds(banEmb.build()).setContent("Requested By: " + event.getUser().getAsMention()).queue();
                event.editSelectMenu(fakemenu).queue();
                return;
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("stat-mutes")) {
                int Mutes = 0;
                try {
                    Mutes = Main.getStatisticCount("mutes");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> MutesData = null;
                try {
                    MutesData = Main.getRecentAccInfo("mutes");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder mutesSB = new StringBuilder();
                mutesSB.append("**Total Muted Players:** **`").append(Mutes).append("`**").append("\n\n__**Most Recent Player Mutes**__ \n");
                EmbedBuilder muteEmb = new EmbedBuilder();
                MutesData.stream().forEach(entry -> mutesSB.append(entry));
                muteEmb.setTimestamp(new Date().toInstant());
                muteEmb.setTitle("<:ModRank:970626971228848149> Viewing Muted Player Statistics");
                muteEmb.setColor(new Color(51, 153, 204));
                muteEmb.setFooter("TwistedMC");
                muteEmb.setDescription(mutesSB);
                event.replyEmbeds(muteEmb.build()).setContent("Requested By: " + event.getUser().getAsMention()).queue();
                event.editSelectMenu(fakemenu).queue();
                return;
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("stat-bl")) {
                int BLs = 0;
                try {
                    BLs = Main.getStatisticCount("blacklists");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> BlacklistData = null;
                try {
                    BlacklistData = Main.getRecentAccInfo("blacklists");
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder blSB = new StringBuilder();
                blSB.append("**Total Banned Players:** **`").append(BLs).append("`**").append("\n\n__**Most Recent Player BlackLists**__ \n");
                EmbedBuilder BLEmb = new EmbedBuilder();
                BlacklistData.stream().forEach(entry -> blSB.append(entry));
                BLEmb.setTimestamp(new Date().toInstant());
                BLEmb.setTitle("<:Blacklists:972955708016427039> Viewing Blacklisted Player Statistics");
                BLEmb.setColor(new Color(51, 153, 204));
                BLEmb.setFooter("TwistedMC");
                BLEmb.setDescription(blSB);
                event.replyEmbeds(BLEmb.build()).setContent("Requested By: " + event.getUser().getAsMention()).queue();
                event.editSelectMenu(fakemenu).queue();
                return;
            }
        }
    }

}