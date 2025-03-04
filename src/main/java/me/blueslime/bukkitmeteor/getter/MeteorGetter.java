package me.blueslime.bukkitmeteor.getter;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.commands.CommandBuilder;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Random;

public class MeteorGetter implements AdvancedModule {
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
    public Random provideRandom() {
        return new Random();
    }

    @Register
    public CommandBuilder provideCommandBuilder() {
        return new CommandBuilder();
    }

    @Register(identifier = "folder")
    public File provideFolder() {
        return plugin.getDataFolder();
    }

    @Register
    public Menus provideMenus() {
        return new Menus(plugin);
    }

    @Register
    public Inventories provideInventories() {
        return new Inventories(plugin);
    }

    @Register
    public MeteorLogger provideLogs() {
        return plugin;
    }

    @Register
    public BukkitMeteorPlugin provideBukkitMeteorPlugin() {
        return plugin;
    }
}
