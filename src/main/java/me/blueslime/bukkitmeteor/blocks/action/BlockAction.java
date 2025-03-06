package me.blueslime.bukkitmeteor.blocks.action;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Service;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class BlockAction implements Service {

    private final Set<String> prefixes = new HashSet<>();

    public BlockAction(String prefix, String... extraPrefixes) {
        this.prefixes.addAll(Arrays.asList(extraPrefixes));
        this.prefixes.add(prefix);
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     */
    public abstract void execute(BukkitMeteorPlugin plugin, String parameter, World world);

    public String replace(String parameter) {
        for (String prefix : prefixes) {
            parameter = parameter.replace(" " + prefix + " ", "").replace(" " + prefix, "").replace(prefix + " ", "").replace(prefix, "");
        }
        return parameter;
    }

    public Location getLocation(String parameter, World world) {
        String[] parts = parameter.replace(" ", "").split(",");

        if (parts.length < 4) {
            return null;
        }

        return new Location(
                world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

    public String getAction(String parameter) {
        String[] parts = parameter.split(",");
        return parts[0].replace(" ", "");
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
}

