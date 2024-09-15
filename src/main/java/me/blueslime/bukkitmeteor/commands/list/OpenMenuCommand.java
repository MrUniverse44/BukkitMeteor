package me.blueslime.bukkitmeteor.commands.list;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.InjectedCommand;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.menus.Menus;
import me.blueslime.bukkitmeteor.menus.list.PersonalMenu;
import me.blueslime.utilitiesapi.commands.sender.Sender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class OpenMenuCommand extends InjectedCommand {

    public OpenMenuCommand() {
        super("open-meteor-menu");
    }

    @Override
    public void executeCommand(Sender sender, String command, String[] arguments) {
        if (arguments.length == 0) {
            return;
        }

        String filename = arguments[0].toLowerCase(Locale.ENGLISH);

        String playerName = arguments.length >= 2 &&
            (sender.hasPermission("pillars.admin") || sender.isConsole())
                ? arguments[1]
                : null;

        Menus menus = Implements.fetch(Menus.class);

       if (playerName == null) {

           if (!sender.isPlayer()) {
                return;
            }

            PersonalMenu menuX = menus.getSpecifiedMenu(filename, sender.toPlayer());

            if (menuX != null) {
                menuX.open(sender.toPlayer());
            }

        } else {

           Player targetPlayer = Implements
                    .fetch(BukkitMeteorPlugin.class)
                    .getServer()
                    .getPlayer(playerName);

           if (targetPlayer == null) {
                return;
           }

           PersonalMenu menu = menus.getSpecifiedMenu(filename, targetPlayer);

           if (menu != null) {
                menu.open(targetPlayer);
           }
        }
    }
}
