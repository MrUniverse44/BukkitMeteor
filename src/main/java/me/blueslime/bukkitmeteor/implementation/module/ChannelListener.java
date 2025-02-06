package me.blueslime.bukkitmeteor.implementation.module;

import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Set;

public interface ChannelListener extends PluginMessageListener, Service {

    /**
     * Channels for this channel listener
     * @return channel set
     */
    default Set<String> getListenerChannels() {
        return Set.of(getChannel());
    }

    /**
     * Channel id for this channel listener
     * If you have more channels please ignore this
     * and overwrite {@link #getListenerChannels()}
     * @return channel id
     */
    String getChannel();

}
