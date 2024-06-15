package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class ConsoleAction extends Action {
    public ConsoleAction() {
        super("[console]", "<console>", "console:");
    }

    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, List<Player> players) {
        if (players == null || players.isEmpty()) {
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    replace(parameter)
            );
            return;
        }

        boolean placeholders = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        for (Player player : players) {
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    placeholders ?
                            PlaceholderAPI.setPlaceholders(player, replace(parameter)) :
                            replace(parameter)
            );
        }
    }
}
