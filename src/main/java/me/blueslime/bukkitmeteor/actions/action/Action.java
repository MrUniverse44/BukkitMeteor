package me.blueslime.bukkitmeteor.actions.action;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Service;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class Action implements Service {

    private final Set<String> prefixes = new HashSet<>();

    private boolean stop = false;

    public Action(String prefix, String... extraPrefixes) {
        this.prefixes.addAll(Arrays.asList(extraPrefixes));
        this.prefixes.add(prefix);
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param players   players
     * @param parameter text
     */
    public void execute(BukkitMeteorPlugin plugin, String parameter, Player... players) {
        execute(plugin, parameter, Arrays.asList(players));
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param players   players
     * @param parameter text
     */
    public abstract void execute(BukkitMeteorPlugin plugin, String parameter, List<Player> players);

    public String replace(String parameter) {
        for (String prefix : prefixes) {
            parameter = parameter.replace(" " + prefix + " ", "").replace(" " + prefix, "").replace(prefix + " ", "").replace(prefix, "");
        }
        return parameter;
    }

    public boolean isAction(String parameter) {
        if (parameter == null) {
            return false;
        }
        String param = parameter.toLowerCase(Locale.ENGLISH);
        for (String prefix : prefixes) {
            if (param.startsWith(" " + prefix.toLowerCase(Locale.ENGLISH)) || param.startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prefixes of your actions
     * @return the set of your prefixes
     */
    public Set<String> getPrefixes() {
        return prefixes;
    }

    /**
     * Checks if a player can execute this action
     * @param plugin instance
     * @param player to check
     * @param parameter to check
     * @return execute value
     */
    public boolean canExecute(BukkitMeteorPlugin plugin, Player player, String parameter) {
        return true;
    }

    /**
     * Check if a player is stopping upcoming actions
     * @param plugin instance
     * @param parameter used
     * @param player of this check
     * @return result
     */
    public boolean isStoppingUpcomingActions(BukkitMeteorPlugin plugin, String parameter, Player player) {
        if (stop) {
            stop = false;
            return true;
        }
        return false;
    }

    public boolean requiresMainThread() {
        return false;
    }
}

