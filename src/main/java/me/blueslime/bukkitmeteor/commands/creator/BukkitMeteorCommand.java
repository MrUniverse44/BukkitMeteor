package me.blueslime.bukkitmeteor.commands.creator;

import me.blueslime.bukkitmeteor.commands.creator.interfaces.ArgumentHandler;
import me.blueslime.bukkitmeteor.commands.creator.interfaces.TabCompletable;
import me.blueslime.bukkitmeteor.commands.sender.Sender;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BukkitMeteorCommand extends CommandExecutable {

    private final CommandCreator creator;

    public BukkitMeteorCommand(CommandCreator creator) {
        super(creator.getCommandName(), new ArrayList<>(creator.getAliases()));
        this.creator = creator;
    }

    /**
     * Command Execution
     *
     * @param sender    Source object which is executing this command
     * @param command   The alias of the command used
     * @param arguments All arguments passed to the command, split via ' '
     */
    @Override
    public void executeCommand(Sender sender, String command, String[] arguments) {
        for (ArgumentHandler handler : creator.getArgumentHandlers()) {
            if (handler.handle(sender, arguments)) {
                return;
            }
        }
    }

    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (!creator.hasTabCompletable()) {
            return Collections.emptyList();
        }
        Sender s = Sender.build(sender);
        // Preguntamos a cada handler; el primero que devuelva lista no vacía la usa
        for (ArgumentHandler handler : creator.getArgumentHandlers()) {
            if (handler instanceof TabCompletable) {
                List<String> suggestions = ((TabCompletable) handler).complete(s, args);
                if (!suggestions.isEmpty()) {
                    return suggestions;
                }
            }
        }
        // fallback al comportamiento de Bukkit
        return super.tabComplete(sender, alias, args);
    }
}
