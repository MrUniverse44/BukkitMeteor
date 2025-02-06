package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implementer;
import me.blueslime.bukkitmeteor.implementation.entries.Entries;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.tasks.SchedulerService;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public interface AdvancedModule extends PersistentModule, Implementer, Entries {

    /**
     * Register listeners
     * @param listeners to register
     */
    default void registerAll(List<Class<? extends Listener>> listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        PluginManager manager = plugin.getServer().getPluginManager();

        for (Class<? extends Listener> listenerClass : listeners) {
            manager.registerEvents(createInstance(listenerClass), plugin);
        }
    }

    /**
     * Register listeners
     * @param listeners to register
     */
    default void registerAll(Listener... listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        PluginManager manager = plugin.getServer().getPluginManager();

        for (Listener listener : listeners) {
            manager.registerEvents(listener, plugin);
        }
    }

    /**
     * Register outgoing channels
     * @param channels to register
     */
    default void registerOutgoingChannels(String... channels) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        Messenger messenger = plugin.getServer().getMessenger();

        for (String channel : channels) {
            messenger.registerOutgoingPluginChannel(
                plugin,
                channel
            );
        }
    }

    /**
     * Register all incoming channels
     * @param listeners to register
     */
    default void registerAllIncoming(List<Class<? extends ChannelListener>> listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        Messenger manager = plugin.getServer().getMessenger();

        for (Class<? extends ChannelListener> listenerClass : listeners) {
            ChannelListener listener = createInstance(listenerClass);

            for (String channel : listener.getListenerChannels()) {
                manager.registerIncomingPluginChannel(plugin, channel, listener);
            }
        }
    }

    /**
     * Register all incoming channels
     * @param listeners to register
     */
    default void registerAllIncoming(ChannelListener... listeners) {
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);

        Messenger manager = plugin.getServer().getMessenger();

        for (ChannelListener listener : listeners) {
            for (String channel : listener.getListenerChannels()) {
                manager.registerIncomingPluginChannel(plugin, channel, listener);
            }
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

    /**
     * Register server messenger
     * @return server messenger
     */
    default Messenger getMessenger() {
        return fetch(BukkitMeteorPlugin.class).getServer().getMessenger();
    }

    /**
     * Unregister listener classes
     * @param listeners to unregister
     */
    default void unregisterAll(Listener... listeners) {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    /**
     * Gets the world using the world name
     * @param name of the world
     * @return world if presents or empty if not
     */
    default Optional<World> getWorld(String name) {
        return Optional.ofNullable(getServer().getWorld(name));
    }

    /**
     * Gets the player instance using the player uniqueId
     * @param uniqueId of the player
     * @return player if presents or empty if not
     */
    default Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(getServer().getPlayer(uniqueId));
    }
}
