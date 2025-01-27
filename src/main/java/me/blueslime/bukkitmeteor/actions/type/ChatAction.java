package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        List<String> playerNames = null;

        if (parameter.contains("%for:")) {
            // well... this message will be sent for other(s) players.
            playerNames = new ArrayList<>();

            String regex = "%for:(.*?)%";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(parameter);

            while (matcher.find()) {
                String userName = matcher.group(1);  // El contenido entre %for: y %
                playerNames.add(userName);
            }
        }

        if (playerNames == null) {

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
            return;
        }

        for (String userName : playerNames) {
            Player player = plugin.getServer().getPlayer(userName);

            if (player == null) {
                continue;
            }

            boolean placeholders = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

            parameter = replace(parameter).replace("\\n", "\n");

            String message = parameter;

            if (placeholders) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = message.replace("\\n", "\n");

            player.chat(message);
        }
    }
}
