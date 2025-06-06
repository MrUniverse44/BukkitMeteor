package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.messagehandler.types.actionbar.ActionBarHandler;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionBarAction extends Action {
    public ActionBarAction() {
        super("[actionbar]", "<actionbar>", "actionbar:");
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param replacer  of this check
     * @param players   players
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, TextReplacer replacer, List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        parameter = replacer.apply(parameter);

        boolean placeholders = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        parameter = replace(parameter).replace("\\n", "\n");

        ActionBarHandler ACTION_BAR = ActionBarHandler.getInstance();

        for (Player player : players) {

            String message = parameter;

            if (placeholders) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = message.replace("\\n", "\n");

            ACTION_BAR.send(player, TextUtilities.colorize(message));
        }
    }

    @Override
    public boolean requiresMainThread() {
        return true;
    }
}


