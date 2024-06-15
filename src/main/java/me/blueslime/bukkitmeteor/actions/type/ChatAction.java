package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatAction extends Action {
    public ChatAction() {
        super("chat:", "[chat]", "<chat>");
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

        parameter = replace(parameter).replace("\\n", "\n");

        for (Player player : players) {

            String message = parameter;

            if (placeholders) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = message.replace("\\n", "\n");

            player.chat(message);
        }
    }
}
