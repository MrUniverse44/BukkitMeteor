package me.blueslime.bukkitmeteor.menus.list;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.menus.Menu;
import me.blueslime.menuhandlerapi.inventory.MenuType;
import me.blueslime.menuhandlerapi.item.action.MenuItemAction;
import me.blueslime.menuhandlerapi.item.list.WrapperMenuItem;
import me.blueslime.menuhandlerapi.inventory.MenuInventory;
import me.blueslime.menuhandlerapi.inventory.MenuInventoryBuilder;

import me.blueslime.utilitiesapi.item.ItemWrapper;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class DefaultMenu extends Menu {

    private final Actions actions = Implements.fetch(Actions.class);

    public DefaultMenu(BukkitMeteorPlugin plugin, FileConfiguration configuration, File file) {
        super(plugin, configuration, file);
    }

    public MenuInventory fetchPlayerInventory(TextReplacer replacer) {
        FileConfiguration configuration = getConfiguration();

        MenuInventoryBuilder builder = MenuInventoryBuilder.builder(
                getUniqueId(),
                TextUtilities.colorize(
                        replacer.apply(
                            configuration.getString(
                                "inventory.title",
                                "&8Menu"
                            )
                        )
                ),
                configuration.getInt("inventory.rows", 1)
        );

        MenuInventory inventory = builder.setCanIntroduce(false).type(MenuType.UPDATED_STATIC_INVENTORY).build();

        ConfigurationSection section = getConfiguration().getConfigurationSection("items");

        if (section != null) {

            for (String item : section.getKeys(false)) {

                String path = "items." + item;

                if (!configuration.getBoolean(path + ".enabled", true)) {
                    continue;
                }

                ItemWrapper wrapper = ItemWrapper.fromData(
                        configuration,
                        path
                ).setDynamic(
                        getItemExecutor(replacer)
                );

                MenuItemAction itemAction = new MenuItemAction(
                        event -> {
                            List<String> list = configuration.getStringList(path + ".actions");
                            Player player = (Player) event.getWhoClicked();

                            if (list.isEmpty()) {
                                return;
                            }

                            actions.execute(list, player, replacer);
                        }
                );

                if (configuration.contains(path + ".slot")) {
                    inventory.addItem(
                            WrapperMenuItem.builder(
                                    item,
                                    configuration.getInt(path + ".slot")
                            ).item(
                                    wrapper
                            ).action(
                                    itemAction
                            ).cancelClick(true).build()
                    );
                }

                if (configuration.contains(path + ".slots")) {
                    for (int currentSlot : configuration.getIntegerList(path + ".slots")) {
                        inventory.addItem(
                                WrapperMenuItem.builder(
                                        item,
                                        currentSlot
                                ).item(
                                        wrapper
                                ).action(
                                        itemAction
                                ).cancelClick(true).build()
                        );
                    }
                }
            }
        }

        return inventory;
    }

    @Override
    public void openMenu(Player player, TextReplacer replacer) {
        fetchPlayerInventory(replacer).openInventory(player);
    }
}


