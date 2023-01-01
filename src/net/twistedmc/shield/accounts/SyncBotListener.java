package net.twistedmc.shield.accounts;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class SyncBotListener extends ListenerAdapter {

    public static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    public static int year = calendar.get(Calendar.YEAR);

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getId().equals("549595806009786388")) {
            if (!event.getUser().isBot()) {
                SyncBot.syncRolesNames(event.getUser(), event.getMember(), event.getGuild().getId(), event.getJDA());
                SyncBot.sendMessage(event.getUser(), ":white_check_mark: Synced data for " + guild.getName() + "!");
            }
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        if (guild.getId().equals("549595806009786388")) {
            Objects.requireNonNull(event.getJDA().getGuildById("797776730857013259")).kick(event.getUser()).reason("Left main Discord server").queue();
            Objects.requireNonNull(event.getJDA().getGuildById("963576009091842138")).kick(event.getUser()).reason("Left main Discord server").queue();
        } else if (guild.getId().equals("963576009091842138")) {
            MessageEmbed messageEmbed = SyncBot.generateMessageEmbed("Left TwistedMC Subteams Discord server", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            SyncBot.sendMessage(event.getUser(), messageEmbed);
            SyncBot.sendMessage(event.getUser(), "Contact shasta#0001 if you believe this was an error.");

            SyncBot.clearRoles(event.getUser(), Objects.requireNonNull(event.getMember()), "549595806009786388", event.getJDA());

            Objects.requireNonNull(event.getJDA().getGuildById("797776730857013259")).kick(event.getUser()).reason("Left TwistedMC Subteams Discord server").queue();
        }

    }

    /*@Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            if (event.getGuild().getId().equals("797776730857013259")) {
                if (event.getChannel().getId().equals("797965216671072276")) {
                    event.getMessage().delete().queue();
                }
            }
        }
    }*/

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equalsIgnoreCase("syncall")) {

            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.reply("<:squareexclamationred:1058119075789803650> This command cannot be used in this guild!").queue();
                return;
            }


            try {
                event.deferReply().setContent("<:sharefromsquareregular:1057956937871933480> Syncing data for this guild, this may take a few minutes...").queue();
                for (Member member : Objects.requireNonNull(event.getGuild()).getMemberCache()) {
                    if (!member.getUser().isBot()) {
                        SyncBot.syncRolesNames(member.getUser(), member, event.getGuild().getId(), event.getJDA());
                    }
                }
                event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Finished syncing data.").queue();
            } catch (Exception e) {
                event.getHook().sendMessage("<:squarexmarksolid:1057753638329663598> Failed to sync your data.").queue();
            }

            return;
        }

        if (event.getName().equalsIgnoreCase("sync")) {

            if (!event.isFromGuild()) {
                event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in guilds!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.reply("<:squareexclamationred:1058119075789803650> This command cannot be used in this guild!").queue();
                return;
            }

            try {
                if (Objects.requireNonNull(event.getGuild()).getId().equals("797776730857013259")) {
                    event.deferReply().setContent("<:sharefromsquareregular:1057956937871933480> Syncing your data, this may take a few minutes...").setEphemeral(true).queue();
                    SyncBot.syncRolesNames(event.getUser(), Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild()).getId(), event.getJDA());

                    Member member = event.getMember();

                    TextChannel c = event.getGuild().getTextChannelById("797967157123088466");
                    Webhook s = null;
                    assert c != null;
                    for (Webhook h : c.retrieveWebhooks().complete())
                        if (h.getName().equals("TwistedMC Sync")) {
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

                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();
                    webhookMessageBuilder.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                    WebhookEmbed embed = new WebhookEmbedBuilder()
                            .setAuthor(new WebhookEmbed.EmbedAuthor("Sync Logs", member != null ? member.getUser().getEffectiveAvatarUrl() : event.getJDA().getSelfUser().getEffectiveAvatarUrl(), null))
                            .setColor(0x2F3136)
                            .setDescription(member.getAsMention() + " synced their data!")
                            .setTimestamp(new Date().toInstant())
                            .setFooter(new WebhookEmbed.EmbedFooter("© " + year + " TwistedMC Studios", event.getJDA().getSelfUser().getEffectiveAvatarUrl()))
                            .build();

                    TextChannel logChannel = event.getGuild().getTextChannelById("797967157123088466");

                    if (logChannel != null) client.send(webhookMessageBuilder.addEmbeds(embed).build());
                    event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Finished syncing your data.").queue();
                } else {
                    event.deferReply().setContent("<:sharefromsquareregular:1057956937871933480> Syncing your data, this may take a few minutes...").queue();
                    SyncBot.syncRolesNames(event.getUser(), Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild()).getId(), event.getJDA());
                    event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Finished syncing your data.").queue();
                }
            } catch (Exception e) {
                event.getHook().sendMessage("<:squarexmarksolid:1057753638329663598> Failed to sync your data.").queue();
            }

        }
    }
}
