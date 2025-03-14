package me.blueslime.bukkitmeteor.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public class PluginUtil {
    /**
     * Converts the inserted value in a row of an chest inventory.
     * @param size is the inserted value
     * @return converted row.
     */
    public static int getRows(int size) {
        if (size < 0) return 9;
        if (size < 7) return size * 9;
        if (size > 50) return 54;
        if (size > 40) return 45;
        if (size > 30) return 36;
        if (size > 20) return 27;
        if (size > 10) return 18;
        return 9;
    }

    public Set<Entity> getEntitiesAt(WorldLocation location, int range) {
        return getEntitiesAt(location.toLocation(), range);
    }

    public Set<Entity> getEntitiesAt(Location location, int range) {
        return getEntitiesAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), range);
    }

    public Set<Entity> getEntitiesAt(World world, double x, double y, double z, int range) {
        Set<Entity> entities = new HashSet<>();

        int chunkX = (int) x >> 4;
        int chunkZ = (int) z >> 4;
        int chunkRadius = (int) Math.ceil(range / 16.0);

        Location location = new Location(world, x, y, z);

        for (int cx = chunkX - chunkRadius; cx <= chunkX + chunkRadius; cx++) {
            for (int cz = chunkZ - chunkRadius; cz <= chunkZ + chunkRadius; cz++) {
                Chunk chunk = world.getChunkAt(cx, cz);
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getLocation().distanceSquared(location) <= range * range) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
}
