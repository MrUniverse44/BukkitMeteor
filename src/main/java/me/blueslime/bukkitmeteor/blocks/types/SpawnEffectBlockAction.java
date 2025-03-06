package me.blueslime.bukkitmeteor.blocks.types;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.blocks.action.BlockAction;
import me.blueslime.utilitiesapi.tools.PluginTools;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Locale;

public class SpawnEffectBlockAction extends BlockAction {

    public SpawnEffectBlockAction() {
        super("spawn-effect=", "effect=");
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param world     to use
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, World world) {
        String[] split = replace(parameter).replace(" ", "").split(",");

        int amount = split.length >= 2 ? PluginTools.isNumber(split[1]) ? Integer.parseInt(split[1]) : 5 : 5;

        String replaced = split[0].toUpperCase(Locale.ENGLISH);

        Effect effect = PluginConsumer.ofUnchecked(
            () -> Effect.valueOf(replaced),
            e -> getLogs().info("Can't find effect with name: " + replaced),
            () -> Effect.POTION_BREAK
        );

        Location location = getLocation(parameter, world);

        world.playEffect(
            location,
            effect,
            amount
        );
    }
}

