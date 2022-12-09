package net.twistedmc.shield.accounts;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Accounts extends ListenerAdapter{

    private String token;
    public static JDA jda;

    public Accounts(String token) {
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
    }

    public void stop(){
        this.jda.shutdown();
    }

}