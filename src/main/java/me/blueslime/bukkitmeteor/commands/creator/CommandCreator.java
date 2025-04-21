package me.blueslime.bukkitmeteor.commands.creator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommandCreator {

    private final Set<String> aliases = new HashSet<>();
    private final String commandName;

    private CommandCreator(String commandName) {
        this.commandName = commandName;
    }

    public static CommandCreator create(String commandName) {
        return new CommandCreator(commandName);
    }

    public String getCommandName() {
        return commandName;
    }

    public void aliases(Collection<String> aliases) {
        this.aliases.addAll(aliases);
    }

    public void aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void alias(String alias) {
        this.aliases.add(alias);
    }

    public void removeAliases(Collection<String> aliases) {
        aliases.forEach(this.aliases::remove);
    }

    public void removeAliases(String... aliases) {
        Arrays.asList(aliases).forEach(this.aliases::remove);
    }

    public void removeAlias(String alias) {
        this.aliases.remove(alias);
    }



}
