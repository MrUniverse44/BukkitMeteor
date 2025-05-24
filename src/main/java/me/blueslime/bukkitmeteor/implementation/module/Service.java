package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.events.EventExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public interface Service extends AdvancedModule {

    /**
     * Get main folder of your plugin
     * @return folder
     */
    default File getDataFolder() {
        return fetch(File.class, "folder");
    }

    default <T extends Event> EventExecutor<T> createEvent(Class<T> event) {
        return createEvent(event, EventPriority.NORMAL);
    }

    default <T extends Event> EventExecutor<T> createEvent(Class<T> event, EventPriority priority) {
        return new EventExecutor<T>(event, priority);
    }

    /**
     * Gets the player instance using the player uniqueId
     * @param name of the player
     * @return player if presents or empty if not
     */
    default Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(getServer().getPlayerExact(name));
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
