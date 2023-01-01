package net.twistedmc.shield.mab;


import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.BungeeCord;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Images;
import net.twistedmc.shield.Util.ModerationCommandAction;
import net.twistedmc.shield.Util.Utils;
import net.twistedmc.shield.mab.commands.HelpCommand;
import net.twistedmc.shield.mab.commands.InfoCommand;
import net.twistedmc.shield.mab.commands.bwmarketplace.EmbedAdvertisementCommand;
import net.twistedmc.shield.mab.commands.mabadmin.*;
import net.twistedmc.shield.mab.commands.moderation.ModerateCommand;
import net.twistedmc.shield.mab.commands.moderation.PurgeCommand;
import net.twistedmc.shield.mab.commands.moderation.SearchCaseCommand;
import net.twistedmc.shield.mab.commands.moderation.TimeoutCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.AppealURLCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.JoinLoggingCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.MessageLoggingCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.PunishmentLoggingCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.toggle.ToggleAppealCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.toggle.ToggleJoinLoggingCommand;
import net.twistedmc.shield.mab.commands.moderation.logging.toggle.ToggleMessageLoggingCommand;
import net.twistedmc.shield.mab.music.commands.*;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MAB extends ListenerAdapter {

    // MAIN VARS
    private String token;
    public static JDA jda;
    public static boolean StatusNotifs = false; // ????

    // STRINGS
    public static String GuildID = "549595806009786388";
    public static String VirtualBanRoleID = "837168599814373416";
    public static String SyncRoleID = "797737410514780201";

    // HASHMAPS
    public static HashMap<String,String[]> modMap = new HashMap<>(); // Key: DiscordID of sender; Values: String[] = {userID,Action,Reason}; (3)
    public static HashMap<String,User> modMapUser = new HashMap<>();
    public static HashMap<String,Boolean> modConfirmBypass = new HashMap<>(); // Key: DiscordID of sender; Value: true or false
    public static HashMap<String,String[]> modTimeout = new HashMap<>();
    public static HashMap<String,Boolean> appealUserConfirmed = new HashMap<>(); // K: DID of sender; V: t/f
    public static HashMap<User,User> timeoutMember = new HashMap<>();
    public static HashMap<User,String> guildID = new HashMap<>();
    public static HashMap[] maps = {modMap,modMapUser,modConfirmBypass,modTimeout,appealUserConfirmed};
    public static ArrayList<HashMap> macMaps = new ArrayList<>();
    public final Map<Long, Message> messageMap = new HashMap<>();
    public static HashMap<User,String> userGuildHashMap = new HashMap<>();

    // OTHER
    public static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    public static int year = calendar.get(Calendar.YEAR);
    public static String footer = "© " + year + " TwistedMC Studios v" + Main.botVersion;

    // SHARDS
    private static int shardsTotal = 1;
    public static ShardManager shardManager;
    private static boolean shardAutoMode = true;

    public MAB(String token) {
        this.token = token;
    }

    public void start() throws LoginException, InterruptedException {
        shardsTotal = 1;

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
                .createDefault(token, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setShardsTotal(shardsTotal);

        shardManager = builder.build();
        jda = shardManager.retrieveApplicationInfo().getJDA();

        shardManager.addEventListener(this);
        // MUSIC //
        shardManager.addEventListener(new ClearQueueCommand());
        shardManager.addEventListener(new JoinCommand());
        shardManager.addEventListener(new PlayCommand());
        shardManager.addEventListener(new QueueCommand());
        shardManager.addEventListener(new StopCommand());
        shardManager.addEventListener(new SkipCommand());
        shardManager.addEventListener(new RepeatCommand());
        shardManager.addEventListener(new NowPlayingCommand());
        // BW MARKETPLACE //
        shardManager.addEventListener(new EmbedAdvertisementCommand());
        // MAB ADMIN //
        shardManager.addEventListener(new ActivateBetaCommand());
        shardManager.addEventListener(new BanGuildCommand());
        shardManager.addEventListener(new GuildInfoCommand());
        shardManager.addEventListener(new ShutdownCommand());
        shardManager.addEventListener(new EmbedCommand());
        // MODERATION LOG TOGGLE //
        shardManager.addEventListener(new ToggleAppealCommand());
        shardManager.addEventListener(new ToggleJoinLoggingCommand());
        shardManager.addEventListener(new ToggleMessageLoggingCommand());
        // MODERATION LOGGING //
        shardManager.addEventListener(new AppealURLCommand());
        shardManager.addEventListener(new JoinLoggingCommand());
        shardManager.addEventListener(new MessageLoggingCommand());
        shardManager.addEventListener(new PunishmentLoggingCommand());
        // MODERATION //
        shardManager.addEventListener(new ModerateCommand());
        shardManager.addEventListener(new PurgeCommand());
        shardManager.addEventListener(new SearchCaseCommand());
        shardManager.addEventListener(new TimeoutCommand());
        // GENERAL //
        shardManager.addEventListener(new HelpCommand());
        shardManager.addEventListener(new InfoCommand());






        shardManager.setPresence(OnlineStatus.IDLE, Activity.playing("Starting up.."));

        macMaps.add(modMap);
        macMaps.add(modConfirmBypass);
        macMaps.add(modMapUser);
        macMaps.add(modTimeout);
        macMaps.add(timeoutMember);
        macMaps.add(guildID);
        macMaps.add(userGuildHashMap);

        shardManager.retrieveApplicationInfo().getJDA().awaitReady();

        shardManager.getShards().forEach(jda -> {
            jda.updateCommands()
                    .addCommands(Commands.slash("help", "MAB Help Command")
                            .setGuildOnly(true))

                    .addCommands(Commands.context(Command.Type.USER, "Moderate User")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)))

                    .addCommands(Commands.slash("music", "Main Music Command")
                            .setGuildOnly(true)
                            .addSubcommands(new SubcommandData("play", "Play music")
                                    .addOption(OptionType.STRING, "url", "YouTube URL", true))
                            .addSubcommands(new SubcommandData("join", "Join current voice channel"))
                            .addSubcommands(new SubcommandData("queue", "View music queue"))
                            .addSubcommands(new SubcommandData("nowplaying", "[NEW] View current song"))
                            .addSubcommands(new SubcommandData("skip", "[NEW] Skip current song"))
                            .addSubcommands(new SubcommandData("repeat", "[NEW] Toggle repeat mode"))
                            .addSubcommands(new SubcommandData("clearqueue", "Clear music queue"))
                            .addSubcommands(new SubcommandData("stop", "Disconnect MAB from channel and clear music queue")))

                    .addCommands(Commands.slash("mab", "Main MAB Command")
                            .setGuildOnly(true)
                            .addSubcommands(new SubcommandData("info", "Bot Info")))

                    .addCommands(Commands.slash("mabsettings", "Customize MAB")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                            .addSubcommands(new SubcommandData("joinlog", "Set join log channel")
                                    .addOption(OptionType.CHANNEL, "channel", "Channel for join logs", true))
                            .addSubcommands(new SubcommandData("punishmentlog", "Set punishment log channel")
                                    .addOption(OptionType.CHANNEL, "channel", "Channel for punishment logs", true))
                            .addSubcommands(new SubcommandData("messagelog", "Set message log channel")
                                    .addOption(OptionType.CHANNEL, "channel", "Channel for message logs", true))
                            .addSubcommands(new SubcommandData("appealurl", "Set an appeal url for users to appeal at when they are timed out or banned")
                                    .addOption(OptionType.STRING, "appeallink", "Appeal url", true))
                            .addSubcommands(new SubcommandData("toggleappeal", "Toggle appeal url"))
                            .addSubcommands(new SubcommandData("togglejoinlogs", "Toggle join logs"))
                            .addSubcommands(new SubcommandData("togglemessagelogs", "Toggle message logs")))

                    .addCommands(Commands.slash("moderate", "Moderate a user with different options")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                            .addOption(OptionType.USER, "user", "User to Moderate", true)
                            .addOption(OptionType.BOOLEAN, "bypass", "Bypass the final confirmation?", false))

                    .addCommands(Commands.slash("timeout", "Quickly Timeout a user")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                            .addOption(OptionType.USER, "user", "User to Timeout", true))

                    .addCommands(Commands.slash("searchcase", "View a Discord Punishment Case")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                            .addOption(OptionType.STRING, "caseid", "Case ID of the punishment", true))

                    .addCommands(Commands.slash("purge", "Purge messages")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                            .addOption(OptionType.STRING, "number", "Number of messages to delete", true))
                    .queue();
        });

        Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(1001147856687742996L))).upsertCommand("bwmarketplace", "BW Marketplace Custom Commands")
                .setGuildOnly(true)
                .addSubcommands(new SubcommandData("embedadvertisement", "Create Embed Advertisement")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to post in", true)
                        .addOption(OptionType.STRING, "name", "Plugin/Setup Name", true)
                        .addOption(OptionType.STRING, "description", "Plugin/Setup Description", true)
                        .addOption(OptionType.STRING, "price", "Plugin/Setup Price", true)
                        .addOption(OptionType.MENTIONABLE, "creator", "Creator of plugin/setup", false)
                        .addOption(OptionType.STRING, "test_server", "Plugin/Setup Test Server", false)
                        .addOption(OptionType.STRING, "builtbybit_link", "BuiltByBit Link", false)
                        .addOption(OptionType.STRING, "spigot_link", "Spigot Link", false)
                        .addOption(OptionType.STRING, "polymart_link", "Polymart Link", false)
                        .addOption(OptionType.STRING, "support_server", "Support Server Link", false)
                        .addOption(OptionType.STRING, "logo_url", "Top Right Corner Logo URL", false)
                        .addOption(OptionType.INTEGER, "rgb_r", "RGB R", false)
                        .addOption(OptionType.INTEGER, "rgb_g", "RGB G", false)
                        .addOption(OptionType.INTEGER, "rgb_b", "RGB B", false)
                ).queue();

        Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(549595806009786388L))).upsertCommand("mabadmin", "MAB Admin Commands")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addSubcommands(new SubcommandData("guildinfo", "Retrieve Guild Info")
                        .addOption(OptionType.STRING, "guildid", "Guild ID", true))
                .addSubcommands(new SubcommandData("banguild", "Ban a guild from using MAB")
                        .addOption(OptionType.STRING, "guildid", "Guild ID", true)
                        .addOption(OptionType.STRING, "reason", "Reason for ban", true))
                .addSubcommands(new SubcommandData("beta", "Activate beta features for specified guild")
                        .addOption(OptionType.STRING, "guildid", "Guild ID", true))
                .addSubcommands(new SubcommandData("maintenance","Put the bot into maintenance"))
                .addSubcommands(new SubcommandData("shutdown","Shutdown MAB"))
                .addSubcommands(new SubcommandData("embed","Create embed")
                        .addOption(OptionType.STRING, "title", "Embed Title", true)
                        .addOption(OptionType.STRING, "desc", "Embed Description", true)
                        .addOption(OptionType.CHANNEL, "channel", "Channel to send embed in", true)
                        .addOption(OptionType.BOOLEAN, "button", "Send button with embed", true)
                        .addOption(OptionType.STRING, "buttontitle", "Button Title", true)
                        .addOption(OptionType.STRING, "buttonurl", "Button URL", true))
                .queue();

        completeOnline();

        reloadShards();
    }

    public static void stop(){
        shardManager.shutdown();
        Arrays.stream(maps).forEach(HashMap::clear);
        macMaps.clear();
        GuildInfoCommand.missingPermissions.clear();
    }

    public ShardManager getShardManager() {
        return this.shardManager;
    }

    public static void startShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Integer> activeIds = new ArrayList<>();
                for(JDA jda : shardManager.getShards()) {
                    activeIds.add(jda.getShardInfo().getShardId());
                }
                Collections.sort(activeIds);
                for(int i = 0; i < shardManager.getShardsTotal(); i++) {
                    if(!activeIds.contains(i)) {
                        shardManager.start(i);
                    }
                }
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Started all shards");
            }
        }).start();
    }

    public static void stopShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(JDA jda : MAB.shardManager.getShards()) {
                    shardManager.shutdown(jda.getShardInfo().getShardId());
                }
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Stopped and remove all shards");
            }
        }).start();
    }

    public static void restartShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startShards();
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Restarted all shards");
            }
        }).start();
    }

    public static void setShardAutoMode(boolean mode) {
        shardAutoMode = mode;
    }

    public static boolean getShardAutoMode() {
        return shardAutoMode;
    }

    public static void reloadShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(shardManager.getShardsRunning() < shardManager.getShardsTotal()) {
                    if(getShardAutoMode()) {
                        BungeeCord.getInstance().getLogger().log(Level.INFO, "Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online. Auto restarting...");
                        startShards();
                    } else {
                        BungeeCord.getInstance().getLogger().log(Level.INFO, "[WARN] Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online");
                    }
                }

                shardManager.setPresence(OnlineStatus.ONLINE, Activity.playing("Happy New Years!"));

                /*if(completeOnline()) {
                    shardManager.setPresence(OnlineStatus.ONLINE, Activity.playing("/help"));
                } else {
                    shardManager.setPresence(OnlineStatus.IDLE, Activity.playing("Limited functionality"));
                }*/
            }
        }).start();
    }

    public static boolean completeOnline() {
        boolean status = true;
        for(JDA jda : shardManager.getStatuses().keySet()) {
            if(jda.getStatus() != JDA.Status.CONNECTED) {
                status = false;
            }
        }
        return (shardManager.getShardsRunning() == shardManager.getShardsTotal()) && status;
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
                .flatMap(channel -> channel.sendMessage(content).addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse")))
                .queue();
    }

    public static void sendMessage(User user, MessageEmbed embed) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed).addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse")))
                .queue();
    }

    public static void sendMessage(User user, MessageEmbed embed, String punishmentType, String guildID) throws SQLException, ClassNotFoundException {
        Guild g = jda.getGuildById(guildID);

        if (g.getOwnerIdLong() == 478410064919527437L) {
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Appeal this punishment")))
                    .queue();
        } else {
            if (Main.appealEnabled(Long.parseLong(guildID))) {
                user.openPrivateChannel()
                        .flatMap(channel -> {
                            try {
                                return channel.sendMessageEmbeds(embed).addActionRow(Button.link(Main.getAppealLink(guildID), "Appeal this punishment")).addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse"));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .queue();
            } else if (!Main.appealEnabled(Long.parseLong(guildID))) {
                user.openPrivateChannel()
                        .flatMap(channel -> {
                            return channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse"));
                        })
                        .queue();
            }
        }
    }

    public static void sendMessageKick(User user, MessageEmbed embed, String guildID) {
        Guild g = jda.getGuildById(guildID);

        if (g.getOwnerIdLong() == 478410064919527437L) {
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://discord.twistedmcstudios.com/", "Join back"), Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse")))
                    .queue();
        } else {
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Report abuse")))
                    .queue();
        }
    }

    public static void sendMessage(User user, String content, int delay) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue(m -> m.delete().queueAfter(delay, TimeUnit.SECONDS));
    }
    public static void sendMessage(User user, String content,Collection<MessageEmbed> embeds, int delay) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content).setEmbeds(embeds))
                .queue(m -> m.delete().queueAfter(delay, TimeUnit.SECONDS));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("sendmanageserverdm")) {
            event.editButton(event.getButton().asDisabled()).queue();

            Guild guild = shardManager.getGuildById(String.valueOf(userGuildHashMap.get(event.getUser())));

            guild.getOwner().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(
                    "**ALERT!** MAB is missing MANAGE_SERVER permission in **" + guild.getName() + "**! Please enable MANAGE_SERVER permission for MAB to prevent functionality issues.").queue());

            userGuildHashMap.remove(event.getUser());
            GuildInfoCommand.missingPermissions.remove(event.getUser());

        }
    }

    private String formatTime(long duration) {
        final long hours = duration / TimeUnit.HOURS.toMillis(1);
        final long minutes = duration / TimeUnit.MINUTES.toMillis(1);
        final long seconds = duration % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
            eb.setFooter("© " + year + " TwistedMC Studios", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            eb.setTimestamp(new Date().toInstant());

            ticket.sendMessageEmbeds(eb.build()).setMessageReference(event.getMember().getAsMention() + " use this channel to post any other information or screenshots that can help us out :)").queue();
            event.reply("Thank you for your bug report!").setEphemeral(true).queue();
        }
        if (event.getModalId().equalsIgnoreCase("timeout:timeout")) {
            Guild g = event.getJDA().getGuildById(event.getGuild().getId());
            ModerationCommandAction action = ModerationCommandAction.TIMEOUT;
            String reasonValue = event.getValue("timeout:reason").getAsString();
            String durationValue = event.getValue("timeout:duration").getAsString();
            String timeunitValue = event.getValue("timeout:timeunit").getAsString();

            if (durationValue.matches("[a-zA-Z]+")){
                event.reply("<:squareexclamationred:1058119075789803650> Invalid Duration! Integer required!").setEphemeral(true).queue();
                timeoutMember.remove(event.getUser());
                guildID.remove(event.getUser());
                return;
            }

            int duration = Integer.parseInt(durationValue);

            if (!timeunitValue.equalsIgnoreCase("seconds") && !timeunitValue.equalsIgnoreCase("minutes") && !timeunitValue.equalsIgnoreCase("hours") && !timeunitValue.equalsIgnoreCase("days")) {
                event.reply("<:squareexclamationred:1058119075789803650> Invalid Timeunit! MAC Cancelled!").setEphemeral(true).queue();
                timeoutMember.remove(event.getUser());
                guildID.remove(event.getUser());
                return;
            }
            TimeUnit timeUnit = TimeUnit.valueOf(timeunitValue.toUpperCase());
            if (timeUnit == TimeUnit.DAYS && duration > 28 ||
                    timeUnit == TimeUnit.HOURS && duration > 672 ||
                    timeUnit == TimeUnit.MINUTES && duration > 40320 ||
                    timeUnit == TimeUnit.SECONDS && duration > 2419200) {
                event.reply("<:squareexclamationred:1058119075789803650> Invalid Duration! MAC Cancelled!").queue();
                timeoutMember.remove(event.getUser());
                guildID.remove(event.getUser());
                return;
            }
            try {
                User user = timeoutMember.get(event.getUser());
                assert g != null;
                Member member = g.getMember(user);

                EnumSet<Permission> memPerms = member.getPermissions();
                boolean memIsAdmin = memPerms.toString().contains(Permission.ADMINISTRATOR.name());

                if (member.isTimedOut()) {
                    event.reply("This user is already timed out!").setEphemeral(true).queue();
                    timeoutMember.remove(event.getUser());
                    guildID.remove(event.getUser());
                    return;
                }

                if (memIsAdmin) {
                    event.reply("You cannot timeout this user!").setEphemeral(true).queue();
                    timeoutMember.remove(event.getUser());
                    guildID.remove(event.getUser());
                } else if (!memIsAdmin) {
                    String caseID = "#" + Main.generateRandomID(7);
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
                    if (timeUnit.equals(TimeUnit.DAYS)) {
                        calendar.add(Calendar.DATE,duration);
                    }
                    if (timeUnit.equals(TimeUnit.HOURS)) {
                        calendar.add(Calendar.HOUR,duration);
                    }
                    if (timeUnit.equals(TimeUnit.MINUTES)) {
                        calendar.add(Calendar.MINUTE,duration);
                    }
                    if (timeUnit.equals(TimeUnit.SECONDS)) {
                        calendar.add(Calendar.SECOND,duration);
                    }
                    SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a 'EST'");

                    WebhookEmbed log = Main.generateModlog(event.getUser(), timeoutMember.get(event.getUser()), action, reasonValue,caseID,"" + duration + " " +timeUnit,format.format(calendar.getTime()),guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                    assert g != null;
                    TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                    Webhook s = null;
                    assert channel != null;
                    for (Webhook h : channel.retrieveWebhooks().complete())
                        if (h.getName().equals("MAB")) {
                            s = h;
                            break;
                        }

                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                    webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                    WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                    builder.setThreadFactory((job) -> {
                        Thread thread = new Thread(job);
                        thread.setName("Hello");
                        thread.setDaemon(true);
                        return thread;
                    });
                    builder.setWait(true);
                    club.minnced.discord.webhook.WebhookClient client = builder.build();

                    client.send(webhookMessageBuilder.addEmbeds(log).build());

                    Main.insertCase(timeoutMember.get(event.getUser()), action, reasonValue, event.getUser(), caseID, guildID.get(event.getUser()));

                    event.getJDA().getGuildById(event.getGuild().getId()).getMemberById(user.getId()).timeoutFor(Long.parseLong(String.valueOf(duration)), TimeUnit.valueOf(timeunitValue)).queue();

                    MessageEmbed vbPM = Main.generateTimeoutEmbed(reasonValue,caseID,"" + duration + " " +timeUnit,format.format(calendar.getTime()), event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    sendMessage(timeoutMember.get(event.getUser()), vbPM, "1", guildID.get(event.getUser()));

                    event.reply("<:squarechecksolid:1057753652602867813> Moderation Completed!").setEphemeral(true).queue();
                    timeoutMember.remove(event.getUser());
                    guildID.remove(event.getUser());
                }
                return;
            } catch (NullPointerException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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
            eb.setFooter("© " + year + " TwistedMC Studios", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
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
                Guild g = event.getJDA().getGuildById(event.getGuild().getId());
                String reason = data[2];
                modMap.put(event.getUser().getId(), data);
                if (modConfirmBypass.get(event.getUser().getId())) {
                    if (data[1].equalsIgnoreCase("kick")) {
                        ModerationCommandAction action = ModerationCommandAction.KICK;
                        String caseID = "#" + Main.generateRandomID(7);
                        MessageEmbed vbPM = Main.generateKickEmbed(reason,caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        sendMessageKick(target,vbPM,guildID.get(event.getUser()));
                        Main.insertCase(target, action,data[2],event.getUser(), caseID, guildID.get(event.getUser()));
                        g.kick(UserSnowflake.fromId(target.getId()),reason).queue();
                        WebhookEmbed log = Main.generateModlog(event.getUser(), target, action,reason,caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        assert g != null;
                        TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                        Webhook s = null;
                        assert channel != null;
                        for (Webhook h : channel.retrieveWebhooks().complete())
                            if (h.getName().equals("MAB")) {
                                s = h;
                                break;
                            }

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                        webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                        builder.setThreadFactory((job) -> {
                            Thread thread = new Thread(job);
                            thread.setName("Hello");
                            thread.setDaemon(true);
                            return thread;
                        });
                        builder.setWait(true);
                        club.minnced.discord.webhook.WebhookClient client = builder.build();

                        client.send(webhookMessageBuilder.addEmbeds(log).build());

                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ban")) {
                        ModerationCommandAction action = ModerationCommandAction.BAN;
                        String caseID = "#" + Main.generateRandomID(7);
                        MessageEmbed vbPM = Main.generateBanEmbed(reason, caseID, guildID.get(event.getUser()), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        sendMessage(target,vbPM, "2", guildID.get(event.getUser()));

                        g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(reason).queue();

                        WebhookEmbed log = Main.generateModlog(event.getUser(), target, action,reason,caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        assert g != null;
                        TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                        Webhook s = null;
                        assert channel != null;
                        for (Webhook h : channel.retrieveWebhooks().complete())
                            if (h.getName().equals("MAB")) {
                                s = h;
                                break;
                            }

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                        webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                        builder.setThreadFactory((job) -> {
                            Thread thread = new Thread(job);
                            thread.setName("Hello");
                            thread.setDaemon(true);
                            return thread;
                        });
                        builder.setWait(true);
                        club.minnced.discord.webhook.WebhookClient client = builder.build();

                        client.send(webhookMessageBuilder.addEmbeds(log).build());

                        Main.insertCase(target, action,data[2],event.getUser(), caseID, guildID.get(event.getUser()));
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("warning")) {
                        ModerationCommandAction action = ModerationCommandAction.WARN;
                        String caseID = "#" + Main.generateRandomID(7);
                        WebhookEmbed log = Main.generateModlog(event.getUser(), target, action,reason,caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        assert g != null;
                        TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                        Webhook s = null;
                        assert channel != null;
                        for (Webhook h : channel.retrieveWebhooks().complete())
                            if (h.getName().equals("MAB")) {
                                s = h;
                                break;
                            }

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                        webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());


                        WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                        builder.setThreadFactory((job) -> {
                            Thread thread = new Thread(job);
                            thread.setName("Hello");
                            thread.setDaemon(true);
                            return thread;
                        });
                        builder.setWait(true);
                        club.minnced.discord.webhook.WebhookClient client = builder.build();

                        client.send(webhookMessageBuilder.addEmbeds(log).build());

                        Main.insertCase(target, action,data[2],event.getUser(), caseID, guildID.get(event.getUser()));
                        MessageEmbed warn = Main.generatewarnEmbed(reason,caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        sendMessage(target,warn);
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("virtual-ban")) {
                        ModerationCommandAction action = ModerationCommandAction.VIRTUALBAN;
                        String caseID = "#" + Main.generateRandomID(7);

                        WebhookEmbed log = Main.generateModlog(event.getUser(), target, action,reason,caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        assert g != null;
                        TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                        Webhook s = null;
                        assert channel != null;
                        for (Webhook h : channel.retrieveWebhooks().complete())
                            if (h.getName().equals("MAB")) {
                                s = h;
                                break;
                            }

                        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                        webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                        builder.setThreadFactory((job) -> {
                            Thread thread = new Thread(job);
                            thread.setName("Hello");
                            thread.setDaemon(true);
                            return thread;
                        });
                        builder.setWait(true);
                        club.minnced.discord.webhook.WebhookClient client = builder.build();

                        MessageEmbed vbPM = Main.generateVirtualBanEmbed(reason,caseID, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        Main.insertCase(target,action,reason,event.getUser(),caseID, guildID.get(event.getUser()));
                        sendMessage(target,vbPM, "3", guildID.get(event.getUser()));
                        // VIRTUAL BAN PROCESS BEGIN
                        Main.deSyncUser(target,GuildID,SyncRoleID,event.getJDA());
                        Main.compileAndRemoveRoles(target,GuildID,event.getJDA());
                        // VIRTUAL BAN PROCESS END
                        client.send(webhookMessageBuilder.addEmbeds(log).build());
                        g.addRoleToMember(UserSnowflake.fromId(target.getId()),g.getRoleById(VirtualBanRoleID)).queue();
                        event.reply("Moderation Complete!").setEphemeral(true).queue();
                        removeUserFromMACMaps(event.getUser().getId());
                        return;
                    }
                    if (data[1].equalsIgnoreCase("timeout")) {
                        ModerationCommandAction action = ModerationCommandAction.TIMEOUT;
                        String caseID = "#" + Main.generateRandomID(7);
                        String unit = event.getValue("mac:to:unit").getAsString();
                        int duration = Integer.parseInt(event.getValue("mac:to:duration").getAsString());
                        TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
                        if (timeUnit == TimeUnit.DAYS && duration > 28 ||
                                timeUnit == TimeUnit.HOURS && duration > 672 ||
                                timeUnit == TimeUnit.MINUTES && duration > 40320 ||
                                timeUnit == TimeUnit.SECONDS && duration > 2419200) {
                            event.reply("<:squareexclamationred:1058119075789803650> Invalid Duration! MAC Cancelled!").queue();
                            removeUserFromMACMaps(event.getUser().getId());
                            return;
                        }
                        try {
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
                            if (timeUnit.equals(TimeUnit.DAYS)) {
                                calendar.add(Calendar.DATE,duration);
                            }
                            if (timeUnit.equals(TimeUnit.HOURS)) {
                                calendar.add(Calendar.HOUR,duration);
                            }
                            if (timeUnit.equals(TimeUnit.MINUTES)) {
                                calendar.add(Calendar.MINUTE,duration);
                            }
                            if (timeUnit.equals(TimeUnit.SECONDS)) {
                                calendar.add(Calendar.SECOND,duration);
                            }
                            SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a 'EST'");

                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, action,reason,caseID,"" + duration + " " + timeUnit, format.format(calendar.getTime()),guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            Main.insertCase(target, action,data[2],event.getUser(), caseID, guildID.get(event.getUser()));

                            User user = timeoutMember.get(event.getUser());

                            event.getJDA().getGuildById(event.getGuild().getId()).getMemberById(user.getId()).timeoutFor(Long.parseLong(event.getValue("mac:to:duration").getAsString()), TimeUnit.valueOf(unit)).queue();

                            MessageEmbed vbPM = Main.generateTimeoutEmbed(reason,caseID,"" + duration + " " + timeUnit, format.format(calendar.getTime()), event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            sendMessage(target,vbPM, "1", guildID.get(event.getUser()));

                            event.reply("<:squarechecksolid:1057753652602867813> Moderation Completed!").setEphemeral(true).queue();
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
                        confirm_msg.setDescription("**User:** " + target.getAsMention() + "\nID: " + target.getId() + " \n"
                                + "**Action:** " + data[1] + "\n"
                                + "**Duration:** " + event.getValue("mac:to:duration").getAsString() + "\n"
                                + "**Timeunit:** " + event.getValue("mac:to:unit").getAsString().toLowerCase() + "\n"
                                + "**Reason:** " + event.getValue("mac:reasoninput").getAsString() + ""
                        );
                    } else {
                        confirm_msg.setDescription("**User:** " + target.getAsMention() + "\nID: " + target.getId() + "\n"
                                + "**Action:** " + data[1] + "\n"
                                + "**Reason:** " + event.getValue("mac:reasoninput").getAsString() + ""
                        );
                    }
                    confirm_msg.setFooter(footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
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
                event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
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
                sb.append("**Total SHIELD Reports:** ").append(SRs).append("").append("\n\n__**Most Recent SHIELD Reports**__ \n");
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
                accountsSB.append("**Total Player Accounts:** ").append(Accs).append("").append("\n\n__**Most Recent Player Accounts**__ \n");
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
                bansSB.append("**Total Banned Players:** ").append(Bans).append("").append("\n\n__**Most Recent Player Bans**__ \n");
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
                mutesSB.append("**Total Muted Players:** ").append(Mutes).append("").append("\n\n__**Most Recent Player Mutes**__ \n");
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
                blSB.append("**Total Banned Players:** ").append(BLs).append("").append("\n\n__**Most Recent Player BlackLists**__ \n");
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
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(ModeratorRole);
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.BAN_MEMBERS)) {
                    if (modConfirmBypass.get(event.getUser().getId())) {
                        try {
                            String caseID = "#" + Main.generateRandomID(7);
                            User target = modMapUser.get(event.getUser().getId());
                            Guild g = event.getJDA().getGuildById(event.getGuild().getId());
                            g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(ModerationCommandAction.SCAMBAN.getDefaultReason()).queue();
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            Main.insertCase(target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), event.getUser(), caseID, guildID.get(event.getUser()));
                            event.editSelectMenu(fakemenu).queue();
                        /*permList.clear();
                        permList = null;*/
                        }
                        catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255,89,89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        }
                        catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255,89,89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    } else {
                        User target = modMapUser.get(event.getUser().getId());
                        EmbedBuilder confirm_msg = new EmbedBuilder();
                        confirm_msg.setTitle("Confirm Moderation Action?");
                        confirm_msg.setDescription("**User:** " + target.getAsMention() + "\nID: " + target.getId() + " \n"
                                + "**Action:** " + ModerationCommandAction.SCAMBAN.getActionLabel() + " \n"
                                + "**Reason:** " + ModerationCommandAction.SCAMBAN.getDefaultReason() + ""
                        );
                        confirm_msg.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
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
                        /*permList.clear();
                        permList = null;*/
                        return;
                    }
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.BAN_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-uau")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(ModeratorRole);
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.BAN_MEMBERS)) {
                    if (modConfirmBypass.get(event.getUser().getId())) {
                        try {
                            User target = modMapUser.get(event.getUser().getId());
                            String caseID = "#" + Main.generateRandomID(7);
                            Guild g = event.getJDA().getGuildById(event.getGuild().getId());
                            g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(ModerationCommandAction.UNDERAGE.getDefaultReason()).queue();
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason(), caseID,guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            Main.insertCase(target, ModerationCommandAction.UNDERAGE, data[2], event.getUser(), caseID, guildID.get(event.getUser()));
                            event.editSelectMenu(fakemenu).queue();
                        /*permList.clear();
                        permList = null;*/
                        }
                        catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255,89,89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        }
                        catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255,89,89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    } else {
                        User target = modMapUser.get(event.getUser().getId());
                        EmbedBuilder confirm_msg = new EmbedBuilder();
                        confirm_msg.setTitle("Confirm Moderation Action?");
                        confirm_msg.setDescription("**User:** " + target.getAsMention() + "\nID: " + target.getId() + " \n"
                                + "**Action:** " + ModerationCommandAction.UNDERAGE.getActionLabel() + " \n"
                                + "**Reason:** " + ModerationCommandAction.UNDERAGE.getDefaultReason() + ""
                        );
                        confirm_msg.setFooter(footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
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
                        /*permList.clear();
                        permList = null;*/
                        return;
                    }
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.BAN_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-w")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(HelperRole);
                permList.add(ModeratorRole);
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.MODERATE_MEMBERS)) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.WARN.getActionLabel();
                    /*permList.clear();
                    permList = null;*/
                    return;
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.MODERATE_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-k")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(ModeratorRole);
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.KICK_MEMBERS)) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.KICK.getActionLabel();
                    /*permList.clear();
                    permList = null;*/
                    return;
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.KICK_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-b")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.BAN_MEMBERS)) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.BAN.getActionLabel();
                    /*permList.clear();
                    permList = null;*/
                    return;
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.BAN_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-t")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(HelperRole);
                permList.add(ModeratorRole);
                permList.add(srModRole);
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.MODERATE_MEMBERS)) {

                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    TextInput timeunitInput = TextInput.create("mac:to:duration", "Duration of Timeout", TextInputStyle.SHORT)
                            .setRequired(true)
                            .setPlaceholder("Insert timeout duration (Max 28 days)")
                            .setRequiredRange(1, 10)
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
                    /*permList.clear();
                    permList = null;*/
                    return;
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.MODERATE_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
            if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("mac-vb")) {
                /*ArrayList<String> permList = new ArrayList<>();
                permList.add(AdminRole);
                permList.add(OwnerRole);*/
                if (event.getJDA().getGuildById(event.getGuild().getId()).getMember(event.getUser()).hasPermission(Permission.BAN_MEMBERS)) {
                    TextInput reasonInput = TextInput.create("mac:reasoninput", "Reason for Moderation", TextInputStyle.PARAGRAPH)
                            .setRequired(true)
                            .setPlaceholder("Insert reason for moderating this user")
                            .setRequiredRange(1, 2000)
                            .build();
                    Modal modal = Modal.create("mac:reason", "Reason for Moderation").addActionRows(ActionRow.of(reasonInput)).build();
                    event.replyModal(modal).queue();
                    event.editSelectMenu(fakemenu).queue();
                    data[1] = ModerationCommandAction.VIRTUALBAN.getActionLabel();
                    /*permList.clear();
                    permList = null;*/
                    return;
                } else {
                    EmbedBuilder emb = new EmbedBuilder();
                    emb.setDescription("<:squareexclamationred:1058119075789803650> You do not have permission to use this action!\n\n(" + Permission.MODERATE_MEMBERS.getName() + ")");
                    emb.setColor(new Color(255,89,89));
                    emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                    emb.setTimestamp(new Date().toInstant());
                    event.editMessageEmbeds(emb.build()).queue();
                    event.editSelectMenu(fakemenu).queue();
                    emb.clear();
                    emb = null;
                    /*permList.clear();
                    permList = null;*/
                    return;
                }
            }
        }
        if (event.getSelectMenu().getId().equalsIgnoreCase("menu:modaction-confirm")) {
            try {
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("confirm-yes")) {
                    SelectMenu fakemenu = event.getSelectMenu().createCopy().setDisabled(true).setPlaceholder("Confirm Selected").build();
                    String[] data = modMap.get(event.getUser().getId());
                    User target = modMapUser.get(event.getUser().getId());
                    Guild g = event.getJDA().getGuildById(event.getGuild().getId());
                    String reason = data[2];
                    String caseID = "#" + Main.generateRandomID(7);
                    if (data[1].equalsIgnoreCase("kick")) {
                        try {
                            ModerationCommandAction action = ModerationCommandAction.KICK;
                            Main.insertCase(target, action, reason, event.getUser(), caseID, guildID.get(event.getUser()));
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, action, reason, caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            MessageEmbed vbPM = Main.generateKickEmbed(reason, caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            sendMessageKick(target, vbPM, guildID.get(event.getUser()));

                            g.kick(UserSnowflake.fromId(target.getId()), reason).queue();
                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                            event.editSelectMenu(fakemenu).queue();
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ban")) {
                        try {
                            ModerationCommandAction action = ModerationCommandAction.BAN;
                            Main.insertCase(target, action, reason, event.getUser(), caseID, guildID.get(event.getUser()));
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, action, reason, caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            MessageEmbed vbPM = Main.generateBanEmbed(reason, caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            sendMessage(target, vbPM, "2", guildID.get(event.getUser()));

                            g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(reason).queue();
                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                            event.editSelectMenu(fakemenu).queue();
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    if (data[1].equalsIgnoreCase("warning")) {
                        try {
                            ModerationCommandAction action = ModerationCommandAction.WARN;
                            Main.insertCase(target, action, data[2], event.getUser(), caseID, guildID.get(event.getUser()));
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, action, reason, caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            MessageEmbed warn = Main.generatewarnEmbed(reason, caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            sendMessage(target, warn);

                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                            event.editSelectMenu(fakemenu).queue();
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    if (data[1].equalsIgnoreCase("ca-ban")) {
                        try {
                            g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(ModerationCommandAction.SCAMBAN.getDefaultReason()).queue();

                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            Main.insertCase(target, ModerationCommandAction.SCAMBAN, ModerationCommandAction.SCAMBAN.getDefaultReason(), event.getUser(), caseID, guildID.get(event.getUser()));
                            event.editSelectMenu(fakemenu).queue();
                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    if (data[1].equalsIgnoreCase("underage-ban")) {
                        try {
                            g.ban(UserSnowflake.fromId(target.getId()), 0, TimeUnit.DAYS).reason(ModerationCommandAction.UNDERAGE.getDefaultReason()).queue();
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason(), caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            client.send(webhookMessageBuilder.addEmbeds(log).build());

                            Main.insertCase(target, ModerationCommandAction.UNDERAGE, ModerationCommandAction.UNDERAGE.getDefaultReason(), event.getUser(), caseID, guildID.get(event.getUser()));

                            MessageEmbed vbPM = Main.generateBanEmbed(ModerationCommandAction.UNDERAGE.getDefaultReason(), caseID, event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            sendMessage(target, vbPM, "2", guildID.get(event.getUser()));

                            event.editSelectMenu(fakemenu).queue();
                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    if (data[1].equalsIgnoreCase("timeout")) {
                        try {
                            ModerationCommandAction action = ModerationCommandAction.TIMEOUT;
                            String[] todata = modTimeout.get(event.getUser().getId());
                            String unit = todata[0];
                            assert unit != null;
                            assert todata[1] != null;
                            unit.trim();
                            int duration = Integer.parseInt(todata[1].trim());
                            if (!unit.equalsIgnoreCase("seconds") && !unit.equalsIgnoreCase("minutes") && !unit.equalsIgnoreCase("hours") && !unit.equalsIgnoreCase("days")) {
                                event.reply("<:squareexclamationred:1058119075789803650> Invalid Timeunit! MAC Cancelled!").setEphemeral(true).queue();
                                removeUserFromMACMaps(event.getUser().getId());
                                return;
                            }
                            TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
                            if (timeUnit == TimeUnit.DAYS && duration > 28 ||
                                    timeUnit == TimeUnit.HOURS && duration > 672 ||
                                    timeUnit == TimeUnit.MINUTES && duration > 40320 ||
                                    timeUnit == TimeUnit.SECONDS && duration > 2419200) {
                                event.reply("<:squareexclamationred:1058119075789803650> Invalid Duration! MAC Cancelled!").queue();
                                removeUserFromMACMaps(event.getUser().getId());
                                return;
                            }
                            try {
                                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
                                if (timeUnit.equals(TimeUnit.DAYS)) {
                                    calendar.add(Calendar.DATE, duration);
                                }
                                if (timeUnit.equals(TimeUnit.HOURS)) {
                                    calendar.add(Calendar.HOUR, duration);
                                }
                                if (timeUnit.equals(TimeUnit.MINUTES)) {
                                    calendar.add(Calendar.MINUTE, duration);
                                }
                                if (timeUnit.equals(TimeUnit.SECONDS)) {
                                    calendar.add(Calendar.SECOND, duration);
                                }
                                SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a 'EST'");
                                WebhookEmbed log = Main.generateModlog(event.getUser(), target, action, reason, caseID, "" + duration + " " + timeUnit, format.format(calendar.getTime()), guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                                assert g != null;
                                TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                                Webhook s = null;
                                assert channel != null;
                                for (Webhook h : channel.retrieveWebhooks().complete())
                                    if (h.getName().equals("MAB")) {
                                        s = h;
                                        break;
                                    }

                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                                webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                                WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                                builder.setThreadFactory((job) -> {
                                    Thread thread = new Thread(job);
                                    thread.setName("Hello");
                                    thread.setDaemon(true);
                                    return thread;
                                });
                                builder.setWait(true);
                                club.minnced.discord.webhook.WebhookClient client = builder.build();

                                client.send(webhookMessageBuilder.addEmbeds(log).build());
                                Main.insertCase(target, action, data[2], event.getUser(), caseID, guildID.get(event.getUser()));

                                User user = timeoutMember.get(event.getUser());

                                event.getJDA().getGuildById(guildID.get(event.getUser())).getMemberById(user.getId()).timeoutFor(Long.parseLong(String.valueOf(duration)), TimeUnit.valueOf(unit)).queue();

                                String durationOf = duration + " " + timeUnit;

                                MessageEmbed vbPM = Main.generateTimeoutEmbed(reason, caseID, "" + duration + " " + timeUnit, format.format(calendar.getTime()), event.getJDA().getGuildById(guildID.get(event.getUser())).getName(), event.getJDA().getGuildById(guildID.get(event.getUser())), event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                                sendMessage(target, vbPM, "1", guildID.get(event.getUser()));

                                event.reply("<:squarechecksolid:1057753652602867813> Moderation Completed!").setEphemeral(true).queue();
                                removeUserFromMACMaps(event.getUser().getId());
                            } catch (InsufficientPermissionException e) {
                                EmbedBuilder emb = new EmbedBuilder();
                                emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                                emb.setColor(new Color(255, 89, 89));
                                emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                                emb.setTimestamp(new Date().toInstant());
                                event.editMessageEmbeds(emb.build()).queue();
                                event.editSelectMenu(fakemenu).queue();
                                emb.clear();
                            } catch (UnsupportedOperationException e) {
                                EmbedBuilder emb = new EmbedBuilder();
                                emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                                emb.setColor(new Color(255, 89, 89));
                                emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                                emb.setTimestamp(new Date().toInstant());
                                event.editMessageEmbeds(emb.build()).queue();
                                event.editSelectMenu(fakemenu).queue();
                                emb.clear();
                            } catch (SQLException | ClassNotFoundException e) {
                                event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                            return;
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    if (data[1].equalsIgnoreCase("virtual-ban")) {
                        try {
                            ModerationCommandAction action = ModerationCommandAction.VIRTUALBAN;
                            WebhookEmbed log = Main.generateModlog(event.getUser(), target, action, reason, caseID, guildID.get(event.getUser()), event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            assert g != null;
                            TextChannel channel = event.getGuild().getTextChannelById(Main.getLogChannel(g.getId()));
                            Webhook s = null;
                            assert channel != null;
                            for (Webhook h : channel.retrieveWebhooks().complete())
                                if (h.getName().equals("MAB")) {
                                    s = h;
                                    break;
                                }

                            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                            webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                            WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                            builder.setThreadFactory((job) -> {
                                Thread thread = new Thread(job);
                                thread.setName("Hello");
                                thread.setDaemon(true);
                                return thread;
                            });
                            builder.setWait(true);
                            club.minnced.discord.webhook.WebhookClient client = builder.build();

                            MessageEmbed vbPM = Main.generateVirtualBanEmbed(reason, caseID, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            Main.insertCase(target, action, reason, event.getUser(), caseID, guildID.get(event.getUser()));
                            sendMessage(target, vbPM, "3", guildID.get(event.getUser()));
                            // VIRTUAL BAN PROCESS BEGIN
                            Main.deSyncUser(target, GuildID, SyncRoleID, event.getJDA());
                            Main.compileAndRemoveRoles(target, GuildID, event.getJDA());
                            // VIRTUAL BAN PROCESS END
                            client.send(webhookMessageBuilder.addEmbeds(log).build());
                            g.addRoleToMember(UserSnowflake.fromId(target.getId()), g.getRoleById(VirtualBanRoleID)).queue();
                            event.reply("Moderation Complete!").setEphemeral(true).queue();
                            removeUserFromMACMaps(event.getUser().getId());
                        } catch (InsufficientPermissionException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Insufficient Permissions!\n\n(" + e.getPermission().getName() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (UnsupportedOperationException e) {
                            EmbedBuilder emb = new EmbedBuilder();
                            emb.setDescription("<:squareexclamationred:1058119075789803650> Unsupported Operation!\n\n(" + e.getMessage() + ")");
                            emb.setColor(new Color(255, 89, 89));
                            emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                            emb.setTimestamp(new Date().toInstant());
                            event.editMessageEmbeds(emb.build()).queue();
                            event.editSelectMenu(fakemenu).queue();
                            emb.clear();
                        } catch (SQLException | ClassNotFoundException e) {
                            event.reply("<:squareexclamationred:1058119075789803650> Something went wrong!").setEphemeral(true).queue();
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                }
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("confirm-no")) {
                    removeUserFromMACMaps(event.getUser().getId());
                    event.reply("Moderation Cancelled!").setEphemeral(true).queue();
                    event.editSelectMenu(event.getSelectMenu().createCopy().setDisabled(true).setPlaceholder("MAC Cancelled").build()).queue();
                    removeUserFromMACMaps(event.getUser().getId());
                    return;
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }

        }
    }

    // MESSAGE LOGS

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().isBot()) {
            messageMap.put(event.getMessageIdLong(), event.getMessage());
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        try {
            if (Main.isEnabled("messagelogs", event.getGuild().getIdLong()) && Main.messageLogSet(event.getGuild().getId()) && !Main.isBanned(event.getGuild().getIdLong())) {
                if (messageMap.containsKey(event.getMessageIdLong())) {

                    boolean hasMember = messageMap.containsKey(event.getMessageIdLong());

                    Message message = messageMap.getOrDefault(event.getMessageIdLong(), null);
                    Member member = hasMember ? event.getGuild().getMemberById(message.getAuthor().getId()) : null;

                    if (member != null && member.getUser().isBot()) return;

                    MessageChannelUnion channel = event.getChannel();

                    TextChannel c = event.getGuild().getTextChannelById(Main.getMessageLog(event.getGuild().getId()));
                    Webhook s = null;
                    assert c != null;
                    for (Webhook h : c.retrieveWebhooks().complete())
                        if (h.getName().equals("MAB")) {
                            s = h;
                            break;
                        }

                    WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                    builder.setThreadFactory((job) -> {
                        Thread thread = new Thread(job);
                        thread.setName("Hello");
                        thread.setDaemon(true);
                        return thread;
                    });
                    builder.setWait(true);
                    club.minnced.discord.webhook.WebhookClient client = builder.build();

                    assert member != null;

                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                    webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                    WebhookEmbed embed = new WebhookEmbedBuilder()
                            .setAuthor(new WebhookEmbed.EmbedAuthor(member != null ? member.getUser().getAsTag() : "...", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                            .setColor(13192011)
                            .setDescription("**Message sent by " + member.getUser().getAsMention() + " deleted in " + channel.getAsMention() + "**")
                            .addField(new WebhookEmbed.EmbedField(true, "Message:", message != null ? !message.getContentRaw().equals("") ? message.getContentRaw().length() > 1024 ? message.getContentRaw().substring(0, 500) + "..." :
                                    message.getContentRaw() :
                                    "ERROR: Message had no content!" :
                                    "ERROR: Message was not in the cache!"))
                            .setTimestamp(new Date().toInstant())
                            .setFooter(new WebhookEmbed.EmbedFooter("User ID: " + (member != null ? member.getId() : "N/A"), event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                            .build();

                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Message sent by " + member.getUser().getAsTag() + " (" + member.getUser().getId() + ") deleted in " + channel.getAsMention() + ", " + event.getGuild().getId());
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Message content: " + message.getContentRaw());
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "--------------------------");

                    String logChannelID = null;
                    try {
                        logChannelID = Main.getMessageLog(event.getGuild().getId());
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    assert logChannelID != null;
                    TextChannel logChannel = Objects.requireNonNull(event.getJDA().getGuildById(event.getGuild().getId())).getTextChannelById(logChannelID);

                    if (logChannel != null) client.send(webhookMessageBuilder.addEmbeds(embed).build());
                }


                messageMap.remove(event.getMessageIdLong());
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        try {
            if (Main.isEnabled("messagelogs", event.getGuild().getIdLong()) && Main.messageLogSet(event.getGuild().getId()) && !Main.isBanned(event.getGuild().getIdLong())) {
                if (event.getAuthor().isBot()) return;
                Member member = event.getMember();

                MessageChannelUnion channel = event.getChannel();

                TextChannel c = event.getGuild().getTextChannelById(Main.getMessageLog(event.getGuild().getId()));
                Webhook s = null;
                assert c != null;
                for (Webhook h : c.retrieveWebhooks().complete())
                    if (h.getName().equals("MAB")) {
                        s = h;
                        break;
                    }

                WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Hello");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                club.minnced.discord.webhook.WebhookClient client = builder.build();

                String oldMessage = messageMap.containsKey(event.getMessageIdLong()) ? messageMap.get(event.getMessageIdLong()).getContentRaw() : "Message not in cache";
                String newMessage = event.getMessage().getContentRaw();

                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                WebhookEmbed embed = new WebhookEmbedBuilder()
                        .setAuthor(new WebhookEmbed.EmbedAuthor(member != null ? member.getUser().getAsTag() : "...", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                        .setColor(16751616)
                        .setDescription("**" + member.getUser().getAsMention() + " updated their message in " + channel.getAsMention() + "** [Jump to Message](" + event.getMessage().getJumpUrl() + ")")
                        .addField(new WebhookEmbed.EmbedField(true, "Old Message:", oldMessage.length() > 1024 ? oldMessage.substring(0, 500) + "..." : oldMessage))
                        .addField(new WebhookEmbed.EmbedField(true, "New Message:", newMessage.length() > 1024 ? newMessage.substring(0, 500) + "..." : newMessage))
                        .setTimestamp(new Date().toInstant())
                        .setFooter(new WebhookEmbed.EmbedFooter("User ID: " + (member != null ? member.getId() : "N/A"), event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                        .build();

                BungeeCord.getInstance().getLogger().log(Level.INFO, "Message sent by " + member.getUser().getAsTag() + " ("  + member.getUser().getId() + ") edited in " + channel.getAsMention() + ", " + event.getGuild().getId());
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Old Message: " + oldMessage);
                BungeeCord.getInstance().getLogger().log(Level.INFO, "New Message: " + newMessage);
                BungeeCord.getInstance().getLogger().log(Level.INFO, "--------------------------");

                String logChannelID = null;
                try {
                    logChannelID = Main.getMessageLog(event.getGuild().getId());
                } catch (SQLException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) client.send(webhookMessageBuilder.addEmbeds(embed).build());

                // Update MessageCache if present, else add to cache
                if (!messageMap.containsKey(event.getMessageIdLong())) messageMap.put(event.getMessageIdLong(), event.getMessage());
                else messageMap.replace(event.getMessageIdLong(), event.getMessage());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // MEMBER LOGS //

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        Guild guild = event.getGuild();

        try {
            if (Main.isEnabled("joinlogs", guild.getIdLong()) && Main.joinLogChannelSet(guild.getId()) && !Main.isBanned(event.getGuild().getIdLong())) {

                Member member = event.getMember();

                TextChannel c = event.getGuild().getTextChannelById(Main.getJoinLogChannel(event.getGuild().getId()));
                Webhook s = null;
                assert c != null;
                for (Webhook h : c.retrieveWebhooks().complete())
                    if (h.getName().equals("MAB")) {
                        s = h;
                        break;
                    }

                WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Hello");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                club.minnced.discord.webhook.WebhookClient client = builder.build();

                long accountAge = member.getTimeJoined().minusHours(5).toEpochSecond() - member.getTimeCreated().minusHours(5).toEpochSecond();
                Utils utils = new Utils(accountAge);

                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                WebhookEmbed embed = new WebhookEmbedBuilder()
                        .setAuthor(new WebhookEmbed.EmbedAuthor("Member Joined", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                        .setColor(564249)
                        .setDescription(member.getAsMention() + " (" + member.getUser().getAsTag() + ", " + member.getId() + ")")
                        .addField(new WebhookEmbed.EmbedField(true, "Member Count:", NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount())))
                        .addField(new WebhookEmbed.EmbedField(true, "Account Age:", utils.toString()))
                        .setTimestamp(new Date().toInstant())
                        .setFooter(new WebhookEmbed.EmbedFooter(footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                        .build();

                BungeeCord.getInstance().getLogger().log(Level.INFO, "Member joined " + event.getGuild().getId() + ": " + member.getUser().getAsTag() + " (" + member.getId() + ")");
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Member Count: " + NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount()));
                BungeeCord.getInstance().getLogger().log(Level.INFO, "Account Age: " + utils.toString());
                BungeeCord.getInstance().getLogger().log(Level.INFO, "--------------------------");

                String logChannelID = null;
                try {
                    logChannelID = Main.getMessageLog(event.getGuild().getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) client.send(webhookMessageBuilder.addEmbeds(embed).build());


            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        Guild guild = event.getGuild();

        try {
            if (Main.isEnabled("joinlogs", guild.getIdLong()) && Main.joinLogChannelSet(guild.getId()) && !Main.isBanned(event.getGuild().getIdLong())) {

                Member member = event.getMember();

                TextChannel c = event.getGuild().getTextChannelById(Main.getJoinLogChannel(event.getGuild().getId()));
                Webhook s = null;
                assert c != null;
                for (Webhook h : c.retrieveWebhooks().complete())
                    if (h.getName().equals("MAB")) {
                        s = h;
                        break;
                    }

                WebhookClientBuilder builder = new WebhookClientBuilder(s.getIdLong(), s.getToken());
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Hello");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                club.minnced.discord.webhook.WebhookClient client = builder.build();

                WebhookEmbed embed = null;

                OffsetDateTime dt = member.getTimeJoined();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ssa 'ET'");

                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                if (member.getRoles().size() >= 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Role roles : member.getRoles()) {
                        stringBuilder.append(roles.getAsMention());
                        if (member.getRoles().size() > 1) {
                            stringBuilder.append(", ");
                        }
                    }

                    embed = new WebhookEmbedBuilder()
                            .setAuthor(new WebhookEmbed.EmbedAuthor("Member Left", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                            .setColor(13192011)
                            .setDescription(member.getAsMention() + " (" + member.getUser().getAsTag() + ", " + member.getId() + ")")
                            .addField(new WebhookEmbed.EmbedField(true, "Member Count:", NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount())))
                            .addField(new WebhookEmbed.EmbedField(true, "Date Joined:", fmt.format(dt)))
                            .addField(new WebhookEmbed.EmbedField(true, "Roles (" + member.getRoles().size() + "):", stringBuilder.toString()))
                            .setTimestamp(new Date().toInstant())
                            .setFooter(new WebhookEmbed.EmbedFooter(footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                            .build();

                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Member Left " + event.getGuild().getId() + ": " + member.getUser().getAsTag() + " (" + member.getId() + ")");
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Member Count: " + NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount()));
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Date Joined: " + fmt.format(dt));
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Roles (" + member.getRoles().size() + "): " + stringBuilder.toString());
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "--------------------------");
                } else if (member.getRoles().size() == 0) {
                    embed = new WebhookEmbedBuilder()
                            .setAuthor(new WebhookEmbed.EmbedAuthor("Member Left", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                            .setColor(13192011)
                            .setDescription(member.getAsMention() + " (" + member.getUser().getAsTag() + ", " + member.getId() + ")")
                            .addField(new WebhookEmbed.EmbedField(true, "Member Count:", NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount())))
                            .addField(new WebhookEmbed.EmbedField(true, "Date Joined:", fmt.format(dt)))
                            .setTimestamp(new Date().toInstant())
                            .setFooter(new WebhookEmbed.EmbedFooter(footer, event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                            .build();
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Member Left " + event.getGuild().getId() + ": " + member.getUser().getAsTag() + " (" + member.getId() + ")");
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Member Count: " + NumberFormat.getNumberInstance().format(event.getGuild().getMemberCount()));
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "Date Joined: " + fmt.format(dt));
                    BungeeCord.getInstance().getLogger().log(Level.INFO, "--------------------------");
                }

                String logChannelID = null;
                try {
                    logChannelID = Main.getMessageLog(event.getGuild().getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                assert logChannelID != null;
                TextChannel logChannel = event.getGuild().getTextChannelById(logChannelID);

                if (logChannel != null) client.send(webhookMessageBuilder.addEmbeds(embed).build());


            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    // ADD BOT TO GUILD //

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        Guild guild = event.getGuild();

        try {
            Main.insertSettings(guild.getId());

            guild.getOwner().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(
                    "Thank you for adding me in **" + guild.getName() + "**! Get started with `/mabsettings`" +
                            "\n\n" +
                            "Join our Discord if you need any bot support here: https://discord.twistedmcstudios.com/").queue());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            if (Main.isBanned(Long.parseLong(guild.getId()))) {
                guild.getOwner().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(
                                "Your guild, **" + guild.getName() + "** is currently suspended from using the MAB bot due to abuse and/or spamming." +
                                        "\n\nIf you believe this was done in error, create a ticket using the button below:")
                        .setActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                        .queue());
                guild.leave().queue();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getName().equals("Moderate User")) {
            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            try {
                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("<:squareexclamationred:1058119075789803650> This guild is currently suspended from using the MAB bot due to abuse and/or spamming." +
                                    "\n\nIf you believe this was done in error, create a ticket using the button below:")
                            .addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                            .queue();
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (Main.isMaintenance("MAB") && event.getGuild().getOwnerIdLong() != 478410064919527437L && event.getUser().getIdLong() != 478410064919527437L) {
                    try {
                        event.reply("<:squareexclamationred:1058119075789803650> MAB is currently undergoing maintenance!\n\nFor More Information, click the button below:")
                                .addActionRow(Button.link(Main.getStatusLink("MAB"), "View Status Updates"))
                                .queue();
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && Main.logChannelSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Appeal URL is not set! An admin must set it with `/mabsettings`! Appeal URL can be toggled on/off with `/mabsettings toggleappeal`!").setEphemeral(true).queue();
                    return;
                }

                if (!Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) ||
                        Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Mod Log Channel is not set! An admin must set it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && !Main.logChannelSet(String.valueOf(event.getGuild().getIdLong())) && !Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong()))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Mod Log Channel & Appeal URL are not set! An admin must set them with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }

                if (Main.appealEnabled(event.getGuild().getIdLong()) && Main.appealLinkSet(String.valueOf(event.getGuild().getIdLong())) && !isUrl(Main.getAppealLink(String.valueOf(event.getGuild().getIdLong())))) {
                    event.deferReply().setContent("<:squareexclamationred:1058119075789803650> Your Appeal URL is not a URL! Please fix it with `/mabsettings`!").setEphemeral(true).queue();
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            Member member = event.getMember();

            EnumSet<Permission> memPerms = member.getPermissions();
            boolean memCanMute = memPerms.toString().contains(Permission.MODERATE_MEMBERS.name());

            if (!memCanMute) {
                event.reply("<:squareexclamationred:1058119075789803650> You do not have permission to use **/moderate**!").setEphemeral(true).queue();
                return;
            }

            User user = event.getTarget();

            if (user.isBot()) {
                event.reply("<:squareexclamationred:1058119075789803650> Cannot use on bot!").setEphemeral(true).queue();
                return;
            }

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
            timeoutMember.put(event.getUser(), user);
            guildID.put(event.getUser(), event.getGuild().getId());
            if (event.getGuild().getIdLong() == 549595806009786388L) {
                SelectMenu menu = SelectMenu.create("menu:modaction")
                        .setPlaceholder("Select Moderation Action Command")
                        .setRequiredRange(1, 1)
                        .addOption("Compromised Account", "mac-ca", "Auto-Ban the user with reason: \"Compromised Account\"")
                        .addOption("Underage User", "mac-uau", "User age < 13 years old. (Req: 13+ according to Discord TOS)")
                        .addOption("Warn", "mac-w", "Warn the user. Sends them a DM with the reason.")
                        .addOption("Kick", "mac-k", "Kicks the user.")
                        .addOption("Ban", "mac-b", "Ban the user.")
                        .addOption("Virtual Ban", "mac-vb", "Blocks user from interacting with the discord")
                        .addOption("Timeout", "mac-t", "Timeout the user.")
                        .addOption("Cancel Moderation", "mac-cancel", "Cancel the current moderation command.")
                        .build();

                EmbedBuilder emb = new EmbedBuilder();
                emb.setColor(new Color(253, 216, 1));
                emb.setDescription("**Please select the moderation action command!**");
                emb.setTimestamp(new Date().toInstant());
                emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
            } else if (event.getGuild().getIdLong() != 549595806009786388L) {
                SelectMenu menu = SelectMenu.create("menu:modaction")
                        .setPlaceholder("Select Moderation Action Command")
                        .setRequiredRange(1, 1)
                        .addOption("Compromised Account", "mac-ca", "Auto-Ban the user with reason: \"Compromised Account\"")
                        .addOption("Underage User", "mac-uau", "User age < 13 years old. (Req: 13+ according to Discord TOS)")
                        .addOption("Warn", "mac-w", "Warn the user. Sends them a DM with the reason.")
                        .addOption("Kick", "mac-k", "Kicks the user.")
                        .addOption("Ban", "mac-b", "Ban the user.")
                        .addOption("Timeout", "mac-t", "Timeout the user.")
                        .addOption("Cancel Moderation", "mac-cancel", "Cancel the current moderation command.")
                        .build();

                EmbedBuilder emb = new EmbedBuilder();
                emb.setColor(new Color(253, 216, 1));
                emb.setDescription("**Please select the moderation action command!**");
                emb.setTimestamp(new Date().toInstant());
                emb.setFooter("Embed from MAB  •  " + footer, "https://cdn.discordapp.com/emojis/1058317602050551838.png");
                event.replyEmbeds(emb.build()).addActionRow(menu).setEphemeral(true).queue();
            }
        }
    }

    public static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

}