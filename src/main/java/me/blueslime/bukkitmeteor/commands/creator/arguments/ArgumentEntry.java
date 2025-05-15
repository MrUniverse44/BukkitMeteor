package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.sender.Sender;
import me.blueslime.bukkitmeteor.implementation.Implements;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Represents a single argument of a command.
 * Handles casting, missing value and cast failure logic.
 *
 * @param <T> the type of the argument (e.g., String, Integer)
 */
public class ArgumentEntry<T> {

    private Consumer<Sender> ifCastFailed = null;
    private Consumer<Sender> ifMissing = null;
    private final Class<T> clazz;

    private ArgumentEntry(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <T> ArgumentEntry<T> of(Class<T> clazz) {
        return new ArgumentEntry<>(clazz);
    }

    public ArgumentEntry<T> ifMissing(Consumer<Sender> action) {
        this.ifMissing = action;
        return this;
    }

    public ArgumentEntry<T> ifCastFailed(Consumer<Sender> action) {
        this.ifCastFailed = action;
        return this;
    }

    public Class<T> getArgumentClass() {
        return clazz;
    }

    public void handleMissing(Sender sender) {
        if (ifMissing != null) {
            ifMissing.accept(sender);
        }
    }

    public void handleCastFailure(Sender sender) {
        if (ifCastFailed != null) {
            ifCastFailed.accept(sender);
        }
    }

    @SuppressWarnings("deprecation")
    public T cast(String input) {

        try {
            if (clazz == String.class) return clazz.cast(input);
            if (clazz == Integer.class) return clazz.cast(Integer.parseInt(input));
            if (clazz == Double.class) return clazz.cast(Double.parseDouble(input));
            if (clazz == Boolean.class) return clazz.cast(Boolean.parseBoolean(input));
            if (clazz == Float.class) return clazz.cast(Float.parseFloat(input));
            if (clazz == Byte.class) return clazz.cast(Byte.parseByte(input));
            if (clazz == Long.class) return clazz.cast(Long.parseLong(input));
            if (clazz == Short.class) return clazz.cast(Short.parseShort(input));
            if (clazz == Character.class) return clazz.cast(input.charAt(0));
            if (clazz == Player.class) return clazz.cast(
                getServer().getPlayerExact(input)
            );
            if (clazz == World.class) return clazz.cast(getServer().getWorld(input));
            if (clazz == OfflinePlayer.class) return clazz.cast(getServer().getOfflinePlayer(input));
            // Add more if needed
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Server getServer() {
        return Implements.fetch(BukkitMeteorPlugin.class).getServer();
    }
}
