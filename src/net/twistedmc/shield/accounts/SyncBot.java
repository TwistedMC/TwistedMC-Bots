package net.twistedmc.shield.accounts;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.BungeeCord;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.twistedmc.shield.accounts.SyncBotListener.year;

public class SyncBot extends ListenerAdapter{

    private String token;
    public static JDA jda;

    public SyncBot(String token) {
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
        jda.addEventListener(new SyncBotListener());

        jda.updateCommands()
                .addCommands(Commands.slash("sync", "Sync your data from the main Discord server.")
                        .setGuildOnly(true))
                .addCommands(Commands.slash("syncall", "Sync data from the main Discord server.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .setGuildOnly(true))
                .queue();
    }

    public static void RunThings() {
        for (Member member : Objects.requireNonNull(jda.getGuildById("963576009091842138")).getMemberCache()) {
            if (!member.getUser().isBot()) {
                SyncBot.syncRolesNames(member.getUser(), member, "963576009091842138", jda);
            }
        }

        for (Member member : Objects.requireNonNull(jda.getGuildById("797776730857013259")).getMemberCache()) {
            if (!member.getUser().isBot()) {
                SyncBot.syncRolesNames(member.getUser(), member, "797776730857013259", jda);
            }
        }
    }

    public static void sendMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }

    public static void sendMessage(User user, MessageEmbed embed) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed).addActionRow(Button.link("https://discord.gg/twistedmc", "Rejoin")).addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Contact support")))
                .queue();
    }

    public static MessageEmbed generateMessageEmbed(String reason, String url) {
        EmbedBuilder log = new EmbedBuilder();
        log.setTitle("You've been automatically kicked from all of the TwistedMC Staff Discord servers!");
        log.setColor(new Color(175, 66, 0));
        log.setTimestamp(new java.util.Date().toInstant());
        log.addField("Reason", reason, false);
        log.setFooter("© " + year + " TwistedMC Studios", url);
        return log.build();
    }

    public static void clearRoles(@Nonnull User user, @Nonnull Member member, @Nonnull String guildID, @Nonnull JDA jda) {

        if (user.isBot()) {
            return;
        }

        Objects.requireNonNull(jda.getGuildById(guildID)).modifyMemberRoles(member, null, member.getRoles()).queue();

        if (Objects.requireNonNull(jda.getGuildById(guildID)).getSelfMember().canInteract(member)) {
            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guildID)).getMember(user)).modifyNickname("Unlinked_Player").queue();
        }
    }

    public static void syncRolesNames(@Nonnull User user, @Nonnull Member member, @Nonnull String guildID, @Nonnull JDA jda) {

        if (user.isBot()) {
            return;
        }

        Objects.requireNonNull(jda.getGuildById(guildID)).modifyMemberRoles(member, null, member.getRoles()).queue();

        List<Role> roles = Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(549595806009786388L)).getMember(UserSnowflake.fromId(user.getId()))).getRoles();

        List<Role> roles_ = new ArrayList<>(member.getRoles());
        for (Role role: roles) {
            if (findRole(guildID, role.getName()) != null) {
                roles_.addAll(Objects.requireNonNull(jda.getGuildById(guildID)).getRolesByName(role.getName(), false));
            }
        }
        Objects.requireNonNull(jda.getGuildById(guildID)).modifyMemberRoles(member, roles_).queue();

        if (Objects.requireNonNull(jda.getGuildById(guildID)).getSelfMember().canInteract(member)) {
            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guildID)).getMember(user)).modifyNickname(Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(549595806009786388L)).getMemberById(user.getId())).getEffectiveName()).queue();
        }
    }

    public static void syncName(@Nonnull User user, @Nonnull Member member, @Nonnull String guildID, @Nonnull JDA jda) {

        if (user.isBot()) {
            return;
        }

        if (Objects.requireNonNull(jda.getGuildById(guildID)).getSelfMember().canInteract(member)) {
            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guildID)).getMember(user)).modifyNickname(Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(549595806009786388L)).getMemberById(user.getId())).getEffectiveName()).queue();
        }
    }

    public static Role findRole(String guildId, String name) {
        List<Role> roles = jda.getGuildById(guildId).getRoles();
        return roles.stream()
                .filter(role -> role.getName().equals(name)) // filter by role name
                .findFirst() // take first result
                .orElse(null); // else return null
    }

    public void stop(){
        this.jda.shutdown();
    }

}