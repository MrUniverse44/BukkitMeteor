package me.blueslime.bukkitmeteor.menus;

import fr.mrmicky.fastinv.FastInvManager;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.menus.event.MenusFolderGenerationEvent;
import me.blueslime.bukkitmeteor.menus.list.PersonalMenu;
import me.blueslime.utilitiesapi.reflection.utils.storage.PluginStorage;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;

public class Menus implements Module {
    private final PluginStorage<String, ConfigurationSection> menuStorage = PluginStorage.initAsConcurrentHash();
    private final BukkitMeteorPlugin plugin;

    public Menus(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        FastInvManager.register(plugin);
    }

    @Override
    public void initialize() {
        shutdown();

        File folder = new File(
            plugin.getDataFolder(),
            "menus"
        );

        if (!folder.exists()) {
            MenusFolderGenerationEvent event = new MenusFolderGenerationEvent(folder);

            plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            if (folder.mkdirs()) {
                plugin.getLogs().info("Created menus folder.");
            }
        }

        folder = new File(
            plugin.getDataFolder(),
            "menus"
        );

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return;
        }

        for (File file : files) {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            String identifier = file.getName().toLowerCase(Locale.ENGLISH).replace(
                ".yml",
                ""
            );
            plugin.getLogs().info("Registered menu with id: " + identifier);

            menuStorage.set(
                identifier,
                configuration
            );
        }
    }

    public void shutdown() {

    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void reload() {
        initialize();
    }

    /**
     * Get a specified menu using the file name
     * @param key is the file name (including .yml)
     * @return null if the menu don't exist
     */
    @SuppressWarnings("unused")
    public ConfigurationSection getSpecifiedMenuSettings(String key) {
        if (key == null) {
            plugin.getLogs().error("Invalid null menu key");
            return null;
        }
        return menuStorage.get(
            key.toLowerCase(Locale.ENGLISH)
        );
    }

    /**
     * Get a specified menu using the file name
     * @param key is the file name (including .yml)
     * @return null if the menu don't exist
     */
    public PersonalMenu getSpecifiedMenu(String key, Player player) {
        if (key == null || player == null) {
            plugin.getLogs().error("Invalid null menu or player keys for openMenu");
            return null;
        }
        ConfigurationSection configuration =  menuStorage.get(
            key.toLowerCase(Locale.ENGLISH)
        );
        if (configuration == null) {
            plugin.getLogs().error("Can't find menu key: " + key + " for: " + player.getName());
            return null;
        }
        return new PersonalMenu(
            plugin,
            player,
            configuration,
            TextReplacer.builder()
                .replace("%player%", player.getName())
                .replace("<player>", player.getName())
        );
    }

    /**
     * Get a specified menu using the file name
     * @param key is the file name (including .yml)
     * @return null if the menu don't exist
     */
    public PersonalMenu getSpecifiedMenu(String key, Player player, TextReplacer replacer) {
        if (key == null || player == null) {
            plugin.getLogs().error("Invalid null menu or player keys for openMenu");
            return null;
        }
        ConfigurationSection configuration =  menuStorage.get(
                key.toLowerCase(Locale.ENGLISH)
        );
        if (configuration == null) {
            plugin.getLogs().error("Can't find menu key: " + key + " for: " + player.getName());
            return null;
        }
        return new PersonalMenu(
                plugin,
                player,
                configuration,
                replacer
        );
    }

    @SuppressWarnings("unused")
    public PluginStorage<String, ConfigurationSection> getMenuStorageSettings() {
        return menuStorage;
    }
}
