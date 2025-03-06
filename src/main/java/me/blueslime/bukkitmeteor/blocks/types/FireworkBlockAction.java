package me.blueslime.bukkitmeteor.blocks.types;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.blocks.action.BlockAction;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkBlockAction extends BlockAction {

    public FireworkBlockAction() {
        super("<firework>", "firework=");
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
        if (world == null) {
            return;
        }

        Location location = getLocation(parameter, world);

        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        // Customize the firework
        FireworkEffect effect = FireworkEffect.builder()
            .withColor(Color.RED)
            .withFade(Color.YELLOW)
            .withFlicker()
            .withTrail()
            .with(FireworkEffect.Type.STAR)
            .build();

        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
