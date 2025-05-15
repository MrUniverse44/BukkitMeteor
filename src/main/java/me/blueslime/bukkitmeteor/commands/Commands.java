package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.creator.CommandExecutable;
import me.blueslime.bukkitmeteor.commands.loader.CommandLoader;

public abstract class Commands {

    private static Commands LOADER_INSTANCE = null;

    public static Commands build(BukkitMeteorPlugin plugin) {
        if (LOADER_INSTANCE == null) {
            LOADER_INSTANCE = new CommandLoader(plugin);
        }
        return LOADER_INSTANCE;
    }

    public static void replaceCommandLoader(Commands loader) {
        LOADER_INSTANCE = loader;
    }

    public abstract void registerCommand(final String name, CommandExecutable executable);

    public abstract void registerCommand(CommandExecutable executable, String alias);

    public abstract Commands register(CommandExecutable command);

    public abstract Commands register(String commandName, CommandExecutable commandClass);

    public Commands unregister(CommandExecutable command) {
        return unregister(command.getCommand());
    }

    public abstract Commands unregister(String commandName);

    public void finish() {

    }

}

