package me.blueslime.bukkitmeteor.commands;

import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.utilitiesapi.commands.sender.Sender;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class Command implements CommandExecutor {
    private String command;

    public Command(String command) {
        this.command = command;
    }

    public void register(JavaPlugin plugin) {
        if (this.command != null) {
            PluginCommand command = plugin.getCommand(this.command);

            if (command != null) {
                command.setExecutor(this);
            }
        }
    }

    public Command setCommand(String command) {
        this.command = command;
        return this;
    }

    public abstract void execute(Sender sender, String command, String[] arguments);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        execute(Sender.build(sender), this.command, args);
        return true;
    }

    public String getCommand() {
        return command;
    }

    public static String colorize(String text) {
        return TextUtilities.convert(text);
    }
}
