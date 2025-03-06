package me.blueslime.bukkitmeteor.blocks.types;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.blocks.action.BlockAction;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.Locale;

public class SetBlockAction extends BlockAction {

    public SetBlockAction() {
        super("set-block=", "s-b=", "set=");
    }

    /**
     * Execute action
     *
     * @param plugin of the event
     * @param parameter text
     * @param world to use
     */
    @Override
    public void execute(BukkitMeteorPlugin plugin, String parameter, World world) {

        Location location = getLocation(parameter, world);
        String[] parts = getAction(parameter).toUpperCase(Locale.ENGLISH).split(";");

        String materialName = parts[0];
        String direction = parts.length > 1 ? parts[1].toUpperCase(Locale.ENGLISH) : "";

        Material material = PluginConsumer.ofUnchecked(
            () -> Material.valueOf(materialName),
            e -> plugin.error("Can't find block with name: " + materialName),
            () -> Material.GLASS
        );

        Block block = location.getBlock();

        if (block.getType() == material) {
            return;
        }

        block.setType(
                material, false
        );

        if (!direction.isEmpty()) {
            BlockData blockData = block.getBlockData();

            if (blockData instanceof Directional directional) {
                PluginConsumer.process(
                    () -> {
                        directional.setFacing(
                            PluginConsumer.ofUnchecked(
                                () -> BlockFace.valueOf(direction),
                                e -> plugin.error("Can't find direction: " + direction),
                                () -> BlockFace.NORTH
                            )
                        );
                        block.setBlockData(directional);
                    },
                    e -> plugin.error(e, "Can't change the block direction")
                );
            }
        }

        block.getState().update(true, false);
    }
}