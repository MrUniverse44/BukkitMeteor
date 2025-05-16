package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.commands.creator.interfaces.*;
import me.blueslime.bukkitmeteor.commands.sender.Sender;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a literal sub‑command node.
 * If the first argument equals `label`, it will execute the rest of args from this node.
 */
public abstract class SubCommandArgumentHandler implements ArgumentHandler, TabCompletable {

    private final Set<String> aliases = new HashSet<>();
    private final String label;
    private final List<ArgumentHandler> children = new ArrayList<>();

    public SubCommandArgumentHandler(String label) {
        this.label = label;
        registerAliases();
        registerSubArguments();
        registerAll();
    }

    private void registerAll() {
        Set<ArgumentEntry<?>> argumentMissingEntries = new HashSet<>();
        Set<ArgumentEntry<?>> argumentCastEntries = new HashSet<>();

        for (Method m : super.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(MissingArgument.class)) {
                MissingArgument argument = m.getAnnotation(MissingArgument.class);

                Class<?> arg = argument.value();
                int index = argument.index();

                argumentMissingEntries.add(
                    ArgumentEntry.of(arg).addMissingHandle(
                        new ArgumentEntry.ArgumentMethod(this, m, index, arg)
                    )
                );
            }
            if (m.isAnnotationPresent(CastFailedArgument.class)) {
                CastFailedArgument argument = m.getAnnotation(CastFailedArgument.class);

                Class<?> arg = argument.value();
                int index = argument.index();

                argumentCastEntries.add(
                    ArgumentEntry.of(arg).addCastFailed(
                        new ArgumentEntry.ArgumentMethod(this, m, index, arg)
                    )
                );
            }
        }

        for (Method m : super.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Argument.class)) {
                Argument argument = m.getAnnotation(Argument.class);
                String name = argument.name();
                String[] aliases = argument.aliases();

                List<ArgumentEntry<?>> argumentEntryList = new ArrayList<>();

                for (Parameter p : m.getParameters()) {
                    Set<ArgumentEntry<?>> argumentMissing = argumentMissingEntries
                        .stream()
                        .filter(entry -> entry.getArgumentClass() == p.getType())
                        .collect(Collectors.toSet());

                    Set<ArgumentEntry<?>> argumentCastFailed = argumentCastEntries
                        .stream()
                        .filter(entry -> entry.getArgumentClass() == p.getType())
                        .collect(Collectors.toSet());

                    ArgumentEntry<?> combined = ArgumentEntry.of(argumentMissing, argumentCastFailed, p.getType());

                    argumentEntryList.add(combined);
                }

                sub(
                    name,
                    aliases,
                    TypedExistentArgument.with(
                        argumentEntryList
                    ).run(
                        new TypedExistentArgument.RunnableMethod(m, this)
                    )
                );
            }

            if (m.isAnnotationPresent(EmptyArgument.class)) {
                withHandler(
                    EmptyMethodCommandHandler.create()
                        .add(new TypedExistentArgument.RunnableMethod(m, this))
                );
            }
        }


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
     * Add a new sub argument node
     * @param name for this argument
     * @param handler for this node
     * @return this instance
     */
    public SubCommandArgumentHandler sub(String name, ArgumentHandler handler) {
        this.children.add(SubCommandArgument.of(name).withHandler(handler));
        return this;
    }

    /**
     * Add a new sub argument node
     * @param name for this argument
     * @param handler for this node
     * @return this instance
     */
    public SubCommandArgumentHandler sub(String name, String[] aliases, ArgumentHandler handler) {
        this.children.add(SubCommandArgument.of(name).addAliases(aliases).withHandler(handler));
        return this;
    }

    /**
     * Define a direct handler for this node, with an extra key this will work for
     * the '/(command) (this sub arg name)' without more args
     */
    public SubCommandArgumentHandler withHandler(ArgumentHandler handler) {
        this.children.add(handler);
        return this;
    }

    public void registerAliases() {

    }

    public void registerSubArguments() {

    }

    /**
     * Show a message when a subcommand doesn't exist
     * @return same instance
     */
    public Consumer<Sender> onUnknownSub() {
        return null;
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

        Consumer<Sender> onUnknownSub = onUnknownSub();

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


