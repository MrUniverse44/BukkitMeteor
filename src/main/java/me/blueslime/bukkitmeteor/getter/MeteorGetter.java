package me.blueslime.bukkitmeteor.getter;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class MeteorGetter implements Module {
    private final BukkitMeteorPlugin plugin;

    public MeteorGetter(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        registerImplementedModule(this);
    }

    @Register(identifier = "settings.yml")
    public FileConfiguration provideSettings() {
        return plugin.load(new File(plugin.getDataFolder(), "settings.yml"), "settings.yml");
    }

    @Register
    public Menus provideMenus() {
        return new Menus(plugin);
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Register
    public Inventories provideInventories() {
        return new Inventories(plugin);
    }

    @Register
    public MeteorLogger getLogs() {
        return plugin;
    }

    @Register
    public BukkitMeteorPlugin provideBukkitMeteorPlugin() {
        return plugin;
    }
}
