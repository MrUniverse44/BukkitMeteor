package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.messagehandler.types.titles.TitlesHandler;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class TitlesAction extends Action {
    public TitlesAction() {
        super("[titles]", "<titles>", "titles:");
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param players   players
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        boolean placeholders = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        parameter = replace(parameter).replace("\\n", "<subtitle>");

        TitlesHandler MESSAGES = TitlesHandler.getInstance();

        for (Player player : players) {

            String message = parameter;

            if (placeholders) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = message.replace("\\n", "\n");

            MESSAGES.send(player, TextUtilities.colorize(message));
        }
    }
}

