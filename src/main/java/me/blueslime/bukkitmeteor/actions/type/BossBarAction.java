package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.messagehandler.types.bossbar.BossBarHandler;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class BossBarAction extends Action {
    public BossBarAction() {
        super("[bossbar]", "<bossbar>", "bossbar:");
    }



    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param players   players
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, TextReplacer replacer, List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        parameter = replacer.apply(parameter);

        boolean placeholders = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        parameter = replace(parameter);

        String[] split = parameter.split(";");

        String message;
        float percentage = split.length >= 2 ? Float.parseFloat(split[1]) : 1;

        BossBarHandler BOSS_BAR = BossBarHandler.getInstance();

        for (Player player : players) {
            message = split[0];

            if (placeholders) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = message.replace("\\n", "\n");

            BOSS_BAR.send(player, TextUtilities.colorize(message), percentage);
        }
    }

    @Override
    public boolean requiresMainThread() {
        return true;
    }
}
