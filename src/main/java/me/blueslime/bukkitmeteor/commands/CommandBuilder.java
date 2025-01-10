package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.commands.advanced.CommandSender;
import me.blueslime.bukkitmeteor.commands.advanced.NotNullArgument;
import me.blueslime.bukkitmeteor.commands.advanced.NullableArgument;
import me.blueslime.bukkitmeteor.commands.function.CommandFunction;
import me.blueslime.bukkitmeteor.commands.issues.CommandArgumentNotFoundException;
import me.blueslime.bukkitmeteor.commands.issues.CommandWrongSenderException;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.utilitiesapi.commands.sender.Sender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class CommandBuilder implements AdvancedModule {

    private final Map<Class<?>, CommandFunction<Sender, ?>> senderConverter = new HashMap<>();
    private final Map<Class<?>, CommandFunction<String, ?>> typeConverters = new HashMap<>();

    public CommandBuilder() {

        registerConverter(int.class, Integer::parseInt);
        registerConverter(double.class, Double::parseDouble);
        registerConverter(boolean.class, Boolean::parseBoolean);
        registerConverter(String.class, s -> s);

        // Register Bukkit-specific converters
        registerConverter(Player.class, name -> {
            Player player = Bukkit.getPlayerExact(name);
            if (player == null) {
                throw new CommandArgumentNotFoundException(Player.class);
            }
            return player;
        });

        registerConverter(World.class, name -> {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                throw new CommandArgumentNotFoundException(World.class);
            }
            return world;
        });

        Implements.register(this);
    }

    /**
     * Register commands for the plugin
     * @param commands to register
     */
    public CommandBuilder register(InjectedCommand... commands) {
        for (InjectedCommand command : commands) {
            command.register();
        }
        return this;
    }

    /**
     * Register commands for the plugin
     * @param commands to register
     */
    @SafeVarargs
    public final CommandBuilder register(Class<? extends InjectedCommand>... commands) {
        for (Class<? extends InjectedCommand> command : commands) {
            InjectedCommand injectedCommand = createInstance(command);
            injectedCommand.register();
        }
        return this;
    }

    public <T> CommandBuilder registerConverter(Class<T> type, CommandFunction<String, T> converter) {
        typeConverters.put(type, converter);
        return this;
    }

    public <T> CommandBuilder registerSenderConverter(Class<T> type, CommandFunction<Sender, T> converter) {
        senderConverter.put(type, converter);
        return this;
    }

    public Object[] parseArguments(Sender sender, String[] args, Method method) throws CommandArgumentNotFoundException, CommandWrongSenderException {
        Parameter[] parameters = method.getParameters();

        if (parameters.length != args.length) {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + parameters.length + ", got " + args.length);
        }

        Object[] parsedArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            Class<?> paramType = parameters[i].getType();
            Parameter param = parameters[i];

            if (paramType.equals(sender.getClass())) {
                parsedArgs[i] = sender;
            }

            if (parameters[i].isAnnotationPresent(CommandSender.class)) {
                if (paramType.equals(org.bukkit.command.CommandSender.class)) {
                    parsedArgs[i] = sender.toCommandSender();
                }
                if (paramType.equals(Player.class)) {
                    if (sender.isPlayer()) {
                        parsedArgs[i] = sender.toPlayer();
                    }
                    throw new CommandWrongSenderException(true);
                } else if (paramType.equals(ConsoleCommandSender.class)) {
                    if (sender.isConsole()) {
                        parsedArgs[i] = sender.toConsole();
                    }
                    throw new CommandWrongSenderException(false);
                }
                throw new CommandWrongSenderException();
            }
            CommandFunction<String, ?> converter = typeConverters.get(paramType);

            if (converter == null) {
                throw new IllegalArgumentException("No converter registered for type: " + paramType.getName());
            }

            try {
                parsedArgs[i] = converter.apply(args[i]);
            } catch (Exception e) {
                if (e instanceof CommandArgumentNotFoundException exception) {
                    if (param.isAnnotationPresent(NotNullArgument.class)) {
                        throw exception;
                    } else if (param.isAnnotationPresent(NullableArgument.class)) {
                        parsedArgs[i] = null;
                    }
                } else {
                    throw new CommandArgumentNotFoundException(paramType);
                }
            }
        }

        return parsedArgs;
    }

    public Object parseSender(Sender sender, Parameter parameter) throws CommandArgumentNotFoundException, CommandWrongSenderException {
        Object result;

        Class<?> paramType = parameter.getType();

        if (paramType.equals(sender.getClass())) {
            return sender;
        }

        if (parameter.isAnnotationPresent(CommandSender.class)) {
            if (paramType.equals(org.bukkit.command.CommandSender.class)) {
                return sender.toCommandSender();
            }
            if (paramType.equals(Player.class)) {
                if (sender.isPlayer()) {
                    return sender.toPlayer();
                }
                throw new CommandWrongSenderException(true);
            } else if (paramType.equals(ConsoleCommandSender.class)) {
                if (sender.isConsole()) {
                    return sender.toConsole();
                }
                throw new CommandWrongSenderException(false);
            }
            throw new CommandWrongSenderException();
        }
        CommandFunction<Sender, ?> converter = senderConverter.get(paramType);

        if (converter == null) {
            throw new IllegalArgumentException("No converter registered for type: " + paramType.getName());
        }

        result = converter.apply(sender);

        return result;
    }

    @Register
    public CommandBuilder provideInstance() {
        return this;
    }

}
