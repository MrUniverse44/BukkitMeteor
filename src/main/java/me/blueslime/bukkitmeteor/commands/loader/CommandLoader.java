package me.blueslime.bukkitmeteor.commands.loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.Commands;
import me.blueslime.bukkitmeteor.commands.creator.CommandExecutable;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;

public class CommandLoader extends Commands {

    private final Map<String, Command> bukkitCommands;
    private final CommandMap commandMap;

    @SuppressWarnings("unchecked")
    public CommandLoader(BukkitMeteorPlugin plugin) {
        Field bukkitCommandMapField = PluginConsumer.ofUnchecked(
                () -> plugin.getServer().getClass().getDeclaredField("commandMap"),
                e -> {},
                () -> null
        );

        CommandMap commandMap = null;

        if (bukkitCommandMapField != null) {
            bukkitCommandMapField.setAccessible(true);

            commandMap = PluginConsumer.ofUnchecked(
                    () -> (CommandMap)bukkitCommandMapField.get(plugin.getServer()),
                    e -> {},
                    () -> null
            );
        }

        if (commandMap == null) {
            // In this case CommandMap Field was not found, so we need to try with the method instead.
            commandMap = PluginConsumer.ofUnchecked(
                    () -> {
                        Method getCommandMap = plugin.getServer().getClass().getDeclaredMethod("getCommandMap");
                        getCommandMap.setAccessible(true);
                        return (CommandMap)getCommandMap.invoke(plugin.getServer());
                    },
                    e -> plugin.getLogs().error("Failed to get command map in this minecraft version."),
                    () -> null
            );
        }

        this.commandMap = commandMap;
        this.bukkitCommands = PluginConsumer.ofUnchecked(
                () -> {
                    Field bukkitCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                    bukkitCommands.setAccessible(true);
                    return (Map<String, Command>) bukkitCommands.get(this.commandMap);
                },
                e -> {},
                () -> null
        );
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void registerCommand(final String name, CommandExecutable executable) {
        final org.bukkit.command.Command oldCommand = commandMap.getCommand(name);

        if (
                oldCommand instanceof PluginIdentifiableCommand &&
                        (
                                (executable.overwriteCommand())
                                        || (!executable.overwriteCommand() && ((PluginIdentifiableCommand) oldCommand).getPlugin() == executable.getMeteorPlugin())
                        )
        ) {
            bukkitCommands.remove(name);
            oldCommand.unregister(commandMap);
        }

        String fallbackName = executable.getMeteorPlugin().getName().toLowerCase(Locale.ENGLISH);

        commandMap.register(executable.getCommand(), fallbackName, executable);

        if (executable.getAliases() == null || executable.getAliases().isEmpty()) {
            return;
        }

        for (String alias : executable.getAliases()) {
            if (alias == null) {
                continue;
            }

            PluginConsumer.ofUnchecked(
                    () -> {
                        if (commandMap.getCommand(alias) != null) {
                            bukkitCommands.remove(alias);
                        }
                        commandMap.register(alias, fallbackName, executable);
                        return true;
                    },
                    e -> {},
                    () -> {
                        registerCommand(executable, alias);
                        executable.getLogs().info("Registered command \"" + alias + "\".");
                        return true;
                    }
            );
        }
    }

    @Override
    public void registerCommand(CommandExecutable executable, String alias) {
        PluginConsumer.process(
                () -> {
                    final org.bukkit.command.Command oldCommand = commandMap.getCommand(alias);

                    if (
                        oldCommand instanceof PluginIdentifiableCommand &&
                        (
                            (executable.overwriteCommand())
                            || (!executable.overwriteCommand() && ((PluginIdentifiableCommand) oldCommand).getPlugin() == executable.getMeteorPlugin())
                        )
                    ) {
                        bukkitCommands.remove(alias);
                        oldCommand.unregister(commandMap);
                    }

                    String fallbackName = executable.getMeteorPlugin().getName().toLowerCase(Locale.ENGLISH);

                    commandMap.register(alias, fallbackName, executable);
                },
                e -> {}
        );
    }

    @Override
    public CommandLoader register(CommandExecutable command) {
        return register(
                command.getCommand(),
                command
        );
    }

    @Override
    public CommandLoader register(String commandName, CommandExecutable commandClass) {
        if (commandMap != null) {
            registerCommand(commandName, commandClass);
        }
        return this;
    }

    @Override
    public CommandLoader unregister(CommandExecutable command) {
        return unregister(command.getCommand());
    }

    @Override
    public CommandLoader unregister(String commandName) {
        if (commandMap == null) {
            return this;
        }

        Command command = commandMap.getCommand(commandName);

        if (command == null) {
            return this;
        }

        command.unregister(
            commandMap
        );
        return this;
    }

}
