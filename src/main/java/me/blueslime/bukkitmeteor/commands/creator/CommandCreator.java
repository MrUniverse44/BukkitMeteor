package me.blueslime.bukkitmeteor.commands.creator;

import me.blueslime.bukkitmeteor.commands.creator.arguments.EmptyCommandArgumentHandler;
import me.blueslime.bukkitmeteor.commands.creator.arguments.SubCommandArgument;
import me.blueslime.bukkitmeteor.commands.creator.arguments.SubCommandArgumentHandler;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.ArgumentHandler;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.EmptyCommandArgument;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.ExistentArgument;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommandCreator {

    private final Set<ArgumentHandler> argumentHandlers = new HashSet<>();
    private final Set<String> aliases = new HashSet<>();
    private final String commandName;

    private boolean tabCompletable = true;

    private CommandCreator(String commandName) {
        this.commandName = commandName;
    }

    public static CommandCreator create(String commandName) {
        return new CommandCreator(commandName);
    }

    public String getCommandName() {
        return commandName;
    }

    public CommandCreator aliases(Collection<String> aliases) {
        this.aliases.addAll(aliases);
        return this;
    }

    public CommandCreator aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandCreator alias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    public CommandCreator allowArgumentTabComplete(boolean tabCompletable) {
        this.tabCompletable = tabCompletable;
        return this;
    }

    public CommandCreator addArgument(SubCommandArgument subCommandArgument) {
        this.argumentHandlers.add(subCommandArgument);
        return this;
    }

    public CommandCreator addArgument(SubCommandArgumentHandler subCommandArgumentHandler) {
        this.argumentHandlers.add(subCommandArgumentHandler);
        return this;
    }

    public CommandCreator addArgument(EmptyCommandArgumentHandler emptyCommandArgumentHandler) {
        this.argumentHandlers.add(emptyCommandArgumentHandler);
        return this;
    }

    public CommandCreator addArgument(ExistentArgument argumentHandler) {
        this.argumentHandlers.add(argumentHandler);
        return this;
    }

    public CommandCreator addArgument(EmptyCommandArgument argumentHandler) {
        this.argumentHandlers.add(argumentHandler);
        return this;
    }

    public CommandCreator removeAliases(Collection<String> aliases) {
        aliases.forEach(this.aliases::remove);
        return this;
    }

    public CommandCreator removeAliases(String... aliases) {
        Arrays.asList(aliases).forEach(this.aliases::remove);
        return this;
    }

    public CommandCreator removeAlias(String alias) {
        this.aliases.remove(alias);
        return this;
    }

    public CommandExecutable build() {
        return new BukkitMeteorCommand(this);
    }

    public boolean hasTabCompletable() {
        return tabCompletable;
    }

    public Set<ArgumentHandler> getArgumentHandlers() {
        return argumentHandlers;
    }

    public Set<String> getAliases() {
        return aliases;
    }
}
