package me.blueslime.bukkitmeteor.inventory;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.inventory.event.InventoriesFolderGenerationEvent;
import me.blueslime.bukkitmeteor.inventory.handlers.DefaultInventory;
import me.blueslime.bukkitmeteor.inventory.inventory.MeteorInventory;
import me.blueslime.inventoryhandlerapi.InventoryHandlerAPI;
import me.blueslime.utilitiesapi.reflection.utils.storage.PluginStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public class CustomInventoryProvider implements Module {
    private final PluginStorage<String, MeteorInventory> menuStorage = PluginStorage.initAsConcurrentHash();
    private final BukkitMeteorPlugin plugin;

    public CustomInventoryProvider(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        InventoryHandlerAPI.setCustomPrefix("bkt-mtr-");
        InventoryHandlerAPI.setCustomIdentifierPrefix("bkt-ite-");
        InventoryHandlerAPI.register(plugin);
    }

    @Override
    public void initialize() {
        menuStorage.clear();

        File folder = new File(
                plugin.getDataFolder(),
                "inventories"
        );

        if (!folder.exists() && folder.mkdirs()) {
            plugin.getServer().getPluginManager().callEvent(new InventoriesFolderGenerationEvent(folder));
            plugin.getLogs().info("Created inventories folder.");
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) {
            return;
        }

        for (File file : files) {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            plugin.getLogs().info("Registered inventory with id: " + file.getName());

            menuStorage.add(
                file.getName().toLowerCase(Locale.ENGLISH).replace(
                    ".yml",
                    ""
                ),
                (k) -> new DefaultInventory(
                    plugin,
                    configuration,
                    file
                )
            );
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
    public MeteorInventory getSpecifiedInventory(String key) {
        if (key == null) {
            return null;
        }
        return menuStorage.get(
            key.toLowerCase()
        );
    }

    public PluginStorage<String, MeteorInventory> getInventoryStorage() {
        return menuStorage;
    }
}

