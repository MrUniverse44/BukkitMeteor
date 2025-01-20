package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.tasks.SchedulerService;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import java.io.File;

public interface Service extends AdvancedModule {

    /**
     * Get main folder of your plugin
     * @return folder
     */
    default File getDataFolder() {
        return fetch(File.class, "folder");
    }

    /**
     * Get the logs of your plugin
     * @return plugin logs
     */
    default MeteorLogger getLogs() {
        return fetch(MeteorLogger.class);
    }

    /**
     * Get the server instance
     * @return server instance
     */
    default Server getServer() {
        return fetch(BukkitMeteorPlugin.class).getServer();
    }

    /**
     * Gets the scheduler service
     * @return scheduler
     */
    default SchedulerService getScheduler() {
        return fetch(SchedulerService.class);
    }

    /**
     * Gets the plugin manager instance
     * @return instance
     */
    default PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }

    /**
     * Checks if a plugin is enabled
     * @param name of the plugin
     * @return result
     */
    default boolean isPluginEnabled(String name) {
        return getPluginManager().isPluginEnabled(name);
    }

}
