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
        String id = replace(parameter);
        String[] split = id.replace(" ", "").split(",");

        String filename = split[0].toLowerCase(Locale.ENGLISH);
        String playerName = split.length >= 2 ? split[1] : null;

        Menus menus = Implements.fetch(Menus.class);
        players.forEach(player -> {
            if (playerName == null) {
                PersonalMenu menu = menus.getSpecifiedMenu(filename, player);
                if (menu != null) {
                    menu.open(player);
                }
            } else {
                Player targetPlayer = plugin.getServer().getPlayer(playerName);
                if (targetPlayer == null) {
                    return;
                }
                PersonalMenu menu = menus.getSpecifiedMenu(filename, targetPlayer);
                if (menu != null) {
                    menu.open(targetPlayer);
                }
            }
        });
    }
}
