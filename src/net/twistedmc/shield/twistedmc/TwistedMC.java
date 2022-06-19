package net.twistedmc.shield.twistedmc;


import net.dv8tion.jda.api.*;
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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.MySQL;
import net.twistedmc.shield.Util.ModerationCommandAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.sql.Time;
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
    public static boolean StatusNotifs = false;


    public static String GuildID = "549595806009786388";
    public static String VirtualBanRoleID = "837168599814373416";
    public static String ModlogChannelID = "772160218477756419";
    private static HashMap<String,String[]> modMap = new HashMap<>(); // Key: DiscordID of sender; Values: String[] = {userID,Action,Reason}; (3)
    private static HashMap<String,User> modMapUser = new HashMap<>();
    private static HashMap<String,Boolean> modConfirmBypass = new HashMap<>(); // Key: DiscordID of sender; Value: true or false
    private static HashMap<String,String[]> modTimeout = new HashMap<>();
    private static HashMap<String,Boolean> appealUserConfirmed = new HashMap<>(); // K: DID of sender; V: t/f
    private static HashMap[] maps = {modMap,modMapUser,modConfirmBypass,modTimeout,appealUserConfirmed};
    private static ArrayList<HashMap> macMaps = new ArrayList<>();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    int year = calendar.get(Calendar.YEAR);
    String footer = "© " + year + " TwistedMC Studios";

    public TwistedMC(String token) {
        this.token = token;
    }

    public void start(){
        try {
            macMaps.add(modMap);
            macMaps.add(modConfirmBypass);
            macMaps.add(modMapUser);
            macMaps.add(modTimeout);
            this.jda = JDABuilder.createDefault(token).build();
            jda.awaitReady();
            jda.addEventListener(this);
            //jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            jda.getPresence().setPresence(Activity.playing("We're online!"), false);
            jda.getGuildById(GuildID).upsertCommand("shieldreport", "View a SHIELD report.")
                    .addOption(OptionType.STRING, "id", "id of shield report", true).queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("staffstatistics","View different network statistics!").queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("bugreport", "Create a TwistedMC bug report.").queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("suggestion", "Give us feedback!").queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("feedback", "Give us feedback!").queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("moderate","Moderate a user with different options")
                    .addOption(OptionType.USER,"user","User to Moderate",true)
                    .addOption(OptionType.BOOLEAN,"bypass","Bypass the final confirmation?",false)
                    .queue();
            jda.updateCommands().queue();
            jda.getGuildById(GuildID).upsertCommand("maintenance", "Send Maintenance Alert in #sync")
                    .addOption(OptionType.STRING, "link", "Status URL Link", true).queue();
            jda.updateCommands().queue();
            System.out.println("[SHIELD] Starting TwistedMC bot..");
        } catch (LoginException | InterruptedException err) {
            System.out.println("[SHIELD] Failed to start TwistedMC Bot!");
        }
    }

    public void stop(){
        getJDA().shutdown();
        Arrays.stream(maps).forEach(HashMap::clear);
        macMaps.clear();
    }

    public static JDA getJDA(){
        return jda;
    }

    private static void removeUserFromMACMaps(String userid) {
        macMaps.stream().forEach((map) -> {
            if (map.containsKey(userid)) {
                map.remove(userid);
            }
        });
    }


    public static void sendMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }

    public static void sendMessage(User user, MessageEmbed embed) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed))
                .queue();
    }

    public static void sendMessage(User user, MessageEmbed embed, String punishmentType) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://twistedmc.net/tickets/create/?ticket_form_id=6&discord_id=" + user.getId() + "&punishment_type=" + punishmentType, "Appeal this punishment")))
                .queue();
    }

    public static void sendMessageKick(User user, MessageEmbed embed) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://discord.gg/twistedmc", "Join back")))
                .queue();
    }

    public static void sendMessage(User user, String content, int delay) {
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

        if (event.getName().equals("maintenance")) {

            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            } else if (event.getGuild().getOwnerIdLong() != 478410064919527437L) {
                event.reply("You cannot use **/maintenance** in this guild!").setEphemeral(true).queue();
                return;
            }

            String link = event.getOption("link").getAsString();

            EmbedBuilder maintenance = new EmbedBuilder();
            maintenance.setTitle("<:danger:869367070591189014> WE'RE UNDER MAINTENANCE!");
            maintenance.setDescription("Sorry about that! You are not able to link your account as we are currently under maintenance. For More Information, click the button below:");
            maintenance.setColor(new Color(255, 0, 0));
            maintenance.setFooter(footer);
            maintenance.setTimestamp(new Date().toInstant());
            event.replyEmbeds(maintenance.build()).addActionRow(Button.link(link, "View Status Updates"))
                    .queue();
            maintenance.clear();
            maintenance = null;
            return;
        }

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

        if (event.getName().equals("moderate")) {
            if (!event.isFromGuild()) {
                event.reply("<:danger:869367070591189014> **HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }
            User user = event.getOption("user").getAsUser();
            if (modConfirmBypass.containsKey(event.getUser().getId())) {
                modConfirmBypass.remove(event.getUser().getId());
            }
            if (event.getOption("bypass") != null) {
                if (event.getOption("bypass").getAsBoolean()) {
                    modConfirmBypass.put(event.getUser().getId(),Boolean.TRUE);
                } else {
                    modConfirmBypass.put(event.getUser().getId(),Boolean.FALSE);
                }
            } else {
                modConfirmBypass.put(event.getUser().getId(),Boolean.FALSE);
            }
            if (modMap.containsKey(event.getUser().getId())) {
                modMap.remove(event.getUser().getId());
            }
            if (modMapUser.containsKey(event.getUser().getId())) {
                modMapUser.remove(event.getUser().getId());
            }

            String[] data = {user.getId(),"",""};
            modMapUser.put(event.getUser().getId(),user);
            modMap.put(event.getUser().getId(),data);
            SelectMenu menu = SelectMenu.create("menu:modaction")
                    .setPlaceholder("Select Moderation Action Command")
                    .setRequiredRange(1, 1)
                    .addOption("Compromised Account", "mac-ca", "Auto-Ban the user with reason: \"Compromised Account\"")
                    .addOption("Underage User","mac-uau","User age < 13 years old. (Req: 13+ according to Discord TOS)")
                    .addOption("Warn", "mac-w", "Warn the user. Sends them a DM with the reason.")
                    .addOption("Kick", "mac-k", "Kicks the user.")
                    .addOption("Ban", "mac-b", "Ban the user.")
                    .addOption("Virtual Ban","mac-vb","Blocks user from interacting with the discord")
                    .addOption("Timeout","mac-t","Timeout the user.")
                    .addOption("Cancel Moderation","mac-cancel","Cancel the current moderation command.")
                    .build();
            EmbedBuilder emb = new EmbedBuilder();
            emb.setColor(new Color(253, 216, 1));
            emb.setDescription("**Please select the moderation action command!**");
            emb.setTimestamp(new Date().toInstant());
            emb.setFooter(footer);
            event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
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
            return;
        }

        if (event.getName().equals("staffstatistics")) {
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
            return;
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
        if (event.getModalId().equalsIgnoreCase("mac:reason")) {
            try {
                String[] data = modMap.get(event.getUser().getId());
                data[2] = event.getValue("mac:reasoninput").getAsString();
                User target = modMapUser.get(event.getUser().getId());
                assert target != null;
                Guild g = getJDA().getGuildById(GuildID);
                String reason = data[2];
                modMap.put(event.getUser().getId(), data);
                if (modConfirmBypass.get(event.getUser().getId())) {
                    if (data[1].equalsIgnoreCase("kick")) {
                        ModerationCommandAction action = ModerationCommandAction.KICK;

                        MessageEmbed vbPM = Main.generateKickEmbed(reason);
                        sendMessageKick(target,vbPM);

                        g.kick(UserSnowflake.fromId(target.getId()),reason).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action,reason);
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ban")) {
                        ModerationCommandAction action = ModerationCommandAction.BAN;

                        MessageEmbed vbPM = Main.generateBanEmbed(reason);
                        sendMessage(target,vbPM, "2");

                        g.ban(UserSnowflake.fromId(target.getId()),1,reason).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action,reason);
                        Main.insertCase(target, action,data[2],event.getUser());
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("warning")) {
                        ModerationCommandAction action = ModerationCommandAction.WARN;
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action,reason);
                        Main.insertCase(target, action,data[2],event.getUser());
                        MessageEmbed warn = Main.generatewarnEmbed(reason);
                        sendMessage(target,warn);
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("virtual-ban")) {
                        ModerationCommandAction action = ModerationCommandAction.VIRTUALBAN;
                        MessageEmbed log = Main.generateModlog(event.getUser(),target,action,reason);
                        MessageEmbed vbPM = Main.generateVirtualBanEmbed(reason);
                        Main.insertCase(target,action,reason,event.getUser());
                        sendMessage(target,vbPM, "3");
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        g.addRoleToMember(UserSnowflake.fromId(target.getId()),g.getRoleById(VirtualBanRoleID)).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("timeout")) {
                        ModerationCommandAction action = ModerationCommandAction.TIMEOUT;
                        String unit = event.getValue("mac:to:unit").getAsString();
                        int duration = Integer.parseInt(event.getValue("mac:to:duration").getAsString());
                        /*if (!unit.equalsIgnoreCase("seconds") || !unit.equalsIgnoreCase("minutes") || !unit.equalsIgnoreCase("hours") || !unit.equalsIgnoreCase("days")) {
                                event.reply("Invalid TimeUnit! MAC Cancelled!").setEphemeral(true).queue();
                                removeUserFromMACMaps(event.getUser().getId());
                                return;
                            }*/
                        TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
                        if (timeUnit == TimeUnit.DAYS && duration > 28 ||
                                timeUnit == TimeUnit.HOURS && duration > 672 ||
                                timeUnit == TimeUnit.MINUTES && duration > 40320 ||
                                timeUnit == TimeUnit.SECONDS && duration > 2419200) {
                            event.reply("Invalid Duration! MAC Cancelled!").queue();
                            removeUserFromMACMaps(event.getUser().getId());
                            return;
                        }
                        try {
                            MessageEmbed log = Main.generateModlog(event.getUser(), target, action,reason);
                            Main.insertCase(target, action,data[2],event.getUser());

                            MessageEmbed vbPM = Main.generateTimeoutEmbed(reason);
                            sendMessage(target,vbPM, "1");

                            g.getMember(UserSnowflake.fromId(target.getId())).timeoutFor(duration,timeUnit).queue();
                            g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                            event.reply("Moderation Completed!").setEphemeral(true).queue();
                            removeUserFromMACMaps(event.getUser().getId());
                            return;
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    EmbedBuilder confirm_msg = new EmbedBuilder();
                    confirm_msg.setTitle("Confirm Moderation Action?");
                    if (data[1].equalsIgnoreCase("timeout")) {
                        confirm_msg.setDescription("**User:** **`" + target.getAsTag() + "`** | (**`" + target.getId() + "`**) \n"
                                + "**Action:** **`" + data[1].toUpperCase() + "`** \n"
                                + "**Duration:** **`" + event.getValue("mac:to:duration").getAsString() + "`** \n"
                                + "**TimeUnit:** **`" + event.getValue("mac:to:unit").getAsString() + "`** \n"
                                + "**Reason:** **`" + event.getValue("mac:reasoninput").getAsString() + "`**"
                        );
                    } else {
                        confirm_msg.setDescription("**User:** **`" + target.getAsTag() + "`** | (**`" + target.getId() + "`**) \n"
                                + "**Action:** **`" + data[1].toUpperCase() + "`** \n"
                                + "**Reason:** **`" + event.getValue("mac:reasoninput").getAsString() + "`**"
                        );
                    }
                    confirm_msg.setFooter(footer);
                    confirm_msg.setTimestamp(new Date().toInstant());
                    confirm_msg.setColor(new Color(253, 216, 1));
                    SelectMenu confirm_menu = SelectMenu.create("menu:modaction-confirm")
                            .addOption("Yes", "confirm-yes")
                            .addOption("No", "confirm-no")
                            .setPlaceholder("Confirm this action?")
                            .setRequiredRange(1, 1)
                            .build();
                    if (data[1].equalsIgnoreCase("timeout")) {
                        String[] todata = new String[2];
                        todata[0] = event.getValue("mac:to:unit").getAsString();
                        todata[1] = event.getValue("mac:to:duration").getAsString();
                        modTimeout.put(event.getUser().getId(),todata);
                    }
                    modMap.put(event.getUser().getId(), data);
                    event.replyEmbeds(confirm_msg.build()).addActionRow(confirm_menu).setEphemeral(true).queue();
                    confirm_msg.clear();
                    confirm_msg = null;
                }
            } catch (SQLException | ClassNotFoundException | NullPointerException | IllegalArgumentException e) {
                e.printStackTrace();
                event.reply("Something went wrong!").setEphemeral(true).queue();
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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("**Total SHIELD Reports:** **`").append(SRs).append("`**").append("\n\n__**Most Recent SHIELD Reports**__ \n");
                List<String> reports = null;
                try {
                    reports = Main.getSHIELDReportList(10);
                } catch (SQLException | ClassNotFoundException e) {
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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> AccountsData = null;
                try {
                    AccountsData = Main.getRecentAccInfo("accounts");
                } catch (SQLException | ClassNotFoundException e) {
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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> BansData = null;
                try {
                    BansData = Main.getRecentAccInfo("bans");
                } catch (SQLException | ClassNotFoundException e) {
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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> MutesData = null;
                try {
                    MutesData = Main.getRecentAccInfo("mutes");
                } catch (SQLException | ClassNotFoundException e) {
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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                List<String> BlacklistData = null;
                try {
                    BlacklistData = Main.getRecentAccInfo("blacklists");
                } catch (SQLException | ClassNotFoundException e) {
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
        if (event.getSelectMenu().getId().equalsIgnoreCase("menu:modaction")) {
            try {
                String[] data = modMap.get(event.getUser().getId());
                SelectMenu fakemenu = event.getSelectMenu().createCopy().setPlaceholder("Command Selected").setDisabled(true).build();
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-cancel")) {
                    modMap.remove(event.getUser().getId());
                    modConfirmBypass.remove(event.getUser().getId());
                    modMapUser.remove(event.getUser().getId());
                    event.reply("Command Cancelled!").setEphemeral(true).queue();
                    event.editSelectMenu(fakemenu).queue();
                    return;
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-ca")) {
                    if (modConfirmBypass.get(event.getUser().getId())) {
                        User target = modMapUser.get(event.getUser().getId());
                        Guild g = getJDA().getGuildById(GuildID);
                        g.ban(UserSnowflake.fromId(target.getId()), 1, ModerationCommandAction.SCAMBAN.getDefaultReason()).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason());
                        Main.insertCase(target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), event.getUser());
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    } else {
                        User target = modMapUser.get(event.getUser().getId());
                        EmbedBuilder confirm_msg = new EmbedBuilder();
                        confirm_msg.setTitle("Confirm Moderation Action?");
                        confirm_msg.setDescription("**User:** **`" + target.getAsTag() + "`** | (**`" + target.getId() + "`** \n"
                                + "**Action:** **`" + ModerationCommandAction.SCAMBAN.getActionLabel() + "`** \n"
                                + "**Reason:** **`" + ModerationCommandAction.SCAMBAN.getDefaultReason() + "`**"
                        );
                        confirm_msg.setFooter(footer);
                        confirm_msg.setTimestamp(new Date().toInstant());
                        confirm_msg.setColor(new Color(253, 216, 1));
                        SelectMenu confirm_menu = SelectMenu.create("menu:modaction-confirm")
                                .addOption("Yes", "confirm-yes")
                                .addOption("No", "confirm-no")
                                .setPlaceholder("Confirm this action?")
                                .setRequiredRange(1, 1)
                                .build();
                        data[1] = ModerationCommandAction.SCAMBAN.getActionLabel();
                        data[2] = ModerationCommandAction.SCAMBAN.getDefaultReason();
                        modMap.put(event.getUser().getId(), data);
                        event.replyEmbeds(confirm_msg.build()).addActionRow(confirm_menu).setEphemeral(true).queue();
                        confirm_msg.clear();
                        confirm_msg = null;
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    }
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-uau")) {
                    if (modConfirmBypass.get(event.getUser().getId())) {
                        User target = modMapUser.get(event.getUser().getId());
                        Guild g = getJDA().getGuildById(GuildID);
                        g.ban(UserSnowflake.fromId(target.getId()), 1, ModerationCommandAction.UNDERAGE.getDefaultReason()).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason());
                        Main.insertCase(target, ModerationCommandAction.UNDERAGE, data[2], event.getUser());
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    } else {
                        User target = modMapUser.get(event.getUser().getId());
                        EmbedBuilder confirm_msg = new EmbedBuilder();
                        confirm_msg.setTitle("Confirm Moderation Action?");
                        confirm_msg.setDescription("**User:** **`" + target.getAsTag() + "`** | (**`" + target.getId() + "`**) \n"
                                + "**Action:** **`" + ModerationCommandAction.UNDERAGE.getActionLabel() + "`** \n"
                                + "**Reason:** **`" + ModerationCommandAction.UNDERAGE.getDefaultReason() + "`**"
                        );
                        confirm_msg.setFooter(footer);
                        confirm_msg.setTimestamp(new Date().toInstant());
                        confirm_msg.setColor(new Color(253, 216, 1));
                        SelectMenu confirm_menu = SelectMenu.create("menu:modaction-confirm")
                                .addOption("Yes", "confirm-yes")
                                .addOption("No", "confirm-no")
                                .setPlaceholder("Confirm this action?")
                                .setRequiredRange(1, 1)
                                .build();
                        data[1] = ModerationCommandAction.UNDERAGE.getActionLabel();
                        data[2] = ModerationCommandAction.UNDERAGE.getDefaultReason();
                        modMap.put(event.getUser().getId(), data);
                        event.replyEmbeds(confirm_msg.build()).addActionRow(confirm_menu).setEphemeral(true).queue();
                        confirm_msg.clear();
                        confirm_msg = null;
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    }
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-w")) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.WARN.getActionLabel();
                    return;
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-k")) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.KICK.getActionLabel();
                    return;
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-b")) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.BAN.getActionLabel();
                    return;
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-t")) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    TextInput timeunitInput = TextInput.create("mac:to:duration", "Duration of Timeout", TextInputStyle.SHORT)
                            .setRequired(true)
                            .setPlaceholder("Insert timeout duration (Max 28 days)")
                            .setRequiredRange(1, 2)
                            .build();
                    TextInput timeunitUnitInput = TextInput.create("mac:to:unit", "TimeUnit.valueOf() Unit.", TextInputStyle.SHORT)
                            .setRequired(true)
                            .setPlaceholder("Units: DAYS | HOURS | MINUTES | SECONDS")
                            .setRequiredRange(1, 50)
                            .build();

                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(
                            ActionRow.of(reasonInput),
                            ActionRow.of(timeunitInput),
                            ActionRow.of(timeunitUnitInput)
                    ).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.TIMEOUT.getActionLabel();
                    return;
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-vb")) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.VIRTUALBAN.getActionLabel();
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                event.reply("Something went wrong!").setEphemeral(true).queue();
            }
        }


        if (event.getSelectMenu().getId().equalsIgnoreCase("menu:modaction-confirm")) {
            try {
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("confirm-yes")) {
                    SelectMenu fakemenu = event.getSelectMenu().createCopy().setDisabled(true).setPlaceholder("Confirm Selected").build();
                    String[] data = modMap.get(event.getUser().getId());
                    User target = modMapUser.get(event.getUser().getId());
                    Guild g = getJDA().getGuildById(GuildID);
                    String reason = data[2];
                    if (data[1].equalsIgnoreCase("kick")) {
                        ModerationCommandAction action = ModerationCommandAction.KICK;
                        Main.insertCase(target,action,reason,event.getUser());
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action, reason);
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();

                        MessageEmbed vbPM = Main.generateKickEmbed(reason);
                        sendMessageKick(target,vbPM);

                        g.kick(UserSnowflake.fromId(target.getId()), reason).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ban")) {
                        ModerationCommandAction action = ModerationCommandAction.BAN;
                        Main.insertCase(target,action,reason,event.getUser());
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action, reason);
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();

                        MessageEmbed vbPM = Main.generateBanEmbed(reason);
                        sendMessage(target,vbPM, "2");

                        g.ban(UserSnowflake.fromId(target.getId()), 1, reason).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    }
                    if (data[1].equalsIgnoreCase("warning")) {
                        ModerationCommandAction action = ModerationCommandAction.WARN;
                        Main.insertCase(target, action, data[2], event.getUser());
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, action, reason);
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();

                        MessageEmbed warn = Main.generatewarnEmbed(reason);
                        sendMessage(target, warn);

                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        event.editSelectMenu(fakemenu).queue();
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ca-ban")) {
                        g.ban(UserSnowflake.fromId(target.getId()), 1, ModerationCommandAction.SCAMBAN.getDefaultReason()).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason());
                        Main.insertCase(target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), event.getUser());
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        event.editSelectMenu(fakemenu).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        return;
                    }
                    if (data[1].equalsIgnoreCase("underage-ban")) {
                        g.ban(UserSnowflake.fromId(target.getId()), 1, ModerationCommandAction.UNDERAGE.getDefaultReason()).queue();
                        MessageEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason());
                        Main.insertCase(target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason(), event.getUser());
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();

                        MessageEmbed vbPM = Main.generateBanEmbed(ModerationCommandAction.UNDERAGE.getDefaultReason());
                        sendMessage(target,vbPM, "2");

                        event.editSelectMenu(fakemenu).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        return;
                    }
                    if (data[1].equalsIgnoreCase("timeout")) {
                        ModerationCommandAction action = ModerationCommandAction.TIMEOUT;
                        String[] todata = modTimeout.get(event.getUser().getId());
                        String unit = todata[0];
                        assert unit != null;
                        assert todata[1] != null;
                        unit.trim();
                        int duration = Integer.parseInt(todata[1].trim());
                        if (!unit.equalsIgnoreCase("seconds") && !unit.equalsIgnoreCase("minutes") && !unit.equalsIgnoreCase("hours") && !unit.equalsIgnoreCase("days")) {
                                event.reply("Invalid TimeUnit! MAC Cancelled!").setEphemeral(true).queue();
                                removeUserFromMACMaps(event.getUser().getId());
                                return;
                        }
                        TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
                        if (timeUnit == TimeUnit.DAYS && duration > 28 ||
                                timeUnit == TimeUnit.HOURS && duration > 672 ||
                                timeUnit == TimeUnit.MINUTES && duration > 40320 ||
                                timeUnit == TimeUnit.SECONDS && duration > 2419200) {
                            event.reply("Invalid Duration! MAC Cancelled!").queue();
                            removeUserFromMACMaps(event.getUser().getId());
                            return;
                        }
                        try {
                            MessageEmbed log = Main.generateModlog(event.getUser(), target, action,reason);
                            Main.insertCase(target, action,data[2],event.getUser());
                            g.getMember(UserSnowflake.fromId(modMapUser.get(event.getUser().getId()).getId())).timeoutFor(duration,timeUnit).queue();
                            g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();

                            MessageEmbed vbPM = Main.generateBanEmbed(reason);
                            sendMessage(target,vbPM, "1");

                            event.reply("Moderation Completed!").setEphemeral(true).queue();
                            removeUserFromMACMaps(event.getUser().getId());
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data[1].equalsIgnoreCase("virtual-ban")) {
                        ModerationCommandAction action = ModerationCommandAction.VIRTUALBAN;
                        MessageEmbed log = Main.generateModlog(event.getUser(),target,action,reason);
                        MessageEmbed vbPM = Main.generateVirtualBanEmbed(reason);
                        Main.insertCase(target,action,reason,event.getUser());
                        sendMessage(target,vbPM, "3");
                        g.getTextChannelById(ModlogChannelID).sendMessageEmbeds(log).queue();
                        g.addRoleToMember(UserSnowflake.fromId(target.getId()),g.getRoleById(VirtualBanRoleID)).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("confirm-no")) {
                    removeUserFromMACMaps(event.getUser().getId());
                    event.reply("Moderation Cancelled!").setEphemeral(true).queue();
                    event.editSelectMenu(event.getSelectMenu().createCopy().setDisabled(true).setPlaceholder("MAC Cancelled").build()).queue();
                }
            }  catch (SQLException | ClassNotFoundException | NullPointerException  | IllegalArgumentException e){
                e.printStackTrace();
            }

        }
    }

}