package me.blueslime.bukkitmeteor.actions;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.bukkitmeteor.actions.type.*;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.utils.skin.SkinUtil;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.*;

public class Actions implements AdvancedModule {
    private final List<Action> internalActions = new CopyOnWriteArrayList<>();
    private final List<Action> externalActions = new CopyOnWriteArrayList<>();
    private final BukkitMeteorPlugin plugin;
    private final ExecutorService executor;

    public Actions(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;

        // Initialize thread pool with a fixed size
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        // Register default internal actions
        registerInternalAction(
            new MessageAction(),
            new ActionBarAction(),
            new TitlesAction(),
            new ConsoleAction(),
            new ChatAction(),
            new CloseMenuAction(),
            new InventoryAction(),
            new PlaySoundAction(),
            new MenuAction(),
            new RemoveBossBar(),
            new BossBarAction(),
            new LogDebugAction(),
            new LogErrorAction(),
            new LogInfoAction()
        );

        Implements.register(this);
    }

    /**
     * Register internal actions (not affected by reload)
     * @param actions to register
     */
    public void registerInternalAction(Action... actions) {
        Collections.addAll(this.internalActions, actions);
    }

    /**
     * Register external actions
     * @param actions to register
     */
    public void registerAction(Action... actions) {
        Collections.addAll(this.externalActions, actions);
    }

    /**
     * Get the list of internal actions
     * @return List<Action>
     */
    public List<Action> getActions() {
        return Collections.unmodifiableList(this.internalActions);
    }

    /**
     * Get the list of external actions
     * @return List<Action>
     */
    public List<Action> getExternalActions() {
        return Collections.unmodifiableList(this.externalActions);
    }

    public void execute(List<String> actions, Player player) {
        if (player == null || actions == null || actions.isEmpty()) {
            return;
        }

        TextReplacer replacer = TextReplacer.builder();

        if (
            actions.stream()
                .anyMatch(line -> line.toLowerCase(Locale.ENGLISH).contains("<player-head"))
        ) {
            SkinUtil.getPlayerHeadAsync(player).thenAcceptAsync(
                head -> new BukkitRunnable() {
                    @Override
                    public void run() {
                        replacer.addReplacements(
                            SkinUtil.convertToLines(head)
                        );
                        List<Action> combinedActions = getCombinedActions();
                        for (String param : actions) {
                            if (executeAction(combinedActions, player, param, replacer)) {
                                break;
                            }
                        }
                    }
                }.runTask(fetch(BukkitMeteorPlugin.class))
            ).exceptionally(
                ex -> {
                    getLogs().error(ex, "Can't fetch player head");
                    replacer.replace(
                        "<player-head-0>", ""
                    );
                    replacer.replace(
                        "<player-head-1>", ""
                    );
                    replacer.replace(
                        "<player-head-2>", ""
                    );
                    replacer.replace(
                        "<player-head-3>", ""
                    );
                    replacer.replace(
                        "<player-head-4>", ""
                    );
                    replacer.replace(
                        "<player-head-5>", ""
                    );
                    replacer.replace(
                        "<player-head-6>", ""
                    );
                    replacer.replace(
                        "<player-head-7>", ""
                    );
                    replacer.replace(
                        "<player-head-8>", ""
                    );
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            List<Action> combinedActions = getCombinedActions();
                            for (String param : actions) {
                                if (executeAction(combinedActions, player, param, replacer)) {
                                    break;
                                }
                            }
                        }
                    }.runTask(fetch(BukkitMeteorPlugin.class));
                    return null;
                }
            );
        } else {
            List<Action> combinedActions = getCombinedActions();
            for (String param : actions) {
                if (executeAction(combinedActions, player, param, replacer)) {
                    break;
                }
            }
        }
    }

    public void execute(List<String> actions, Player player, TextReplacer replacer) {
        if (player == null || actions == null || actions.isEmpty() || replacer == null) {
            return;
        }

        executor.submit(() -> {
            if (
                actions.stream().anyMatch(line -> line.contains("<player-head"))
            ) {
                SkinUtil.getPlayerHeadAsync(player).thenAcceptAsync(
                    head -> new BukkitRunnable() {
                        @Override
                        public void run() {
                            replacer.addReplacements(
                                    SkinUtil.convertToLines(head)
                            );
                            List<Action> combinedActions = getCombinedActions();
                            for (String param : actions) {
                                if (executeAction(combinedActions, player, param, replacer)) {
                                    break;
                                }
                            }
                        }
                    }.runTask(fetch(BukkitMeteorPlugin.class))
                ).exceptionally(
                        ex -> {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    List<Action> combinedActions = getCombinedActions();
                                    for (String param : actions) {
                                        if (executeAction(combinedActions, player, param, replacer)) {
                                            break;
                                        }
                                    }
                                }
                            }.runTask(fetch(BukkitMeteorPlugin.class));
                            return null;
                        }
                );
            } else {
                List<Action> combinedActions = getCombinedActions();
                for (String param : actions) {
                    if (executeAction(combinedActions, player, param, replacer)) {
                        break;
                    }
                }
            }
        });
    }

    private boolean executeAction(List<Action> actions, Player player, String param, TextReplacer replacer) {
        for (Action action : actions) {
            if (action.isAction(param) && action.canExecute(plugin, player, param, replacer)) {
                if (action.requiresMainThread()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> action.execute(plugin, param, replacer, player));
                } else {
                    action.execute(plugin, param, replacer, player);
                }
                return action.isStoppingUpcomingActions(plugin, param, player, replacer);
            }
        }

        plugin.getLogger().info(() -> "'" + param + "' doesn't match any action. Use /<command> actions to see available actions.");
        return false;
    }

    private List<Action> getCombinedActions() {
        if (externalActions.isEmpty()) {
            return internalActions;
        }
        if (internalActions.isEmpty()) {
            return externalActions;
        }
        List<Action> list = new CopyOnWriteArrayList<>();
        list.addAll(internalActions);
        list.addAll(externalActions);
        return list;
    }

    @Register
    public Actions provideActions() {
        return this;
    }
}

