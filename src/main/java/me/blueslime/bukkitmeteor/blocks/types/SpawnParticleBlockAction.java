package me.blueslime.bukkitmeteor.blocks.types;


import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.blocks.action.BlockAction;
import me.blueslime.utilitiesapi.tools.PluginTools;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.Locale;

public class SpawnParticleBlockAction extends BlockAction {

    public SpawnParticleBlockAction() {
        super("spawn-particle=", "particle=", "spawn=");
    }

    /**
     * Execute action
     *
     * @param plugin    of the event
     * @param parameter text
     * @param world to use
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, World world) {
        String[] split = replace(parameter).replace(" ", "").split(",");

        int amount = split.length >= 2 ? PluginTools.toInteger(split[1], 5) : 5;

        String replaced = split[0].toUpperCase(Locale.ENGLISH);

        Particle particle = PluginConsumer.ofUnchecked(
            () -> Particle.valueOf(replaced),
            e -> getLogs().info("Can't find particle with name: " + replaced),
            () -> null
        );

        if (particle == null) {
            return;
        }

        Location location = getLocation(parameter, world);

        world.spawnParticle(
                particle,
                location,
                amount
        );
    }
}
