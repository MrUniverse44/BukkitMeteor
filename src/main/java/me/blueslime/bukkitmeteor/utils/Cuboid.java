package me.blueslime.bukkitmeteor.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cuboid {

    private final double xMinCentered;
    private final double xMaxCentered;
    private final double yMinCentered;
    private final double yMaxCentered;
    private final double zMinCentered;
    private final double zMaxCentered;
    private final World world;
    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;

    public Cuboid(final Location point1, final Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.xMinCentered = xMin + 0.5;
        this.xMaxCentered = xMax + 0.5;
        this.yMinCentered = yMin + 0.5;
        this.yMaxCentered = yMax + 0.5;
        this.zMinCentered = zMin + 0.5;
        this.zMaxCentered = zMax + 0.5;
        this.world = point1.getWorld();
    }

    public Cuboid(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.xMin = minX;
        this.xMax = maxX;
        this.yMin = minY;
        this.yMax = maxY;
        this.zMin = minZ;
        this.zMax = maxZ;
        this.xMinCentered = xMin + 0.5;
        this.xMaxCentered = xMax + 0.5;
        this.yMinCentered = yMin + 0.5;
        this.yMaxCentered = yMax + 0.5;
        this.zMinCentered = zMin + 0.5;
        this.zMaxCentered = zMax + 0.5;
        this.world = world;
    }

    public Set<Block> getBlocks() {
        if (world == null) {
            return new HashSet<>();
        }
        final Set<Block> blockList = new HashSet<>(this.getTotalBlockSize());
        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int y = this.yMin; y <= this.yMax; ++y) {
                for (int z = this.zMin; z <= this.zMax; ++z) {
                    final Block b = this.world.getBlockAt(x, y, z);
                    blockList.add(b);
                }
            }
        }
        return blockList;
    }

    public Location getCenter() {
        return new Location(
                this.world,
                (double)(this.xMax - this.xMin) / 2 + this.xMin,
                (double)(this.yMax - this.yMin) / 2 + this.yMin,
                (double)(this.zMax - this.zMin) / 2 + this.zMin
        );
    }

    public double getDistance() {
        return this.getPoint1().distance(this.getPoint2());
    }

    public double getDistanceSquared() {
        return this.getPoint1().distanceSquared(this.getPoint2());
    }

    public int getHeight() {
        return this.yMax - this.yMin + 1;
    }

    public int getTotalBlockSize() {
        return this.getHeight() * this.getXWidth() * this.getZWidth();
    }

    public int getVolume() {
        return getTotalBlockSize();
    }

    public Location getPoint1() {
        return new Location(this.world, this.xMin, this.yMin, this.zMin);
    }

    public Location getPoint2() {
        return new Location(this.world, this.xMax, this.yMax, this.zMax);
    }

    public int getXWidth() {
        return this.xMax - this.xMin + 1;
    }

    public int getZWidth() {
        return this.zMax - this.zMin + 1;
    }

    public boolean isIn(final Location loc) {
        return loc.getWorld() == this.world &&
                loc.getBlockX() >= this.xMin - 1 && loc.getBlockX() <= this.xMax + 1 &&
                loc.getBlockY() >= this.yMin && loc.getBlockY() <= this.yMax &&
                loc.getBlockZ() >= this.zMin - 1 && loc.getBlockZ() <= this.zMax + 1;
    }

    public boolean isIn(final Player player) {
        return this.isIn(player.getLocation());
    }

    public boolean isInWithMarge(final Location loc, final double marge) {
        return loc.getWorld() == this.world &&
                loc.getX() >= this.xMinCentered - marge && loc.getX() <= this.xMaxCentered + marge &&
                loc.getY() >= this.yMinCentered - marge && loc.getY() <= this.yMaxCentered + marge &&
                loc.getZ() >= this.zMinCentered - marge && loc.getZ() <= this.zMaxCentered + marge;
    }

    public boolean contains(int x, int y, int z, String requiredMaterial) {
        if (x < this.xMin || x > this.xMax || y < this.yMin || y > this.yMax || z < this.zMin || z > this.zMax) {
            return false;
        }
        Block block = this.world.getBlockAt(x, y, z);
        return block.getType().toString().equalsIgnoreCase(requiredMaterial);
    }

    public Set<Entity> getEntities() {
        Set<Entity> entities = new HashSet<>();
        int minChunkX = this.xMin >> 4;
        int maxChunkX = this.xMax >> 4;
        int minChunkZ = this.zMin >> 4;
        int maxChunkZ = this.zMax >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                // Comprobamos que el chunk esté cargado para evitar cargar chunks innecesariamente.
                if (world.isChunkLoaded(cx, cz)) {
                    Chunk chunk = world.getChunkAt(cx, cz);
                    for (Entity entity : chunk.getEntities()) {
                        if (this.isIn(entity.getLocation())) {
                            entities.add(entity);
                        }
                    }
                }
            }
        }
        return entities;
    }

    public Set<Player> getPlayers() {
        Set<Player> players = new HashSet<>();
        for (Entity entity : getEntities()) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public boolean intersects(Cuboid other) {
        if (this.world != other.world) return false;
        return this.xMax >= other.xMin && this.xMin <= other.xMax &&
                this.yMax >= other.yMin && this.yMin <= other.yMax &&
                this.zMax >= other.zMin && this.zMin <= other.zMax;
    }

    public List<Location> getCorners() {
        List<Location> corners = new ArrayList<>(8);
        corners.add(new Location(world, xMin, yMin, zMin));
        corners.add(new Location(world, xMin, yMin, zMax));
        corners.add(new Location(world, xMin, yMax, zMin));
        corners.add(new Location(world, xMin, yMax, zMax));
        corners.add(new Location(world, xMax, yMin, zMin));
        corners.add(new Location(world, xMax, yMin, zMax));
        corners.add(new Location(world, xMax, yMax, zMin));
        corners.add(new Location(world, xMax, yMax, zMax));
        return corners;
    }
}
