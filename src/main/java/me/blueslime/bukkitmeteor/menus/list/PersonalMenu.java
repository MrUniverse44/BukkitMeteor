package me.blueslime.bukkitmeteor.menus.list;

import fr.mrmicky.fastinv.FastInv;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.utils.PluginUtil;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.utilitiesapi.item.ItemWrapper;
import me.blueslime.utilitiesapi.item.dynamic.executor.DynamicExecutor;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.blueslime.utilitiesapi.tools.PlaceholderParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class PersonalMenu extends FastInv {

    private final BukkitMeteorPlugin plugin;

    @SuppressWarnings("unused")
    public PersonalMenu(BukkitMeteorPlugin plugin, Player player, ConfigurationSection configuration) {
        this(plugin, player, configuration, TextReplacer.builder());
    }

    public PersonalMenu(BukkitMeteorPlugin plugin, Player player, ConfigurationSection configuration, TextReplacer replacer) {
        super(
            PluginUtil.getRows(configuration.getInt("menu-settings.rows", 54)),
            TextUtilities.colorize(
                configuration.getString("menu-settings.name", "menu-settings.name not found")
                    .replace("%page%", String.valueOf(1))
                    .replace("<page>", String.valueOf(1))
            )
        );
        this.plugin = plugin;

        ConfigurationSection extra = configuration.getConfigurationSection("items");

        if (extra == null) {
            return;
        }

        replacer = replacer
            .replace("<player>", player.getName())
            .replace("<player_name>", player.getName())
            .replace("<heart>", "❤");

        Actions actions = Implements.fetch(Actions.class);

        for (String key : extra.getKeys(false)) {
            String path = "items." + key;

            ItemWrapper wrapper = ItemWrapper.fromData(configuration, path)
                .setDynamic(getItemExecutor(replacer));

            if (configuration.contains(path + ".slot")) {
                TextReplacer finalReplacer = replacer;
                setItem(
                        configuration.getInt(path + ".slot", 0),
                        wrapper.getDynamicItem(player).getItem(),
                        event -> {
                            List<String> list = configuration.getStringList(path + ".actions");

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
                            List<String> list = configuration.getStringList(path + ".actions");

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
