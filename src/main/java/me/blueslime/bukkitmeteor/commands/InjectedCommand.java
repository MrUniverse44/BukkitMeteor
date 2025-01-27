package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.utilitiesapi.commands.AdvancedCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InjectedCommand extends AdvancedCommand<BukkitMeteorPlugin> implements AdvancedModule {

    public InjectedCommand(String command) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command);
    }

    public InjectedCommand(String command, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, Collections.unmodifiableList(aliases));
    }

    public InjectedCommand(String command, List<String> aliases, boolean customList) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, customList ? aliases : Collections.unmodifiableList(aliases));
    }

    public InjectedCommand(FileConfiguration configuration, String commandPath, String aliasesPath) {
        super(
            Implements.fetch(BukkitMeteorPlugin.class),
            configuration != null ?
                configuration.getString(commandPath, "") :
                "",
            "Plugin Command",
            "/<command> <args>",
            configuration != null ?
                Collections.unmodifiableList(configuration.getStringList(aliasesPath)) :
                Collections.unmodifiableList(new ArrayList<>())
        );
    }

    public InjectedCommand(String command, String description, String usageMessage, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, description, usageMessage, Collections.unmodifiableList(aliases));
    }

    @Override
    public boolean overwriteCommand() {
        return true;
    }

    public InjectedCommand() {
        super(Implements.fetch(BukkitMeteorPlugin.class));
    }
}
