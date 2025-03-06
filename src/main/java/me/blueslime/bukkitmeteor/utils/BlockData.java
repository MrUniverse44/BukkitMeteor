package me.blueslime.bukkitmeteor.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public record BlockData(Material material, BlockFace face) {

    public static BlockData of(Material material, BlockFace face) {
        return new BlockData(material, face);
    }

    public static BlockData of(Material material) {
        return new BlockData(material, null);
    }

}
