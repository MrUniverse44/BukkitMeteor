package me.blueslime.bukkitmeteor.menus.list;

import me.blueslime.utilitiesapi.item.dynamic.executor.DynamicExecutor;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.utilitiesapi.tools.PlaceholderParser;
import me.blueslime.bukkitmeteor.conditions.Conditions;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.bukkitmeteor.utils.PluginUtil;
import me.blueslime.utilitiesapi.item.ItemWrapper;
import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.menus.ItemMenu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.mrmicky.fastinv.FastInv;

import java.util.List;

public class PersonalMenu extends FastInv {

    private final BukkitMeteorPlugin plugin;
    private boolean canOpenMenu = true;

    public PersonalMenu(Player player, ConfigurationSection configuration) {
        this(
            Implements.fetch(BukkitMeteorPlugin.class),
            player,
            configuration
        );
    }

    @SuppressWarnings("unused")
    public PersonalMenu(
        BukkitMeteorPlugin plugin,
        Player player,
        ConfigurationSection configuration
    ) {
        this(
            plugin,
            player,
            configuration,
            TextReplacer.builder()
        );
    }

    private ItemMenu checkConditions(Conditions conditions, ConfigurationSection configuration, Player player, ItemWrapper original, String path) {
        if (!configuration.contains(path + ".display-condition")) {
            return new ItemMenu(original, path);
        }
        List<String> displayConditionList = configuration.getStringList(path + ".display-condition");

        if (conditions != null) {
            if (!conditions.execute(displayConditionList, player)) {
                if (!configuration.contains(path + ".without-conditions")) {
                    return new ItemMenu(null, path);
                }
                return checkConditions(
                    conditions,
                    configuration,
                    player,
                    ItemWrapper.fromData(configuration, path + ".without-conditions"),
                    path + ".without-conditions"
                );
            }
        }
        return new ItemMenu(null, path);
    }

    public PersonalMenu(
        BukkitMeteorPlugin plugin,
        Player player,
        ConfigurationSection configuration,
        TextReplacer replacer
    ) {
        super(
            PluginUtil.getRows(configuration.getInt("menu-settings.rows", 54)),
            TextUtilities.colorize(
                configuration.getString("menu-settings.name", "menu-settings.name not found")
                    .replace("%page%", String.valueOf(1))
                    .replace("<page>", String.valueOf(1))
            )
        );

        List<String> conditionList = configuration.getStringList("menu-settings.open-conditions");

        Conditions conditions = Implements.fetch(Conditions.class);

        if (conditions != null) {
            if (!conditions.execute(conditionList, player)) {
                this.plugin = plugin;
                this.canOpenMenu = false;
                return;
            }
        }

        this.plugin = plugin;

        ConfigurationSection extra = configuration.getConfigurationSection("items");

        if (extra == null) {
            // find items in the same configuration section.
            extra = configuration;
        }

        replacer = replacer
            .replace("<player>", player.getName())
            .replace("<player_name>", player.getName())
            .replace("<heart>", "❤");

        Actions actions = Implements.fetch(Actions.class);

        for (String key : extra.getKeys(false)) {
            String path = "items." + key;

            ItemWrapper wrapper = ItemWrapper
                .fromData(configuration, path)
                .setDynamic(getItemExecutor(replacer));

            /* We check infinite wrapper checks */
            ItemMenu menuItem = checkConditions(conditions, configuration, player, wrapper, path);

            if (!menuItem.isPresent()) {
                return;
            }

            wrapper = menuItem.getWrapper();

            if (wrapper == null) {
                /* Not need to continue because the wrapper don't exist */
                continue;
            }

            /* Gets the new menu item path */
            path = menuItem.getPath();

            final String finalPath = path;

            if (configuration.contains(path + ".slot")) {
                TextReplacer finalReplacer = replacer;
                setItem(
                    configuration.getInt(path + ".slot", 0),
                    wrapper.getDynamicItem(player).getItem(),
                    event -> {
                        event.setCancelled(true);
                        if (configuration.contains(finalPath + ".actions-condition")) {
                            List<String> actionConditionList = configuration.getStringList(finalPath + ".actions-condition");

                            if (conditions != null) {
                                if (!conditions.execute(actionConditionList, player)) {
                                    List<String> list = configuration.getStringList(finalPath + ".failed-actions-conditions");

                                    if (list.isEmpty()) {
                                        return;
                                    }

                                    actions.execute(
                                        list,
                                        player,
                                        finalReplacer
                                    );
                                    return;
                                }
                            }
                        }

                        List<String> list = configuration.getStringList(finalPath + ".actions");

                        if (list.isEmpty()) {
                            return;
                        }

                        actions.execute(
                            list,
                            player,
                            finalReplacer
                        );
                    }
                );
            }

            if (configuration.contains(path + ".slots")) {
                for (int currentSlot : configuration.getIntegerList(path + ".slots")) {
                    TextReplacer finalReplacer1 = replacer;
                    setItem(
                        currentSlot,
                        wrapper.getDynamicItem(player).getItem(),
                        event -> {
                            event.setCancelled(true);
                            if (configuration.contains(finalPath + ".actions-condition")) {
                                List<String> actionConditionList = configuration.getStringList(finalPath + ".actions-condition");

                                if (conditions != null) {
                                    if (!conditions.execute(actionConditionList, player)) {
                                        return;
                                    }
                                }
                            }

                            List<String> list = configuration.getStringList(finalPath + ".actions");

                            if (list.isEmpty()) {
                                return;
                            }

                            actions.execute(
                                list,
                                player,
                                finalReplacer1
                            );
                        }
                    );
                }
            }
        }
    }

    @Override
    public void open(Player player) {
        if (!canOpenMenu) {
            return;
        }
        super.open(player);
    }

    public DynamicExecutor getItemExecutor(TextReplacer replacer) {
        return (item) -> {
            ItemWrapper original = item.getWrapper();
            ItemWrapper wrapper = original.clone();

            Player player = item.getPlayer();

            replacer.replace("<player_name>", player.getName()).replace("<heart>", "❤");

            if (plugin.isPluginEnabled("PlaceholderAPI")) {
                wrapper.setName(
                    original.getName() != null ?
                        PlaceholderParser.parse(player, replacer.apply(original.getName())) :
                        original.getName()
                );

                wrapper.setLore(
                    new ReturnableArrayList<>(original.getLore()).replace(
                        line -> PlaceholderParser.parse(
                            player,
                            replacer.apply(line)
                        )
                    )
                );
            } else {
                wrapper.setName(
                    original.getName() != null ?
                        replacer.apply(original.getName()) :
                        original.getName()
                );

                wrapper.setLore(
                    new ReturnableArrayList<>(original.getLore()).replace(
                        replacer::apply
                    )
                );
            }
            return wrapper;
        };
    }
}
