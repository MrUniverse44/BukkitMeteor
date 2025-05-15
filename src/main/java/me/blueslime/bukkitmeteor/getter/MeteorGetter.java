package me.blueslime.bukkitmeteor.getter;

import me.blueslime.bukkitmeteor.builder.PluginBuilder;
import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.commands.CommandBuilder;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.languages.LanguageProvider;
import me.blueslime.bukkitmeteor.languages.types.DynamicLanguageProviderService;
import me.blueslime.bukkitmeteor.languages.types.StaticLanguageProviderService;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;
import java.io.File;

public class MeteorGetter implements Service {

    public static boolean LANGUAGES = false;

    public MeteorGetter(BukkitMeteorPlugin plugin, PluginBuilder builder) {
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
        // Register settings.yml
        registerImpl(
            FileConfiguration.class,
            "settings.yml",
            plugin.load(new File(plugin.getDataFolder(), "settings.yml"), "settings.yml"),
            true
        );
        LANGUAGES = builder.hasMessageFile();
        registerImpl(
            Boolean.class,
            "languages",
            LANGUAGES,
            true
        );
        if (builder.hasMessageFile()) {
            // Register language files
            if (builder.isMultilingual()) {
                registerImpl(
                    LanguageProvider.class,
                    new DynamicLanguageProviderService(plugin, builder),
                    true
                );
            } else {
                registerImpl(
                    LanguageProvider.class,
                    new StaticLanguageProviderService(),
                    true
                );
            }
        }
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

        if (LANGUAGES) {
            LanguageProvider provider = fetch(LanguageProvider.class);
            if (provider == null) {
                return;
            }
            provider.reload();
        }
    }
}
