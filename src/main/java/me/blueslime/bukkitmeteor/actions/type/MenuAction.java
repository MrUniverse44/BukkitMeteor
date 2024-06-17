package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.menus.Menus;
import me.blueslime.bukkitmeteor.menus.list.PersonalMenu;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class MenuAction extends Action {
    public MenuAction() {
        super("[menu]", "<menu>", "menu:");
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
        Menus menus = Implements.fetch(Menus.class);
        players.forEach(player -> {
            PersonalMenu menu = menus.getSpecifiedMenu(id, player);
            if (menu != null) {
                menu.open(player);
            }
        });
    }
}
