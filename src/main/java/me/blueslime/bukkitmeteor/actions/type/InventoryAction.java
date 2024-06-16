package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.inventory.CustomInventoryProvider;
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
        CustomInventoryProvider menus = Implements.fetch(CustomInventoryProvider.class);
        MeteorInventory inventory = menus.getSpecifiedInventory(parameter.toLowerCase(Locale.ENGLISH));
        if (inventory != null) {
            players.forEach(inventory::setInventory);
        } else {
            plugin.getLogs().error("Can't find inventory with id: " + parameter.toLowerCase(Locale.ENGLISH));
        }
    }
}
