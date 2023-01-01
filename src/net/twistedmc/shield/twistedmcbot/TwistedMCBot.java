package net.twistedmc.shield.twistedmcbot;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.twistedmc.shield.twistedmcbot.commands.EmojisCommand;
import net.twistedmc.shield.twistedmcbot.commands.LinkAllCommand;
import net.twistedmc.shield.twistedmcbot.commands.LinkCommand;

import java.util.Calendar;
import java.util.TimeZone;

public class TwistedMCBot extends ListenerAdapter{

    private String token;
    public static JDA jda;

    public static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    public static int year = calendar.get(Calendar.YEAR);
    public static String footer = "© " + year + " TwistedMC Studios";

    public TwistedMCBot(String token) {
        this.token = token;
    }

    public void start(){
        jda = JDABuilder.createDefault(token,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL).build();
        jda.addEventListener(this);

        jda.addEventListener(new LinkCommand());
        jda.addEventListener(new LinkAllCommand());
        jda.addEventListener(new EmojisCommand());

        jda.updateCommands()
                .addCommands(Commands.slash("link", "Link your Minecraft account.")
                        .setGuildOnly(true))
                .addCommands(Commands.slash("linkall", "Sync data from the TwistedMC Minecraft network.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .setGuildOnly(true))
                .addCommands(Commands.slash("emojis", "Get all custom emojis.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .setGuildOnly(true))
                .queue();
    }

    public void stop(){
        this.jda.shutdown();
    }

}