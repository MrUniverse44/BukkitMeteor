package me.blueslime.bukkitmeteor.implementation.module;

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
