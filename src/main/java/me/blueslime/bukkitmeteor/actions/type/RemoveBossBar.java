package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.messagehandler.types.bossbar.BossBarHandler;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveBossBar extends Action {
    public RemoveBossBar() {
        super("[clear bossbar]", "<clear bossbar>", "clear bossbar:");
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
        BossBarHandler handler = BossBarHandler.getInstance();
        for (Player player : players) {
            handler.remove(player);
        }
    }

    @Override
    public boolean requiresMainThread() {
        return true;
    }
}
