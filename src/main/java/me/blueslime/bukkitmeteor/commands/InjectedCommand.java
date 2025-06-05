package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.utilitiesapi.commands.AdvancedCommand;
import me.blueslime.utilitiesapi.commands.sender.Sender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class InjectedCommand extends AdvancedCommand<BukkitMeteorPlugin> implements AdvancedModule {

    /**
     * Create a command
     * @param command to register
     */
    public InjectedCommand(String command) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command);
    }

    /**
     * Create a command
     * @param command to register
     * @param aliases for the command
     */
    public InjectedCommand(String command, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, aliases);
    }

    /**
     * Create a command from configuration data
     * @param configuration for command data
     * @param commandPath from the configuration
     * @param aliasesPath from the configuration
     */
    public InjectedCommand(FileConfiguration configuration, String commandPath, String aliasesPath) {
        super(
            Implements.fetch(BukkitMeteorPlugin.class),
            configuration != null ?
                configuration.getString(commandPath, "") :
                "",
            "Plugin Command",
            "/<command> <args>",
            configuration != null ?
                configuration.getStringList(aliasesPath) :
                new CopyOnWriteArrayList<>()
        );
    }

    /**
     * Create a command
     * @param command to register
     * @param description for the command
     * @param usageMessage for the command
     * @param aliases for the command
     */
    public InjectedCommand(String command, String description, String usageMessage, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, description, usageMessage, Collections.unmodifiableList(aliases));
    }

    /**
     * You can execute a command here
     * @param sender of the command
     * @param label label
     * @param args of the command executed.
    <repository>
    <id>comugamers-releases</id>
    <url>https://repo.comugamers.com/repository/maven-group/</url>
    </repository>
     */
    @Override
    public abstract void executeCommand(Sender sender, String label, String[] args);

    @Override
    public boolean overwriteCommand() {
        return true;
    }

    public InjectedCommand() {
        super(Implements.fetch(BukkitMeteorPlugin.class));
    }
}
