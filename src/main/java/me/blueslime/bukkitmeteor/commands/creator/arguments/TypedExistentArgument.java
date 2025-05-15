package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.ExistentArgument;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.TabCompletable;
import me.blueslime.bukkitmeteor.commands.sender.Sender;
import me.blueslime.bukkitmeteor.implementation.Implements;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a command with defined arguments.
 * Supports typed arguments and success/failure handling.
 */
public class TypedExistentArgument implements ExistentArgument, TabCompletable {
    private final List<ArgumentEntry<?>> arguments = new ArrayList<>();
    private RunnableExecution execution;

    @FunctionalInterface
    public interface RunnableExecution {
        void run(Sender sender, Object... args);
    }

    public static TypedExistentArgument with(ArgumentEntry<?>... entries) {
        TypedExistentArgument arg = new TypedExistentArgument();
        arg.arguments.addAll(Arrays.asList(entries));
        return arg;
    }

    public TypedExistentArgument run(RunnableExecution execution) {
        this.execution = execution;
        return this;
    }

    @Override
    public boolean handle(Sender sender, String[] args) {
        if (args.length < arguments.size()) {
            arguments.get(args.length).handleMissing(sender);
            return true;
        }

        Object[] parsedArgs = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            String input = args[i];
            ArgumentEntry<?> entry = arguments.get(i);
            Object value = entry.cast(input);
            if (value == null) {
                entry.handleCastFailure(sender);
                return true;
            }
            parsedArgs[i] = value;
        }

        if (execution != null) {
            execution.run(sender, parsedArgs);
        }
        return true;
    }

    @Override
    public List<String> complete(Sender sender, String[] args) {
        int idx = args.length - 1;
        // Si está completando el argumento idx
        if (idx < 0 || idx >= arguments.size()) {
            return List.of();
        }
        Class<?> type = arguments.get(idx).getArgumentClass();
        // Sugerencias básicas para Boolean
        if (type == Boolean.class) {
            return List.of("true", "false");
        }
        if (type == Player.class || type == OfflinePlayer.class) {
            return Implements.fetch(BukkitMeteorPlugin.class)
                .getServer()
                .getOnlinePlayers()
                .stream()
                .map(HumanEntity::getName)
                .collect(Collectors.toList());
        }
        if (type == World.class) {
            return Implements.fetch(BukkitMeteorPlugin.class)
                .getServer()
                .getWorlds()
                .stream()
                .map(World::getName)
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
