package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.sender.Sender;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents a single argument of a command.
 * Handles casting, missing value and cast failure logic.
 *
 * @param <T> the type of the argument (e.g., String, Integer)
 */
public class ArgumentEntry<T> {

    private final Set<ArgumentMethod> missingMethodHandlers = new HashSet<>();
    private final Set<ArgumentMethod> castMethodHandlers = new HashSet<>();

    private Consumer<Sender> ifCastFailed = null;
    private Consumer<Sender> ifMissing = null;

    private final Class<T> clazz;

    private ArgumentEntry(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static class ArgumentMethod {
        protected Object clazz;
        protected Method method;
        protected Class<?> required;
        protected int index;

        public ArgumentMethod(Object clazz, Method method, int index, Class<?> required) {
            this.clazz = clazz;
            this.method = method;
            this.index = index;
            this.required = required;
        }

        protected void invoke(Sender sender) {
            PluginConsumer.process(
                () -> method.invoke(clazz, sender),
                e -> {}
            );
        }
    }

    public static <T> ArgumentEntry<T> of(Set<ArgumentEntry<?>> missingHandlers, Set<ArgumentEntry<?>> castHandlers, Class<T> resultClass) {
        ArgumentEntry<T> result = new ArgumentEntry<>(resultClass);

        for (ArgumentEntry<?> entry : missingHandlers) {
            result.getMissingHandlers().addAll(entry.getMissingHandlers());
        }

        for (ArgumentEntry<?> entry : castHandlers) {
            result.getCastMethodHandlers().addAll(entry.getCastMethodHandlers());
        }

        return result;
    }

    protected Set<ArgumentMethod> getMissingHandlers() {
        return missingMethodHandlers;
    }

    protected Set<ArgumentMethod> getCastMethodHandlers() {
        return castMethodHandlers;
    }

    public static <T> ArgumentEntry<T> of(Class<T> clazz) {
        return new ArgumentEntry<>(clazz);
    }

    public ArgumentEntry<T> addMissingHandle(ArgumentMethod method) {
        missingMethodHandlers.add(method);
        return this;
    }

    public ArgumentEntry<T> removeMissingHandle(ArgumentMethod method) {
        missingMethodHandlers.remove(method);
        return this;
    }

    public ArgumentEntry<T> ifMissing(Consumer<Sender> action) {
        this.ifMissing = action;
        return this;
    }

    public ArgumentEntry<T> addCastFailed(ArgumentMethod method) {
        castMethodHandlers.add(method);
        return this;
    }

    public ArgumentEntry<T> removeCastFailed(ArgumentMethod method) {
        castMethodHandlers.remove(method);
        return this;
    }

    public ArgumentEntry<T> ifCastFailed(Consumer<Sender> action) {
        this.ifCastFailed = action;
        return this;
    }

    public Class<T> getArgumentClass() {
        return clazz;
    }

    public void handleMissing(Sender sender, int index) {
        if (ifMissing != null) {
            ifMissing.accept(sender);
        }
        missingMethodHandlers.stream()
            .filter(method -> (method.index == -1 || method.index == index) && method.required == clazz)
            .forEach(method -> method.invoke(sender));
    }

    public void handleCastFailure(Sender sender, int index) {
        if (ifCastFailed != null) {
            ifCastFailed.accept(sender);
        }
        castMethodHandlers.stream()
            .filter(method -> (method.index == -1 || method.index == index) && method.required == clazz)
            .forEach(method -> method.invoke(sender));
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
