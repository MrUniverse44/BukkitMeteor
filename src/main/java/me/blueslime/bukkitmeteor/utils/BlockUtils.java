package me.blueslime.bukkitmeteor.utils;

import me.blueslime.bukkitmeteor.implementation.Implements;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BlockUtils {

    /**
     * Creates an action list but with random block types
     * @param blocks for actions
     * @param ignoreAirBlocks ignore blocks if their blocks has air
     * @return converted format
     */
    public static List<String> convertToActionList(Set<Block> blocks, boolean ignoreAirBlocks) {
        List<String> result = new ArrayList<>();

        for (Block block : blocks) {
            Location blockLocation = block.getLocation();

            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            String material = block.getType().toString();

            if (block.getType() == Material.AIR && ignoreAirBlocks) {
                continue;
            }

            String direction = "";
            if (block.getBlockData() instanceof Directional directional) {
                direction = "; " + directional.getFacing();
            }

            String format = "set-block=" + material + direction + ", " + x + ", " + y + ", " + z;
            result.add(format);
        }

        return result;
    }

    /**
     * Creates an action list but with random block types
     * @param blocks for actions
     * @param ignoreAirBlocks ignore blocks if their blocks has air
     * @param datas to set-block
     * @return converted format
     */
    public static List<String> convertToActionList(Set<Block> blocks, boolean ignoreAirBlocks, BlockData... datas) {
        List<String> result = new ArrayList<>();
        Random random = Implements.fetch(Random.class);

        for (Block block : blocks) {
            if (block.getType() == Material.AIR && ignoreAirBlocks) {
                continue;
            }

            BlockData blockData = datas.length > 0 ? datas[random.nextInt(datas.length)] : null;

            Material randomData = blockData != null
                ? blockData.material()
                : block.getType();

            BlockFace face = blockData != null && blockData.face() != null
                ? blockData.face()
                : block.getBlockData() instanceof Directional
                ? ((Directional)block.getBlockData()).getFacing()
                : null;

            String direction = "";
            if (face != null) {
                direction = "; " + face;
            }

            Location blockLocation = block.getLocation();
            int x = blockLocation.getBlockX();
            int y = blockLocation.getBlockY();
            int z = blockLocation.getBlockZ();

            String format = "set-block=" + randomData.toString() + direction + ", " + x + ", " + y + ", " + z;
            result.add(format);
        }

        return result;
    }

}
