package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implementer;
import me.blueslime.bukkitmeteor.implementation.entries.Entries;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.tasks.SchedulerService;
import org.bukkit.Server;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public interface AdvancedModule extends PersistentModule, Implementer, Entries {

    default void registerAll(List<Class<? extends Listener>> listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        PluginManager manager = plugin.getServer().getPluginManager();

        for (Class<? extends Listener> listenerClass : listeners) {
            manager.registerEvents(createInstance(listenerClass), plugin);
        }
    }

    default void registerAll(Listener... listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        PluginManager manager = plugin.getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, plugin);
        }
    }

    /**
     * Gets the scheduler service
     * @return scheduler
     */
    default SchedulerService getScheduler() {
        return fetch(SchedulerService.class);
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

    default void unregisterAll(Listener... listeners) {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
