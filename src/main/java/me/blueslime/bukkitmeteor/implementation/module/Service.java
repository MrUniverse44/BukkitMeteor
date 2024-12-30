package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import java.io.File;

public interface Service extends AdvancedModule {

    default File getDataFolder() {
        return fetch(File.class, "folder");
    }

    default MeteorLogger getLogs() {
        return fetch(MeteorLogger.class);
    }

    default Server getServer() {
        return fetch(BukkitMeteorPlugin.class).getServer();
    }

    default PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }

    default boolean isPluginEnabled(String name) {
        return getPluginManager().isPluginEnabled(name);
    }

}
