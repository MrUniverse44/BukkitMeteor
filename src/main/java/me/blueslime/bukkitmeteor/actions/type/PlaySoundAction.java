package me.blueslime.bukkitmeteor.actions.type;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.actions.action.Action;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class PlaySoundAction extends Action {
    public PlaySoundAction() {
        super("[sound]", "<sound>", "sound:");
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
        try {
            String[] arguments = parameter.replace(" ", "").split(",");

            if (arguments.length == 1) {
                Sound sound = Sound.valueOf(parameter.toUpperCase(Locale.ENGLISH));

                play(players, sound, 1, 1);
            } else if (arguments.length == 2) {
                Sound sound = Sound.valueOf(arguments[0].toUpperCase(Locale.ENGLISH));

                play(players, sound, Integer.parseInt(arguments[1]), 1);
            } else if (arguments.length >= 3) {
                Sound sound = Sound.valueOf(arguments[0].toUpperCase(Locale.ENGLISH));

                play(players, sound, Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
            }
        } catch (IllegalArgumentException ignored) {
            plugin.getLogs().error("Can't find sound: " + parameter);
            plugin.getLogs().error("This sound can't be reproduced");
        }
    }

    private void play(List<Player> players, Sound sound, int volume, int pitch) {
        players.forEach(
            player -> player.playSound(player.getLocation(), sound, volume, pitch)
        );
    }
}
