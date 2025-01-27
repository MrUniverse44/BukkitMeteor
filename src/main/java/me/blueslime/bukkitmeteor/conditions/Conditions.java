package me.blueslime.bukkitmeteor.conditions;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.conditions.condition.Condition;
import me.blueslime.bukkitmeteor.conditions.type.PlaceholderCondition;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Conditions implements AdvancedModule {

    private final List<Condition> externalConditions = new CopyOnWriteArrayList<>();
    private final List<Condition> conditions = new CopyOnWriteArrayList<>();
    private final BukkitMeteorPlugin plugin;

    public Conditions(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        registerInternalCondition(new PlaceholderCondition());
        Implements.register(this);
    }

    public void registerInternalCondition(Condition... conditions) {
        Collections.addAll(this.conditions, conditions);
    }

    public void registerCondition(Condition... conditions) {
        Collections.addAll(this.externalConditions, conditions);
    }

    public List<Condition> getCondition() {
        return Collections.unmodifiableList(conditions);
    }

    public List<Condition> getExternalCondition() {
        return Collections.unmodifiableList(externalConditions);
    }

    public boolean execute(List<String> actions, Player player) {
        if (actions == null || actions.isEmpty()) {
            return true;
        }
        return execute(actions, player, TextReplacer.builder());
    }

    public boolean execute(List<String> actions, Player player, TextReplacer replacer) {
        if (actions == null || actions.isEmpty()) {
            return true;
        }

        for (String action : actions) {
            if (!fetch(getAllConditions(), player, action, replacer)) {
                return false;
            }
        }
        return true;
    }

    private boolean fetch(List<Condition> list, Player player, String param, TextReplacer replacer) {
        return list.stream()
            .filter(condition -> condition.isCondition(param))
            .findFirst()
            .map(condition -> condition.execute(plugin, param, player, replacer))
            .orElseGet(() -> {
                plugin.getLogger().warning("'" + param + "' does not match any condition. Returning false...");
                return false;
            });
    }

    private List<Condition> getAllConditions() {
        List<Condition> combined = new CopyOnWriteArrayList<>(externalConditions);
        combined.addAll(conditions);
        return combined;
    }

    @Register
    public Conditions provideConditions() {
        return this;
    }
}