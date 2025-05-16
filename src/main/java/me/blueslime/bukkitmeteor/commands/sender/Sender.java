package me.blueslime.bukkitmeteor.commands.sender;

import me.blueslime.bukkitmeteor.getter.MeteorGetter;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.languages.LanguageProvider;
import me.blueslime.utilitiesapi.UtilitiesAPI;
import me.blueslime.utilitiesapi.color.ColorHandler;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.blueslime.utilitiesapi.tools.PlaceholderParser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class Sender {
    private final CommandSender sender;

    private Sender(CommandSender sender) {
        this.sender = sender;
    }

    /**
     * Create a Sender instance
     * @param sender to be converted
     * @return Sender instance
     */
    public static Sender build(CommandSender sender) {
        return new Sender(sender);
    }

    /**
     * Check if the sender is a player
     * @return result
     */
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    /**
     * Convert the Sender to a player
     * @return Player
     */
    public Player toPlayer() {
        return (Player)sender;
    }

    /**
     * Check if the sender is a ConsoleCommandSender
     * @return boolean result
     */
    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    /**
     * Check if the sender source is a CommandBlock
     * @return boolean result
     */
    public boolean isCommandBlock() {
        return sender instanceof BlockCommandSender;
    }

    /**
     * Convert the Sender to a ConsoleCommandSender
     * @return ConsoleCommandSender if the sender is a ConsoleCommandSender
     */
    public ConsoleCommandSender toConsole() {
        return (ConsoleCommandSender)sender;
    }

    /**
     * Convert the Sender to a CommandBlock
     */
    public BlockCommandSender toCommandBlockSender() {
        return (BlockCommandSender)sender;
    }

    /**
     * Check if a CommandSender contains a specified permission
     * @param permission to check
     * @return boolean result
     */
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    /**
     * Convert the sender to an Entity
     * @return Entity
     */
    public Entity toEntity() {
        return (Entity)sender;
    }

    /**
     * Send a message to the sender
     * @param target player for PlaceholdersAPI if the server has it.
     * @param replacer replacements for the messages
     * @param messages to be sent
     */
    public void send(Player target, TextReplacer replacer, String... messages) {
        if (messages == null || messages.length == 0) {
            sender.sendMessage(" ");
            return;
        }

        if (UtilitiesAPI.hasPlaceholders() && target != null) {
            for (String message : messages) {
                sender.sendMessage(
                    colorize(
                        PlaceholderParser.parse(
                            target,
                            replacer == null ?
                                message :
                                replacer.apply(message)
                        )
                    )
                );
            }
            return;
        }
        for (String message : messages) {
            sender.sendMessage(
                colorize(
                    replacer == null ?
                        message :
                        replacer.apply(message)
                )
            );
        }
    }

    /**
     * Send messages for the sender
     * @param replacer replacements for messages
     * @param messages to be sent
     */
    public void send(TextReplacer replacer, String... messages) {
        send(null, replacer, messages);
    }

    /**
     * Send messages to the sender
     * @param target player to cast PlaceholdersAPI if the server has it.
     * @param messages to be sent
     */
    public void send(Player target, String... messages) {
        send(target, null, messages);
    }

    /**
     * Send messages to the sender
     * @param messages to be sent
     */
    public void send(String... messages) {
        send((Player)null, null, messages);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target player to cast in PlaceholdersAPI if the server has it.
     * @param configuration for search the current path
     * @param path of the message
     * @param def value if the path is not set
     * @param replacer replacements for messages
     */
    public void send(Player target, ConfigurationSection configuration, String path, Object def, TextReplacer replacer) {
        if (configuration == null) {
            if (!MeteorGetter.LANGUAGES) {
                return;
            }
            if (target == null) {
                if (isPlayer()) {
                    target = toPlayer();
                }
            }
            LanguageProvider provider = Implements.fetch(LanguageProvider.class);
            configuration = provider.fromPlayerLocale(target);
            if (configuration == null) {
                return;
            }
        }

        Object ob = configuration.get(path, def);

        if (ob == null) {
            return;
        }

        if (ob instanceof List<?> list) {
            for (Object object : list) {
                send(
                    target,
                    replacer,
                    object.toString()
                );
            }
        } else {
            send(
                target,
                colorize(
                    replacer == null ?
                        ob.toString() :
                        replacer.apply(ob.toString())
                )
            );
        }
    }

    /**
     * Send messages from a configuration path to the sender
     * @param configuration for search the current path
     * @param path of the message
     * @param def value if the path is not set
     * @param replacer replacements for messages
     */
    public void send(ConfigurationSection configuration, String path, Object def, TextReplacer replacer) {
        send(null, configuration, path, def, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param path of the message
     * @param def value if the path is not set
     * @param replacer replacements for messages
     */
    public void send(String path, Object def, TextReplacer replacer) {
        send(null, null, path, def, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param path of the message
     * @param replacer replacements for messages
     */
    public void send(String path, TextReplacer replacer) {
        send(null, null, path, path, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target for placeholders
     * @param path of the message
     * @param def value if the path is not set
     * @param replacer replacements for messages
     */
    public void send(Player target, String path, Object def, TextReplacer replacer) {
        send(target, null, path, def, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target for placeholders
     * @param path of the message
     * @param replacer replacements for messages
     */
    public void send(Player target, String path, TextReplacer replacer) {
        send(target, null, path, path, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param configuration for search the current path
     * @param path of the message
     * @param def value if the path is not set
     */
    public void send(ConfigurationSection configuration, String path, Object def) {
        send(null, configuration, path, def, null);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target player to cast in PlaceholdersAPI if the server has it.
     * @param configuration for search the current path
     * @param path of the message
     * @param def value if the path is not set
     */
    public void send(Player target, ConfigurationSection configuration, String path, Object def) {
        send(target, configuration, path, def, null);
    }

    /**
     * Send base components to the sender
     * @param components to be sent
     */
    public void send(BaseComponent... components) {
        if (isPlayer()) {
            toPlayer().spigot().sendMessage(components);
        }
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target player to cast in PlaceholdersAPI if the server has it.
     * @param configuration for search the current path
     * @param path of the message
     * @param replacer replacements for messages
     */
    public void send(Player target, ConfigurationSection configuration, String path, TextReplacer replacer) {
        Object ob = configuration.get(path);

        if (ob == null) {
            return;
        }

        if (ob instanceof List<?> list) {
            for (Object object : list) {
                send(
                    target,
                    colorize(
                        replacer == null ?
                            object.toString() :
                            replacer.apply(object.toString())
                    )
                );
            }
        } else {
            send(
                    target,
                    colorize(
                            replacer == null ?
                                    ob.toString() :
                                    replacer.apply(ob.toString())
                    )
            );
        }
    }

    /**
     * Send messages from a configuration path to the sender
     * @param configuration for search the current path
     * @param path of the message
     * @param replacer replacements for messages
     */
    public void send(ConfigurationSection configuration, String path, TextReplacer replacer) {
        send(null, configuration, path, replacer);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param target player to cast in PlaceholdersAPI if the server has it.
     * @param configuration for search the current path
     * @param path of the message
     */
    public void send(Player target, ConfigurationSection configuration, String path) {
        send(target, configuration, path, null);
    }

    /**
     * Send messages from a configuration path to the sender
     * @param configuration for search the current path
     * @param path of the message
     */
    public void send(ConfigurationSection configuration, String path) {
        send(configuration, path, null);
    }

    /**
     * Send a list of messages to the sender
     * @param messages to be sent
     * @param replacer replacements for messages
     */
    public void send(List<String> messages, TextReplacer replacer) {
        send(null, false, messages, replacer);
    }

    /**
     * Send a list of messages to the sender
     * @param target player to cast in PlaceholdersAPI if the server has it
     * @param singleMessage Unify all messages in a single message.
     * @param messages to be sent
     * @param replacer replacements for messages
     */
    public void send(Player target, boolean singleMessage, Collection<String> messages, TextReplacer replacer) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        if (singleMessage) {
            String finalMessage;
            if (!UtilitiesAPI.hasPlaceholders() && target != null) {
                List<String> messagesCopy = new ArrayList<>();
                for (String message : messages) {
                    messagesCopy.add(
                        colorize(
                            PlaceholderParser.parse(
                                target,
                                replacer == null ?
                                    message :
                                    replacer.apply(message)
                            )
                        )
                    );
                }
                finalMessage = String.join("\n", messagesCopy);
            } else {
                List<String> messagesCopy = new ArrayList<>();
                for (String message : messages) {
                    messagesCopy.add(
                        colorize(
                            replacer == null ?
                                message :
                                replacer.apply(message)
                        )
                    );
                }
                finalMessage = String.join("\n", messagesCopy);
            }
            sender.sendMessage(finalMessage);
            return;
        }

        if (!UtilitiesAPI.hasPlaceholders() && target != null) {
            for (String message : messages) {
                sender.sendMessage(
                        colorize(
                                PlaceholderParser.parse(
                                        target,
                                        replacer == null ?
                                                message :
                                                replacer.apply(message)
                                )
                        )
                );
            }
            return;
        }
        for (String message : messages) {
            sender.sendMessage(
                    colorize(
                            replacer == null ?
                                    message :
                                    replacer.apply(message)
                    )
            );
        }
    }

    /**
     * Send a message list to the sender
     * @param messages to be sent
     */
    public void send(List<String> messages) {
        send(messages, null);
    }

    /**
     * Colorize a text value
     * @param text to be colorized
     * @return colored text
     */
    public static String colorize(String text) {
        return ColorHandler.convert(text);
    }

    /**
     * Colorize a String List text value
     * @param list to be colorized
     * @return colored text
     */
    public static List<String> colorizeList(List<String> list) {
        return TextUtilities.colorizeList(list);
    }

    public CommandSender toCommandSender() {
        return sender;
    }
}
