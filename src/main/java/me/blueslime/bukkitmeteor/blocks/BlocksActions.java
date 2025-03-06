package me.blueslime.bukkitmeteor.blocks;


import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.blocks.action.BlockAction;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.tasks.TaskSettings;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.bukkitmeteor.blocks.types.*;
import me.blueslime.bukkitmeteor.tasks.TaskType;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlocksActions implements Service {

    private final List<BlockAction> externalActions = new ArrayList<>();
    private final List<BlockAction> action = new ArrayList<>();

    private final BukkitMeteorPlugin plugin;

    public BlocksActions(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;

        registerInternalAction(
            new SetBlockAction(),
            new SpawnParticleBlockAction(),
            new FireworkBlockAction(),
            new SpawnEffectBlockAction()
        );

        registerImpl(
            BlocksActions.class,
            this,
            true
        );
    }

    /**
     * Register actions to the plugin
     * These actions will not be refreshed in a reload event.
     *
     * @param actions to register
     */
    public void registerInternalAction(BlockAction... actions) {
        action.addAll(Arrays.asList(actions));
    }

    /**
     * Register actions to the plugin
     *
     * @param actions to register
     */
    public void registerAction(BlockAction... actions) {
        externalActions.addAll(Arrays.asList(actions));
    }

    /**
     * Get the list of internal actions
     *
     * @return ArrayList
     */
    public List<BlockAction> getActions() {
        return action;
    }

    /**
     * Get the list of external actions
     *
     * @return ArrayList
     */
    public List<BlockAction> getExternalActions() {
        return externalActions;
    }

    public void execute(List<String> actions, World world) {
        List<BlockAction> entireList = new ArrayList<>();

        entireList.addAll(externalActions);
        entireList.addAll(action);

        if (!Bukkit.isPrimaryThread()) {
            getScheduler().execute(
                () -> execute(actions, world),
                TaskSettings.create()
                    .setType(TaskType.NORMAL)
                    .setAsync(false)
            );
            return;
        }

        for (String param : actions) {
            fetch(entireList, world, param);
        }
    }

    public void execute(String actionParameter, World location) {
        List<BlockAction> entireList = new ArrayList<>();

        entireList.addAll(externalActions);
        entireList.addAll(action);

        if (!Bukkit.isPrimaryThread()) {
            getScheduler().execute(
                    () -> execute(actionParameter, location),
                    TaskSettings.create()
                            .setType(TaskType.NORMAL)
                            .setAsync(false)
            );
            return;
        }

        fetch(entireList, location, actionParameter);
    }

    public void execute(List<String> actions, World location, TextReplacer replacer) {
        List<BlockAction> entireList = new ArrayList<>();

        entireList.addAll(externalActions);
        entireList.addAll(action);

        if (!Bukkit.isPrimaryThread()) {
            getScheduler().execute(
                () -> execute(actions, location, replacer),
                TaskSettings.create()
                    .setType(TaskType.NORMAL)
                    .setAsync(false)
            );
            return;
        }

        for (String param : actions) {
            fetch(entireList, location, replacer.apply(param));
        }
    }

    private void fetch(List<BlockAction> list, World world, String param) {
        if (world == null) {
            return;
        }
        for (BlockAction action : list) {
            if (action.isAction(param)) {
                action.execute(plugin, action.replace(param), world);
                return;
            }
        }
        plugin.getLogs().info("'" + param + "' don't have an action, please see actions with /<command> actions");
    }
}

