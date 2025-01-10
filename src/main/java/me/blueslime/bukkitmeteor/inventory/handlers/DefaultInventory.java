package me.blueslime.bukkitmeteor.inventory.handlers;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.conditions.Conditions;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.inventory.inventory.MeteorInventory;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.inventoryhandlerapi.inventory.CustomInventory;
import me.blueslime.inventoryhandlerapi.inventory.CustomInventoryBuilder;
import me.blueslime.inventoryhandlerapi.item.action.InventoryItemAction;
import me.blueslime.inventoryhandlerapi.item.condition.InventoryItemCondition;
import me.blueslime.inventoryhandlerapi.item.list.builder.WrapperInventoryItemBuilder;
import me.blueslime.utilitiesapi.item.ItemWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class DefaultInventory extends MeteorInventory {
    private final Actions actions = Implements.fetch(Actions.class);
    private CustomInventory inventory;

    public DefaultInventory(BukkitMeteorPlugin plugin, ConfigurationSection configuration, File file) {
        super(plugin, configuration, file);
    }

    @Override
    public void load() {

        ReturnableArrayList list;

        list.addValues()
        ConfigurationSection configuration = getConfiguration();

        inventory = CustomInventoryBuilder.builder(getFile().getName(), true).build();

        ConfigurationSection section = getConfiguration().getConfigurationSection("items");

        if (section == null) {
            // Find items at that path
            section = getConfiguration();
        }

        if (section != null) {

            Conditions conditions = Implements.fetch(Conditions.class);

            for (String item : section.getKeys(false)) {

                String path = "items." + item;

                if (!configuration.getBoolean(path + ".enabled", true)) {
                    continue;
                }

                ItemWrapper wrapper = ItemWrapper.fromData(
                        configuration,
                        path
                ).setDynamic(
                        getItemExecutor()
                );

                InventoryItemAction itemAction = new InventoryItemAction(
                        event -> {
                            List<String> list = configuration.getStringList(path + ".actions");
                            Player player = event.getPlayer();

                            if (list.isEmpty()) {
                                return true;
                            }

                            actions.execute(list, player);
                            return true;
                        }
                );

                if (configuration.contains(path + ".slot")) {
                    inventory.addItem(
                        new WrapperInventoryItemBuilder(item, configuration.getInt(path + ".slot"))
                                .item(wrapper)
                                .action(itemAction)
                                .cancelClick(true)
                                .condition(
                                    new InventoryItemCondition(
                                        playerItem -> {
                                            if (configuration.contains(path + ".display-condition")) {
                                                List<String> displayConditionList = configuration.getStringList(path + ".display-condition");

                                                if (conditions != null) {
                                                    return conditions.execute(displayConditionList, playerItem.getPlayer());
                                                }
                                            }
                                            return true;
                                        }
                                    )
                                ).build()
                    );
                }

                if (configuration.contains(path + ".slots")) {
                    for (int currentSlot : configuration.getIntegerList(path + ".slots")) {
                        inventory.addItem(
                                new WrapperInventoryItemBuilder(item, currentSlot)
                                        .item(wrapper)
                                        .action(itemAction)
                                        .cancelClick(true)
                                        .condition(
                                            new InventoryItemCondition(
                                                playerItem -> {
                                                    if (configuration.contains(path + ".display-condition")) {
                                                        List<String> displayConditionList = configuration.getStringList(path + ".display-condition");

                                                        if (conditions != null) {
                                                            return conditions.execute(displayConditionList, playerItem.getPlayer());
                                                        }
                                                    }
                                                    return true;
                                                }
                                            )
                                        ).build()
                        );
                    }
                }
            }
        }
    }

    @Override
    public void setInventory(Player player, boolean clear) {
        List<String> conditionList = getConfiguration().getStringList("inventory.open-conditions");

        Conditions conditions = Implements.fetch(Conditions.class);

        if (conditions != null) {
            if (!conditions.execute(conditionList, player)) {
                return;
            }
        }

        inventory.setInventory(player, clear);
    }
}

