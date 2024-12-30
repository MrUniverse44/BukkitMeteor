package me.blueslime.bukkitmeteor.conditions.condition;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class Condition implements AdvancedModule {

    private final Set<String> prefixes = new HashSet<>();

    /**
     * Create a new condition
     * @param prefix main prefix
     * @param extraPrefixes optional prefixes
     */
    public Condition(String prefix, String... extraPrefixes) {
        prefixes.add(prefix.toLowerCase(Locale.ENGLISH));
        for (String extra : extraPrefixes) {
            prefixes.add(extra.toLowerCase(Locale.ENGLISH));
        }
    }

    public boolean execute(BukkitMeteorPlugin plugin, String parameter, Player player) {
        return execute(plugin, parameter, player, TextReplacer.builder());
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param player    player of this condition
     * @param replacer  if you have texts with custom variables, here you can replace variables in the parameter.
     * @return boolean true if the condition has been overpassed, false if not.
     */
    public abstract boolean execute(BukkitMeteorPlugin plugin, String parameter, Player player, TextReplacer replacer);

    /**
     * Remove the prefix from the entered parameter
     * @param parameter to remove the prefix
     * @return converted parameter
     */
    public String replace(String parameter) {
        if (parameter == null) return "";
        for (String prefix : prefixes) {
            parameter = parameter.replaceAll("\\b" + prefix + "\\b", "").trim();
        }
        return parameter;
    }

    /**
     * Check if this parameter is a condition
     * @param parameter to check
     * @return result
     */
    public boolean isCondition(String parameter) {
        if (parameter == null || parameter.isBlank()) return false;
        String param = parameter.toLowerCase(Locale.ENGLISH).trim();
        return prefixes.stream().anyMatch(param::startsWith);
    }

    public Set<String> getPrefixes() {
        return Collections.unmodifiableSet(prefixes);
    }
}
