package me.blueslime.bukkitmeteor.menus;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.menus.event.MenusFolderGenerationEvent;
import me.blueslime.bukkitmeteor.menus.list.DefaultMenu;
import me.blueslime.menuhandlerapi.MenuHandlerAPI;
import me.blueslime.utilitiesapi.reflection.utils.storage.PluginStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public class Menus implements Module {
    private final PluginStorage<String, Menu> menuStorage = PluginStorage.initAsConcurrentHash();
    private final BukkitMeteorPlugin plugin;

    public Menus(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        MenuHandlerAPI.setCustomIdentifierPrefix("bkt-mir-");
        MenuHandlerAPI.setCustomMenuPrefix("bkt-cmi-");
        MenuHandlerAPI.setCustomItemPrefix("bkt-ime-");
        MenuHandlerAPI.register(false, plugin);
    }

    @Override
    public void initialize() {
        menuStorage.clear();

        File folder = new File(
                plugin.getDataFolder(),
                "menus"
        );

        if (!folder.exists() && folder.mkdirs()) {
            plugin.getServer().getPluginManager().callEvent(new MenusFolderGenerationEvent(folder));
            plugin.getLogs().info("Created menus folder.");
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return;
        }

        for (File file : files) {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            int type = Integer.parseInt(
                    String.valueOf(configuration.get("settings.menu-type", 0))
            );

            if (type <= 1) {
                menuStorage.add(
                    file.getName().toLowerCase(Locale.ENGLISH).replace(
                        ".yml",
                        ""
                    ),
                    (k) -> new DefaultMenu(
                        plugin,
                        configuration,
                        file
                    )
                );
            }
        }
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
    public Menu getSpecifiedMenu(String key) {
        if (key == null) {
            return null;
        }
        return menuStorage.get(
                key.toLowerCase()
        );
    }
}
