package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.utilitiesapi.commands.AdvancedCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public abstract class InjectedCommand extends AdvancedCommand<BukkitMeteorPlugin> {
    public InjectedCommand(String command) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command);
    }

    public InjectedCommand(String command, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, aliases);
    }

    public InjectedCommand(FileConfiguration configuration, String commandPath, String aliasesPath) {
        super(Implements.fetch(BukkitMeteorPlugin.class), configuration, commandPath, aliasesPath);
    }

    public InjectedCommand(String command, String description, String usageMessage, List<String> aliases) {
        super(Implements.fetch(BukkitMeteorPlugin.class), command, description, usageMessage, aliases);
    }

    public InjectedCommand() {
        super(Implements.fetch(BukkitMeteorPlugin.class));
    }
}
