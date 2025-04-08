package me.blueslime.bukkitmeteor.getter;

import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.commands.CommandBuilder;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;
import java.io.File;

public class MeteorGetter implements Service {

    public MeteorGetter(BukkitMeteorPlugin plugin) {
        // Register settings.yml
        registerImpl(
            FileConfiguration.class,
            "settings.yml",
            plugin.load(new File(plugin.getDataFolder(), "settings.yml"), "settings.yml"),
            true
        );
        // Register random
        registerImpl(
            Random.class,
            new Random(),
            true
        );
        // Register Command Builder
        registerImpl(
            CommandBuilder.class,
            new CommandBuilder(),
            true
        );
        // Register plugin's folder
        registerImpl(
            File.class,
            plugin.getDataFolder(),
            true
        );
        // Register Menus instance
        registerImpl(
            Menus.class,
            new Menus(plugin),
            true
        );
        // Register inventories instance
        registerImpl(
            Inventories.class,
            new Inventories(plugin),
            true
        );
        // Register logger instance
        registerImpl(
            MeteorLogger.class,
            plugin,
            true
        );
        // Register BukkitMeteorPlugin instance
        registerImpl(
            BukkitMeteorPlugin.class,
            plugin,
            true
        );
    }

    @Override
    public void reload() {
        // Obtain plugin instance
        BukkitMeteorPlugin plugin = fetch(BukkitMeteorPlugin.class);
        // Register settings.yml
        registerImpl(
            FileConfiguration.class,
            "settings.yml",
            plugin.load(new File(plugin.getDataFolder(), "settings.yml"), "settings.yml"),
            true
        );
    }
}
