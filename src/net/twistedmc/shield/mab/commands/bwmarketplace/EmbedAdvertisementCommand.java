package net.twistedmc.shield.mab.commands.bwmarketplace;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

public class EmbedAdvertisementCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        if (Objects.equals(event.getSubcommandName(), "embedadvertisement")) {

            if (!event.isFromGuild()) {
                event.reply("**HOLD UP!** This command can only be done in guilds!").queue();
                return;
            }

            try {
                if (Main.isBanned(event.getGuild().getIdLong())) {
                    event.reply("**HOLD UP!** This guild is currently suspended from using the MAB bot due to abuse and/or spamming." +
                                    "\n\nIf you believe this was done in error, create a ticket using the button below:")
                            .addActionRow(Button.link("https://twistedmcstudios.com/tickets/create/", "Submit a request"))
                            .queue();
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (!Main.isEnabled("privacypolicy", event.getGuild().getIdLong())) {
                    event.reply("Our privacy policy was recently updated.").addActionRow(Button.link("https://twistedmcstudios.com/mab/privacy-policy/", "View Privacy Policy")).queue();
                    Main.activateSetting("privacypolicy", event.getGuild().getId());
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                if (Main.isMaintenance("MAB")) {
                    try {
                        event.reply("**HOLD UP!** This bot is currently under maintenance!\n\nFor More Information, click the button below:")
                                .addActionRow(Button.link(Main.getStatusLink("MAB"), "View Status Updates"))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                        .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                                .queue();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (event.getGuild().getIdLong() == 1001147856687742996L) {
                if (event.getChannel().getIdLong() != 1016653707892494416L) {
                    event.reply("**HOLD UP!** This command can only be done in <#1016653707892494416>!").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("builtbybit_link") != null) {
                if (!String.valueOf(event.getOption("builtbybit_link")).contains("http")) {
                    event.reply("BuiltByBit Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("spigot_link") != null) {
                if (!String.valueOf(event.getOption("spigot_link")).contains("http")) {
                    event.reply("Spigot Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("polymart_link") != null) {
                if (!String.valueOf(event.getOption("polymart_link")).contains("http")) {
                    event.reply("Polymart Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("support_server") != null) {
                if (!String.valueOf(event.getOption("support_server")).contains("http")) {
                    event.reply("Support Server Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("logo_url") != null) {
                if (!String.valueOf(event.getOption("logo_url")).contains("http")) {
                    event.reply("Logo URL is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            String name = event.getOption("name").getAsString();

            TextChannel textChannel = event.getOption("channel").getAsChannel().asTextChannel();

            EmbedBuilder eb = new EmbedBuilder();

            if (event.getOption("logo_url") != null) {
                eb.setThumbnail(event.getOption("logo_url").getAsString());
            }

            eb.setTitle(name, null);

            eb.setDescription(event.getOption("description").getAsString().replace("%nl%", "\n"));

            if (event.getOption("rgb_r") != null && event.getOption("rgb_g") != null && event.getOption("rgb_b") != null) {
                eb.setColor(new Color(event.getOption("rgb_r").getAsInt(), event.getOption("rgb_g").getAsInt(), event.getOption("rgb_b").getAsInt()));
            } else {
                eb.setColor(new Color(52, 152, 219));
            }

            String price;
            if (Objects.requireNonNull(event.getOption("price")).getAsString().equals("0") || event.getOption("price").getAsString().equals("0$") || event.getOption("price").getAsString().equalsIgnoreCase("free")) {
                price = "FREE";
            } else {
                price = event.getOption("price").getAsString();
            }

            eb.addField("Price:", price, true);

            if (event.getOption("creator") != null) {
                eb.addField("Creator:", event.getOption("creator").getAsMember().getAsMention(), true);
            }

            if (event.getOption("test_server") != null) {
                eb.addField("Test Server:", event.getOption("test_server").getAsString(), true);
            }


            eb.setFooter("" + event.getUser().getId(), event.getGuild().getIconUrl());
            eb.setTimestamp(new Date().toInstant());

            if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("support_server").getAsString(), "Support or Docs")
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                event.deferReply().setContent("Embed Advertisement sent in " + textChannel.getAsMention() + "!").queue();
                textChannel.sendMessageEmbeds(eb.build())
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                .withEmoji(Emoji.fromFormatted("<:spigotemoji:1016642471326924850>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link(event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                .withEmoji(Emoji.fromFormatted("<:polymartemoji:1016642472287408128>")))
                        .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:builtbybit:1016650432111710280>")))
                        .addActionRow(Button.link("https://twistedmcstudios.com", "Support or Docs")
                                .asDisabled()
                                .withEmoji(Emoji.fromFormatted("<:information2:1050337061347000340>")))
                        .queue();
            }

        }
    }
}
