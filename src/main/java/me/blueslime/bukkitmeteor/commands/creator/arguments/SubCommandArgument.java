package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.commands.creator.interfaces.ArgumentHandler;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.TabCompletable;
import me.blueslime.bukkitmeteor.commands.sender.Sender;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a literal sub‑command node.
 * If the first argument equals `label`, it will execute the rest of args from this node.
 */
public class SubCommandArgument implements ArgumentHandler, TabCompletable {

    private final Set<String> aliases = new HashSet<>();
    private final String label;
    private final List<ArgumentHandler> children = new ArrayList<>();
    private Consumer<Sender> onUnknownSub;

    private SubCommandArgument(String label) {
        this.label = label;
    }

    public void addAlias(String alias) {
        aliases.add(alias.toLowerCase(Locale.ENGLISH));
    }

    public void addAliases(String... aliases) {
        for (String alias : aliases) {
            addAlias(alias.toLowerCase(Locale.ENGLISH));
        }
    }

    public void addAliases(Collection<String> aliases) {
        for (String alias : aliases) {
            addAlias(alias.toLowerCase(Locale.ENGLISH));
        }
    }

    public void removeAlias(String alias) {
        aliases.remove(alias.toLowerCase(Locale.ENGLISH));
    }

    public void removeAliases(String... aliases) {
        for (String alias : aliases) {
            removeAlias(alias.toLowerCase(Locale.ENGLISH));
        }
    }

    public void removeAliases(Collection<String> aliases) {
        for (String alias : aliases) {
            removeAlias(alias.toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Create a match to work with `label`.
     * @param label for this sub command argument.
     */
    public static SubCommandArgument of(String label) {
        return new SubCommandArgument(label);
    }

    /**
     * Add a new sub argument node
     * @param name for this argument
     * @param handler for this node
     * @return this instance
     */
    public SubCommandArgument sub(String name, ArgumentHandler handler) {
        this.children.add(SubCommandArgument.of(name).withHandler(handler));
        return this;
    }

    /**
     * Define a direct handler for this node, with an extra key this will work for
     * the '/(command) (this sub arg name)' without more args
     */
    public SubCommandArgument withHandler(ArgumentHandler handler) {
        this.children.add(handler);
        return this;
    }

    /**
     * Show a message when a subcommand doesn't exist
     * @param action to execute
     * @return same instance
     */
    public SubCommandArgument onUnknownSub(Consumer<Sender> action) {
        this.onUnknownSub = action;
        return this;
    }

    @Override
    public boolean handle(Sender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase(label) && !aliases.contains(args[0].toLowerCase(Locale.ENGLISH))) {
            return false;
        }

        String[] rest = Arrays.copyOfRange(args, 1, args.length);

        for (ArgumentHandler child : children) {
            if (child.handle(sender, rest)) {
                return true;
            }
        }

        if (onUnknownSub != null) {
            onUnknownSub.accept(sender);
            return true;
        }
        return true;
    }

    @Override
    public List<String> complete(Sender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 0) {
            // Sugerir la palabra literal de este nodo
            completions.add(label);
            completions.addAll(aliases);
            return completions;
        }
        if (args.length == 1) {
            // Si está escribiendo el primer token, sugerimos label si coincide prefijo
            if (label.startsWith(args[0].toLowerCase(Locale.ENGLISH))) {
                completions.add(label);
            }
            aliases.stream().filter(alias -> alias.startsWith(args[0].toLowerCase(Locale.ENGLISH))).forEach(
                completions::add
            );
            return completions;
        }
        // args.length >= 2: buscamos qué hijo debería completar
        String first = args[0];
        if (!first.equalsIgnoreCase(label)) {
            return Collections.emptyList();
        }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        for (ArgumentHandler child : children) {
            if (child instanceof TabCompletable) {
                List<String> sub = ((TabCompletable) child).complete(sender, rest);
                if (!sub.isEmpty()) {
                    return sub;
                }
            }
        }
        return Collections.emptyList();
    }
}

