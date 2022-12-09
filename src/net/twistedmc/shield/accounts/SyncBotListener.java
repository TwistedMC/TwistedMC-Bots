package net.twistedmc.shield.accounts;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.Util.Utils;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class SyncBotListener extends ListenerAdapter {

    public static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New York"));
    public static int year = calendar.get(Calendar.YEAR);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            if (event.getGuild().getId().equals("797776730857013259")) {
                if (event.getChannel().getId().equals("797965216671072276")) {
                    event.getMessage().delete().queue();
                }
            }
        }

    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (event.getName().equalsIgnoreCase("syncall")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.reply("**HOLD UP!** This command cannot be used in this guild!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("797776730857013259")) {
                event.deferReply().setContent(":twisted_rightwards_arrows: Syncing data for this guild, this may take a few minutes...").queue(hook -> hook.sendMessage(":white_check_mark: Finished syncing data.").queue());
                for (Member member : Objects.requireNonNull(event.getGuild()).getMemberCache()) {
                    if (!member.getUser().isBot()) {
                        SyncBot.syncName(member.getUser(), member, event.getGuild().getId(), event.getJDA());
                    }
                }
            } else {
                event.deferReply().setContent(":twisted_rightwards_arrows: Syncing data for this guild, this may take a few minutes...").queue(hook -> hook.sendMessage(":white_check_mark: Finished syncing data.").queue());
                for (Member member : Objects.requireNonNull(event.getGuild()).getMemberCache()) {
                    if (!member.getUser().isBot()) {
                        SyncBot.syncRolesNames(member.getUser(), member, event.getGuild().getId(), event.getJDA());
                    }
                }
            }

            return;
        }

        if (event.getName().equalsIgnoreCase("sync")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("549595806009786388")) {
                event.reply("**HOLD UP!** This command cannot be used in this guild!").queue();
                return;
            }

            if (Objects.requireNonNull(event.getGuild()).getId().equals("797776730857013259")) {
                event.deferReply().setContent(":twisted_rightwards_arrows: Syncing your data, this may take a few minutes...").setEphemeral(true).queue(hook -> hook.sendMessage(":white_check_mark: Finished syncing your data.").setEphemeral(true).queue());
                SyncBot.syncName(event.getUser(), Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild()).getId(), event.getJDA());

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
            } else {
                event.deferReply().setContent(":twisted_rightwards_arrows: Syncing your data, this may take a few minutes...").queue(hook -> hook.sendMessage(":white_check_mark: Finished syncing your data.").queue());
                SyncBot.syncRolesNames(event.getUser(), Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild()).getId(), event.getJDA());
            }

        }

        return;
    }
}
