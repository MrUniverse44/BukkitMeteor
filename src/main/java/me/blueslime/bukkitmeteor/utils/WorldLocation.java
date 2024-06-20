package me.blueslime.bukkitmeteor.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class WorldLocation {
    protected final String world;
    protected final int x;
    protected final int y;
    protected final int z;
    protected final float yaw;
    protected final float pitch;

    public WorldLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public WorldLocation(String world, int x, int y, int z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static WorldLocation at(Player player) {
        return at(player.getLocation());
    }

    public static WorldLocation at(Location location) {
        if (location.getWorld() != null) {
            return new WorldLocation(
                    location.getWorld().getName(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    location.getYaw(),
                    location.getPitch()
            );
        }
        return new WorldLocation(
                null,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean compareLocations(WorldLocation location) {
        if (world != null) {
            return x == location.getX() && y == location.getY() && z == location.getZ() && world.equals(location.getWorld());
        }
        return x == location.getX() && y == location.getY() && z == location.getZ();
    }

    public boolean compareLocation(Location location) {
        if (world != null) {
            if (location.getWorld() == null) {
                return false;
            }
            return x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ() && world.equals(location.getWorld().getName());
        }
        return x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ();
    }

    public void print(FileConfiguration configuration, String path, boolean deep) {
        path = !path.isEmpty() ? path.endsWith(".") ? path : path + "." : "";

        path = path + (deep ? "location-" + hashCode()  + "." : "");

        configuration.set(path + "world", world);
        configuration.set(path + "x", x);
        configuration.set(path + "y", y);
        configuration.set(path + "z", z);
        configuration.set(path + "yaw", String.valueOf(yaw));
        configuration.set(path + "pitch", String.valueOf(pitch));
    }

    public static WorldLocation fromConfiguration(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        return new WorldLocation(
                section.getString("world", null),
                section.getInt("x", 0),
                section.getInt("y", 0),
                section.getInt("z", 0),
                Float.parseFloat(section.getString("yaw", "0")),
                Float.parseFloat(section.getString("pitch", "0"))
        );
    }

    public static WorldLocation fromConfiguration(ConfigurationSection section, String path) {
        if (section == null) {
            return null;
        }
        path = path != null && !path.isEmpty() ? path.endsWith(".") ? path : path + "." : "";

        return new WorldLocation(
                section.getString(path +"world", null),
                section.getInt(path + "x", 0),
                section.getInt(path + "y", 0),
                section.getInt(path + "z", 0),
                Float.parseFloat(section.getString(path + "yaw", "0")),
                Float.parseFloat(section.getString(path + "pitch", "0"))
        );
    }

    public static List<WorldLocation> getLocations(ConfigurationSection section) {
        if (section == null) {
            return new ArrayList<>();
        }
        List<WorldLocation> worldLocationList = new ArrayList<>();

        for (String id : section.getKeys(false)) {
            WorldLocation worldLocation = fromConfiguration(section.getConfigurationSection(id));
            if (worldLocation != null) {
                worldLocationList.add(
                        worldLocation
                );
            }
        }

        return worldLocationList;
    }

    public Location toLocation() {
        World world = this.world != null ? Bukkit
                .getServer()
                .getWorld(this.world) : null;

        return new Location(
                world,
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public WorldLocation clone() {
        return new WorldLocation(world, x, y, z, yaw, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldLocation that = (WorldLocation) o;
        if (x != that.x || y != that.y || z != that.z) return false;
        return world != null ? world.equals(that.world) : that.world == null;
    }

    @Override
    public int hashCode() {
        int result = world != null ? world.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}

