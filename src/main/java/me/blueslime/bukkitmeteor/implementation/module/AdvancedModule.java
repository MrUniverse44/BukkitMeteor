package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implementer;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.entries.Entries;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public interface AdvancedModule extends PersistentModule, Implementer, Entries {
    default <T> T fetch(Class<T> clazz) {
        return Implements.fetch(clazz);
    }

    default <T> T fetch(Class<T> clazz, String identifier) {
        return Implements.fetch(clazz, identifier);
    }

    default void registerAll(Listener... listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        PluginManager manager = plugin.getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, plugin);
        }
    }

    default void unregisterAll(Listener... listeners) {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
