package me.blueslime.bukkitmeteor.commands.creator;


import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.commands.sender.Sender;
import me.blueslime.bukkitmeteor.commands.Commands;
import me.blueslime.utilitiesapi.color.ColorHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.command.*;
import org.bukkit.Location;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"unused"})
public abstract class CommandExecutable extends BukkitCommand implements Service {
    private final List<String> aliases;
    private String command;

    public CommandExecutable(String command) {
        this(command, "Plugin Command", "/" + command + " <args>", new CopyOnWriteArrayList<>());
    }

    public CommandExecutable(String command, List<String> aliases) {
        this(command, "Plugin Command", "/" + command + " <args>", new CopyOnWriteArrayList<>(aliases));
    }

    public CommandExecutable(FileConfiguration configuration, String commandPath, String aliasesPath) {
        this(
            configuration != null ? configuration.getString(commandPath, "") : "",
            "Plugin Command",
            "/<command> <args>",
            configuration != null ? new CopyOnWriteArrayList<>(configuration.getStringList(aliasesPath)) : new CopyOnWriteArrayList<>()
        );
    }

    public CommandExecutable(String command, String description, String usageMessage, List<String> aliases) {
        super(command, description, usageMessage, aliases);
        this.aliases = new CopyOnWriteArrayList<>(aliases);
        this.command = command;
    }

    public CommandExecutable() {
        this(null);
    }

    public boolean overwriteCommand() {
        return false;
    }

    public void register() {
        if (this.command != null) {
            Commands.build(getMeteorPlugin())
                .register(this)
                .finish();
        }
    }

    public void unregister() {
        Commands.build(getMeteorPlugin())
            .unregister(this)
            .finish();
    }

    public CommandExecutable setCommand(String command) {
        this.command = command;
        super.setName(command);
        return this;
    }

    public @NotNull CommandExecutable setAliases(@NotNull List<String> aliases) {
        super.setAliases(aliases);
        return this;
    }

    public @NotNull CommandExecutable setDescription(@NotNull String description) {
        super.setDescription(description);
        return this;
    }

    public @NotNull CommandExecutable setUsage(@NotNull String message) {
        super.setUsage(message);
        return this;
    }

    @NotNull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Command Execution
     * @param sender Source object which is executing this command
     * @param command The alias of the command used
     * @param arguments All arguments passed to the command, split via ' '
     */
    public abstract void executeCommand(Sender sender, String command, String[] arguments);

    /**
     * Command execution
     * @param sender Source object which is executing this command
     * @param label The alias of the command used
     * @param arguments All arguments passed to the command, split via ' '
     * @return value
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] arguments) {
        executeCommand(Sender.build(sender), this.command, arguments);
        return true;
    }

    /**
     * Tab Complete Execution
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param arguments All arguments passed to the command, split via ' '
     * @return result list
     * @throws IllegalArgumentException when the return value is null
     */
    public List<String> onTabComplete(Sender sender, String alias, String[] arguments) {
        return super.tabComplete(sender.toCommandSender(), alias, arguments);
    }

    /**
     * Tab Complete Execution
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param arguments All arguments passed to the command, split via ' '
     * @param location The position looked at by the sender, or null if none
     * @return result list
     * @throws IllegalArgumentException when the return value is null
     */
    public List<String> onTabComplete(Sender sender, String alias, String[] arguments, Location location) {
        return super.tabComplete(sender.toCommandSender(), alias, arguments);
    }

    /**
     * Tab Complete Execution
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param args All arguments passed to the command, split via ' '
     * @return result list
     * @throws IllegalArgumentException when the return value is null
     */
    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return onTabComplete(Sender.build(sender), alias, args);
    }

    /**
     * Tab Complete Execution
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param args All arguments passed to the command, split via ' '
     * @param location The position looked at by the sender, or null if none
     * @return result list
     * @throws IllegalArgumentException when the return value is null
     */
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        return onTabComplete(Sender.build(sender), alias, args, location);
    }

    public String getCommand() {
        return command;
    }

    public PluginDescriptionFile getDescriptionFile() {
        return getMeteorPlugin().getDescription();
    }

    public static String colorize(String text) {
        return ColorHandler.convert(text);
    }
}
