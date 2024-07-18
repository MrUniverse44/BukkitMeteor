package me.blueslime.bukkitmeteor.actions;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.bukkitmeteor.actions.type.*;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Actions implements Module {
    private final List<Action> externalActions = new ReturnableArrayList<Action>();
    private final List<Action> action = new ReturnableArrayList<Action>();
    private final BukkitMeteorPlugin plugin;

    public Actions(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        registerInternalAction(
            new MessageAction(),
            new ActionBarAction(),
            new TitlesAction(),
            new ConsoleAction(),
            new ChatAction(),
            new CloseMenuAction(),
            new InventoryAction(),
            new PlaySoundAction(),
            new MenuAction()
        );
        Implements.register(this);
    }

    /**
     * Register actions to the plugin
     * These actions will not be refreshed in a reload event.
     * @param actions to register
     */
    public void registerInternalAction(Action... actions) {
        action.addAll(Arrays.asList(actions));
    }

    /**
     * Register actions to the plugin
     * @param actions to register
     */
    public void registerAction(Action... actions) {
        externalActions.addAll(Arrays.asList(actions));
    }

    /**
     * Get the list of internal actions
     * @return ArrayList
     */
    public List<Action> getActions() {
        return action;
    }

    /**
     * Get the list of external actions
     * @return ArrayList
     */
    public List<Action> getExternalActions() {
        return externalActions;
    }

    public void execute(List<String> actions) {
        execute(actions, null);
    }

    public void execute(List<String> actions, Player player) {
        List<Action> entireList = new ReturnableArrayList<Action>();

        entireList.addAll(externalActions);
        entireList.addAll(action);

        for (String param : actions) {
            if (fetch(entireList, player, param)) {
                break;
            }
        }
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public void execute(List<String> actions, Player player, TextReplacer replacer) {
        List<Action> entireList = new ReturnableArrayList<Action>();

        entireList.addAll(externalActions);
        entireList.addAll(action);

        for (String param : actions) {
            if (fetch(entireList, player, replacer.apply(param))) {
                break;
            }
        }
    }

    private boolean fetch(List<Action> list, Player player, String param) {
        if (player == null) {
            return false;
        }
        for (Action action : list) {
            if (action.isAction(param)) {
                if (action.canExecute(player)) {
                    action.execute(plugin, param, player);
                }
                return action.isStoppingUpcomingActions();
            }
        }
        plugin.getLogger().info("'" + param + "' don't have an action, please see actions with /<command> actions");
        return false;
    }

    @Register
    public Actions provideActions() {
        return this;
    }
}

