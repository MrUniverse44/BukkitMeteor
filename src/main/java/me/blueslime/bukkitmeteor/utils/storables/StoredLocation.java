package me.blueslime.bukkitmeteor.utils.storables;

import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIgnore;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.utils.WorldLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.Objects;

public class StoredLocation implements StorageObject {

    @StorageKey(key = "pitch", defaultValue = "0.0F")
    private float pitch;

    @StorageIgnore
    private String world;

    @StorageKey(key = "yaw", defaultValue = "0.0F")
    private float yaw;

    @StorageKey(key = "x", defaultValue = "0")
    private double x;

    @StorageKey(key = "y", defaultValue = "0")
    private double y;

    @StorageKey(key = "z", defaultValue = "0")
    private double z;

    @StorageKey(key = "id")
    private String id;

    @StorageConstructor
    public StoredLocation(
        @StorageKey(key = "pitch", defaultValue = "0.0F") float pitch,
        @StorageKey(key = "yaw", defaultValue = "0.0F") float yaw,
        @StorageKey(key = "x", defaultValue = "0") double x,
        @StorageKey(key = "y", defaultValue = "0") double y,
        @StorageKey(key = "z", defaultValue = "0") double z,
        @StorageKey(key = "id") String id
    ) {
        this.world = "world";
        this.pitch = 0.0F;
        this.yaw = 0.0F;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static StoredLocation at(String id, Player player) {
        return at(id, player.getLocation());
    }

    public static StoredLocation at(String id, Location location) {
        return new StoredLocation(
            location.getPitch(),
            location.getYaw(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            id
        );
    }

    public StoredLocation copy() {
        return new StoredLocation(
            pitch,
            yaw,
            x,
            y,
            z,
            id
        );
    }

    public String getWorld() {
        return this.world;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getBlockX() {
        return this.floor(this.x);
    }

    public int getBlockY() {
        return this.floor(this.y);
    }

    public int getBlockZ() {
        return this.floor(this.z);
    }

    public StoredLocation add(double x, double y, double z, float yaw, float pitch) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.yaw += yaw;
        this.pitch += pitch;
        return this;
    }

    public StoredLocation remove(double x, double y, double z, float yaw, float pitch) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.yaw -= yaw;
        this.pitch -= pitch;
        return this;
    }

    public double distance(Location o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(Location o) {
        return o == null ? 0.0
                : NumberConversions.square(this.x - o.getX())
                + NumberConversions.square(this.y - o.getY())
                + NumberConversions.square(this.z - o.getZ());
    }

    private int floor(double num) {
        int floor = (int)num;
        return (double)floor == num ? floor : floor - (int)(Double.doubleToRawLongBits(num) >>> 63);
    }

    public StoredLocation world(String world) {
        this.world = world;
        return this;
    }

    public boolean compareLocations(WorldLocation location) {
        if (this.world != null) {
            return this.getBlockX() == location.getBlockX() && this.getBlockY() == location.getBlockY() && this.getBlockZ() == location.getBlockZ() && this.world.equals(location.getWorld());
        } else {
            return this.getBlockX() == location.getBlockX() && this.getBlockY() == location.getBlockY() && this.getBlockZ() == location.getBlockZ();
        }
    }

    public boolean basicCompare(Location location) {
        return this.getBlockX() == location.getBlockX() &&
               this.getBlockY() == location.getBlockY() &&
               this.getBlockZ() == location.getBlockZ();
    }

    public boolean compareLocation(Location location) {
        if (this.world != null) {
            if (location.getWorld() == null) {
                return false;
            } else {
                return this.getBlockX() == location.getBlockX() && this.getBlockY() == location.getBlockY() && this.getBlockZ() == location.getBlockZ() && this.world.equals(location.getWorld().getName());
            }
        } else {
            return this.getBlockX() == location.getBlockX() && this.getBlockY() == location.getBlockY() && this.getBlockZ() == location.getBlockZ();
        }
    }

    public Location toLocation() {
        World world = this.world != null ? Bukkit.getServer().getWorld(this.world) : null;
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public Location toLocationAt(World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public StoredLocation clone() {
        return new StoredLocation(this.pitch, this.yaw, this.x, this.y, this.z, this.id).world(this.world);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            StoredLocation that = (StoredLocation)o;
            if (this.x == that.x && this.y == that.y && this.z == that.z) {
                return Objects.equals(this.world, that.world);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.world != null ? this.world.hashCode() : 0;
        result = 31 * result + this.getBlockX();
        result = 31 * result + this.getBlockY();
        result = 31 * result + this.getBlockZ();
        return result;
    }
}
