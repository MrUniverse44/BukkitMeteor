package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;

import java.util.List;

public class LogDebugAction extends Action {

    public LogDebugAction() {
        super("<log-debug>", "[log-debug]", "log-debug:");
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
        parameter = replacer.apply(parameter);
        parameter = replace(parameter);
        getLogs().debug(parameter);
    }
}
