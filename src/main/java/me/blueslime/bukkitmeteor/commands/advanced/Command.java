package me.blueslime.bukkitmeteor.commands.advanced;

import me.blueslime.bukkitmeteor.commands.CommandBuilder;
import me.blueslime.bukkitmeteor.commands.InjectedCommand;
import me.blueslime.bukkitmeteor.commands.issues.CommandArgumentNotFoundException;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.utilitiesapi.commands.sender.Sender;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class Command extends InjectedCommand {

    private final Set<SubCommandHandler> subCommandHandlers = new HashSet<>();

    @SafeVarargs
    public final void registerSubCommandHandler(Class<? extends SubCommandHandler>... subCommandHandlers) {
        if (subCommandHandlers == null) {
            return;
        }
        for (Class<? extends SubCommandHandler> subCommandHandler : subCommandHandlers) {
            SubCommandHandler handler = PluginConsumer.ofUnchecked(
                ()  -> createInstance(subCommandHandler),
                e -> fetch(MeteorLogger.class).error(e, "Can't register sub command handler for a command"),
                () -> null
            );
            if (handler == null) {
                continue;
            }
            this.subCommandHandlers.add(handler);
        }
    }

    public final void registerSubCommandHandler(SubCommandHandler... subCommandHandlers) {
        if (subCommandHandlers == null) {
            return;
        }
        this.subCommandHandlers.addAll(Arrays.asList(subCommandHandlers));
    }

    @Override
    public void executeCommand(Sender sender, String label, String[] args) {
        if (args.length < 1) {
            for (SubCommandHandler handler : subCommandHandlers) {
                Method[] methods = handler.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(DefaultArgument.class) && method.getParameterCount() == 1 && (method.getParameters()[0].getType() == Sender.class || method.getParameters()[0].isAnnotationPresent(CommandSender.class))) {
                        PluginConsumer.process(
                            () -> method.invoke(handler, fetch(CommandBuilder.class).parseSender(sender, method.getParameters()[0])),
                             e -> {
                                 if (e instanceof CommandArgumentNotFoundException argumentException) {
                                     isNot(sender, argumentException, "");
                                     return;
                                 }
                                 MeteorLogger.fetch().error(e, "Error executing default command");
                             }
                        );
                    }
                }
            }
            return;
        }

        String subCommandName = args[0];

        CommandBuilder instance = fetch(CommandBuilder.class);

        for (SubCommandHandler handler : subCommandHandlers) {
            for (Method method : handler.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Argument.class)) {
                    Argument annotation = method.getAnnotation(Argument.class);
                    if (
                        annotation.name().equalsIgnoreCase(subCommandName) ||
                        containsAlias(annotation.aliases(), subCommandName)
                    ) {
                        PluginConsumer.process(
                            () -> {
                                Object[] arguments = instance.parseArguments(sender, sliceArgs(args), method);
                                method.invoke(handler, arguments);
                            },
                            e -> {
                                if (e instanceof CommandArgumentNotFoundException argumentException) {
                                    isNot(sender, argumentException, subCommandName);
                                    return;
                                }
                                MeteorLogger.fetch().error(e, "Error executing subcommand " + annotation.name());
                            }
                        );
                        return;
                    }
                }
            }
        }
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Argument.class)) {
                Argument annotation = method.getAnnotation(Argument.class);
                if (
                    annotation.name().equalsIgnoreCase(subCommandName) ||
                    containsAlias(annotation.aliases(), subCommandName)
                ) {
                    PluginConsumer.process(
                        () -> {
                            Object[] arguments = instance.parseArguments(sender, sliceArgs(args), method);
                            method.invoke(this, arguments);
                        },
                        e -> {
                            if (e instanceof CommandArgumentNotFoundException argumentException) {
                                isNot(sender, argumentException, subCommandName);
                                return;
                            }
                            MeteorLogger.fetch().error(e, "Error executing subcommand " + annotation.name());
                        }
                    );
                    return;
                }
            }
        }
    }

    private void isNot(Sender sender, CommandArgumentNotFoundException argumentException, String subCommandName) {
        CommandBuilder instance = fetch(CommandBuilder.class);

        for (SubCommandHandler handler : subCommandHandlers) {
            for (Method notFoundMethod : handler.getClass().getDeclaredMethods()) {
                if (notFoundMethod.isAnnotationPresent(NotFoundExecuter.class)) {
                    NotFoundExecuter notFoundMethodAnnotation = notFoundMethod.getAnnotation(NotFoundExecuter.class);
                    if (
                        (
                            notFoundMethodAnnotation.argument().isEmpty()
                            || notFoundMethodAnnotation.argument().equalsIgnoreCase(subCommandName)
                            || containsAlias(notFoundMethodAnnotation.aliases(), subCommandName)
                        ) &&
                        (
                            notFoundMethodAnnotation.type().equals(Object.class)
                            || notFoundMethodAnnotation.type().equals(argumentException.getType())
                        ) &&
                        notFoundMethod.getParameterCount() == 1 &&
                        (
                            notFoundMethod.getParameters()[0].getType() == Sender.class
                            || notFoundMethod.getParameters()[0].isAnnotationPresent(CommandSender.class)
                        )
                    ) {
                        PluginConsumer.process(
                            () -> {
                                Object argument = instance.parseSender(sender, notFoundMethod.getParameters()[0]);
                                notFoundMethod.invoke(handler, argument);
                            },
                            notFoundException -> MeteorLogger.fetch().error(notFoundException, "Error executing not found method '" + notFoundMethodAnnotation.argument() + "'")
                        );
                        return;
                    }
                }
            }
        }
        for (Method notFoundMethod : getClass().getDeclaredMethods()) {
            if (notFoundMethod.isAnnotationPresent(NotFoundExecuter.class)) {
                NotFoundExecuter notFoundMethodAnnotation = notFoundMethod.getAnnotation(NotFoundExecuter.class);
                if (
                    (
                        notFoundMethodAnnotation.argument().isEmpty()
                        || notFoundMethodAnnotation.argument().equalsIgnoreCase(subCommandName)
                        || containsAlias(notFoundMethodAnnotation.aliases(), subCommandName)
                    ) &&
                    (
                        notFoundMethodAnnotation.type().equals(Object.class)
                        || notFoundMethodAnnotation.type().equals(argumentException.getType())
                    ) &&
                    notFoundMethod.getParameterCount() == 1 &&
                    (
                        notFoundMethod.getParameters()[0].getType() == Sender.class
                        || notFoundMethod.getParameters()[0].isAnnotationPresent(CommandSender.class)
                    )
                ) {
                    PluginConsumer.process(
                        () -> {
                            Object argument = instance.parseSender(sender, notFoundMethod.getParameters()[0]);
                            notFoundMethod.invoke(this, argument);
                        },
                        notFoundException -> MeteorLogger.fetch().error(notFoundException, "Error executing not found method '" + notFoundMethodAnnotation.argument() + "'")
                    );
                    return;
                }
            }
        }
    }

    private boolean containsAlias(String[] aliases, String subCommandName) {
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(subCommandName)) {
                return true;
            }
        }
        return false;
    }

    private String[] sliceArgs(String[] args) {
        String[] sliced = new String[args.length - 1];
        System.arraycopy(args, 1, sliced, 0, args.length - 1);
        return sliced;
    }

}
