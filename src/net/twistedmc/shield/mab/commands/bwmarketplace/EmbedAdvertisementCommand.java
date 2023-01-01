package net.twistedmc.shield.mab.commands.bwmarketplace;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.twistedmc.shield.Main;
import net.twistedmc.shield.mab.MAB;
import net.twistedmc.shield.mab.permissions.PermissionLevel;
import net.twistedmc.shield.mab.permissions.Permissions;

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
                        event.reply("<:squareexclamationred:1058119075789803650> This bot is currently under maintenance!\n\nFor More Information, click the button below:")
                                .addActionRow(Button.link(Main.getStatusLink("MAB"), "View Status Updates"))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://discord.twistedmcstudios.com/", "Support Server")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (event.getGuild().getIdLong() == 1001147856687742996L) {
                if (event.getChannel().getIdLong() != 1016653707892494416L) {
                    event.reply("<:squareexclamationred:1058119075789803650> This command can only be done in <#1016653707892494416>!").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("builtbybit_link") != null) {
                if (!String.valueOf(event.getOption("builtbybit_link")).contains("http")) {
                    event.reply("<:squareexclamationgold:1058119077085859900> BuiltByBit Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("spigot_link") != null) {
                if (!String.valueOf(event.getOption("spigot_link")).contains("http")) {
                    event.reply("<:squareexclamationgold:1058119077085859900> Spigot Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("polymart_link") != null) {
                if (!String.valueOf(event.getOption("polymart_link")).contains("http")) {
                    event.reply("<:squareexclamationgold:1058119077085859900> Polymart Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("support_server") != null) {
                if (!String.valueOf(event.getOption("support_server")).contains("http")) {
                    event.reply("<:squareexclamationgold:1058119077085859900> Support Server Link is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
                    return;
                }
            }

            if (event.getOption("logo_url") != null) {
                if (!String.valueOf(event.getOption("logo_url")).contains("http")) {
                    event.reply("<:squareexclamationgold:1058119077085859900> Logo URL is not a valid URL! Must contain 'http', 'https'").setEphemeral(true).queue();
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

            eb.addField("List Price:", price, true);

            if (event.getOption("creator") != null) {
                eb.addField("Creator:", event.getOption("creator").getAsMember().getAsMention(), true);
            }

            if (event.getOption("test_server") != null) {
                eb.addField("Test Server:", event.getOption("test_server").getAsString(), true);
            }


            if (Permissions.checkLevel(event.getUser(), event.getMember(), PermissionLevel.CREATOR)) {
                eb.setFooter("Embed from MAB developer", "https://cdn.discordapp.com/emojis/1058317602050551838.png");
            } else if (Permissions.checkLevel(event.getUser(), event.getMember(), PermissionLevel.TRUSTED)) {
                eb.setFooter("Embed from trusted user", "https://cdn.discordapp.com/emojis/1058507639555895377.png");
            } else {
                eb.setFooter("" + event.getUser().getId(), event.getGuild().getIconUrl());
            }

            eb.setTimestamp(new Date().toInstant());

            event.deferReply().setContent("<:sharefromsquareregular:1057956937871933480> Creating embed...").queue();

            try {
                if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") != null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("support_server").getAsString(), "Support or Docs")
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") != null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("builtbybit_link").getAsString(), "Download (BuiltByBit)")
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") == null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Polymart)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") == null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (Spigot)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                } else if (event.getOption("builtbybit_link") == null && event.getOption("spigot_link") != null && event.getOption("polymart_link") != null && event.getOption("support_server") == null) {
                    try {
                        textChannel.sendMessageEmbeds(eb.build())
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("spigot_link").getAsString(), "Download (Spigot)")
                                        .withEmoji(Emoji.fromFormatted("<:spigotemoji:1057952308627001405>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com/mab/links/?link=" + event.getOption("polymart_link").getAsString(), "Download (Polymart)")
                                        .withEmoji(Emoji.fromFormatted("<:polymartemoji:1057952307364511775>")))
                                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.link("https://twistedmcstudios.com", "Download (BuiltByBit)")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:builtbybitemoji:1057952306391433306>")))
                                .addActionRow(Button.link("https://twistedmcstudios.com", "Support or Docs")
                                        .asDisabled()
                                        .withEmoji(Emoji.fromFormatted("<:discordemoji:1057952305338667008>")))
                                .queue();
                        event.getHook().sendMessage("<:squarechecksolid:1057753652602867813> Embed successfully created in " + textChannel.getAsMention() + "!").queue();
                    } catch (Exception e) {
                        event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                        e.printStackTrace();
                    }
                }
            } catch (Exception e){
                event.getHook().sendMessage("<:squareexclamationgold:1058119077085859900> An error occurred whilst creating an embed. " + event.getJDA().getUserById("478410064919527437").getAsMention()).queue();
                e.printStackTrace();
            }

        }
    }
}
