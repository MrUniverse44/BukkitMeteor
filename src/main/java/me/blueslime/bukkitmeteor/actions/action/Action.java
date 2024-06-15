package me.blueslime.bukkitmeteor.actions.action;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class Action {
    private final List<String> prefixes = new ArrayList<>();

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

    public List<String> getPrefixes() {
        return prefixes;
    }

    public boolean canExecute(Player player) {
        return true;
    }
}

