package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.inventory.inventory.MeteorInventory;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class InventoryAction extends Action {

    public InventoryAction() {
        super("<inventory>", "[inventory]", "inventory:");
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param players   players
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, List<Player> players) {
        String id = replace(parameter.toLowerCase(Locale.ENGLISH));
        Inventories menus = Implements.fetch(Inventories.class);
        MeteorInventory inventory = menus.getSpecifiedInventory(id);
        if (inventory != null) {
            players.forEach(inventory::setInventory);
        } else {
            plugin.getLogs().error("Can't find inventory with id: " + id);
        }
    }
}
