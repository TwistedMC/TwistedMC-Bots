package net.twistedmc.shield.twistedmc;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
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
            jda.upsertCommand("bugreport", "Create a TwistedMC bug report.").queue();
            jda.upsertCommand("suggestion", "Give us feedback!").queue();
            jda.upsertCommand("feedback", "Give us feedback!").queue();
            jda.updateCommands().queue();
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

        if (event.getName().equals("bugreport")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            } else if (event.getGuild().getOwnerIdLong() != 478410064919527437L) {
                event.reply("You cannot use **/bugreport** in this guild!").setEphemeral(true).queue();
                return;
            }

            TextInput version = TextInput.create("version","Minecraft Version", TextInputStyle.SHORT)
                    .setPlaceholder("Enter your Minecraft version here")
                    .setRequiredRange(2,50)
                    .build();
            TextInput bug = TextInput.create("bug","Bug Description", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Describe the bug here. If you can provide step-by-step instructions to make this bug happen.")
                    .setRequiredRange(5,500)
                    .build();

            Modal m = Modal.create("bugreport","Create a bug report")
                    .addActionRows(ActionRow.of(version),ActionRow.of(bug))
                    .build();

            event.replyModal(m).queue();
            return;
        }

        if (event.getName().equals("suggestion") || event.getName().equals("feedback")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            } else if (event.getGuild().getOwnerIdLong() != 478410064919527437L) {
                event.reply("You cannot use **suggestion** in this guild!").setEphemeral(true).queue();
                return;
            }

            TextInput server = TextInput.create("server","Server you're giving feedback on", TextInputStyle.SHORT)
                    .setPlaceholder("What server are you giving feedback for?")
                    .setValue("Bed Wars")
                    .build();
            TextInput feedback = TextInput.create("feedback","Your feedback", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Please be as specific as possible, we really want to understand what your saying!")
                    .setRequiredRange(50,1000)
                    .build();
            TextInput examples = TextInput.create("examples","Examples", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("This can be videos, pictures, or anything that might give us a better idea of what you mean.")
                    .setRequiredRange(20,500)
                    .build();
            TextInput additional = TextInput.create("additional","Any Additional Information", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Possibly a developer's note, any final things you'd like the viewer to know.")
                    .setRequired(false)
                    .setRequiredRange(20,100)
                    .build();

            Modal m = Modal.create("suggestion","Feedback Form")
                    .addActionRows(ActionRow.of(server),ActionRow.of(feedback),ActionRow.of(examples),ActionRow.of(additional))
                    .build();

            event.replyModal(m).queue();
            return;
        }


        if (event.getName().equals("shieldreport")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            String id = event.getOption("id").getAsString();

            try {
                if (event.getGuild().getOwnerIdLong() != 478410064919527437L) {
                    event.reply("You cannot use **/shieldreport** in this guild!").setEphemeral(true).queue();
                    return;
                } else if (!Main.idExists(id)) {
                    event.reply("SHIELD Report with id **" + id + "** does not exist!").setEphemeral(true).queue();
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

        if (!event.isFromGuild()) {
            event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").setEphemeral(true).queue();
            return;
        }

        if (event.getGuild().getOwnerIdLong() != 478410064919527437L) {
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

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equalsIgnoreCase("bugreport")) {
            String version = event.getValue("version").getAsString();
            String bug = event.getValue("bug").getAsString();

            Date now = new java.sql.Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-hh-mm");

            Date date = new java.sql.Date(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            TextChannel ticket = event.getGuild().createTextChannel(format.format(now) + "-bugreport-" + event.getMember().getEffectiveName(), event.getGuild().getCategoryById("975287837836599316")).complete();
            ChannelManager ticketManager = ticket.getManager()
                    .putPermissionOverride(event.getMember(), 3072L, 8192L)
                    .putPermissionOverride(event.getGuild().getRolesByName("Admin", true).get(0), 3072L, 8192L)
                    .putPermissionOverride(event.getGuild().getRolesByName("@everyone", true).get(0), 0L, 1024L);
            ticketManager.queue();

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("<:bughunter:929619376179650560> " + event.getMember().getEffectiveName() + "'s Bug Report", null);

            eb.setColor(new Color(0, 148, 255));

            eb.setDescription("Minecraft version: **" + version + "**\nBug: **" + bug + "**");
            eb.setFooter("© " + year + " TwistedMC Studios");
            eb.setTimestamp(new Date().toInstant());

            ticket.sendMessageEmbeds(eb.build()).content(event.getMember().getAsMention() + " use this channel to post any other information or screenshots that can help us out :)").queue();
            event.reply("Thank you for your bug report!").setEphemeral(true).queue();
        }

        if (event.getModalId().equalsIgnoreCase("suggestion")) {
            String server = event.getValue("server").getAsString();
            String feedback = event.getValue("feedback").getAsString();
            String examples = event.getValue("examples").getAsString();
            String additional = event.getValue("additional").getAsString();

            Date now = new java.sql.Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-hh-mm");

            Date date = new java.sql.Date(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            TextChannel ticket = event.getGuild().createTextChannel(format.format(now) + "-feedback-" + event.getMember().getEffectiveName(), event.getGuild().getCategoryById("975287837836599316")).complete();
            ChannelManager ticketManager = ticket.getManager()
                    .putPermissionOverride(event.getMember(), 3072L, 8192L)
                    .putPermissionOverride(event.getGuild().getRolesByName("Admin", true).get(0), 3072L, 8192L)
                    .putPermissionOverride(event.getGuild().getRolesByName("@everyone", true).get(0), 0L, 1024L);
            ticketManager.queue();

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle( event.getMember().getEffectiveName() + "'s Feedback", null);

            eb.setColor(new Color(0, 148, 255));

            eb.setDescription("Server: **" + server + "**\n\nFeedback: **" + feedback + "**\n\nExamples: **" + examples + "**\n\nAdditional Info: **" + additional + "**");
            eb.setFooter("© " + year + " TwistedMC Studios");
            eb.setTimestamp(new Date().toInstant());

            ticket.sendMessageEmbeds(eb.build()).queue();
            event.reply("Thank you for your feedback!").setEphemeral(true).queue();
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