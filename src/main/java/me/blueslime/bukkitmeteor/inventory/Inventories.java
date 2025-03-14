package me.blueslime.bukkitmeteor.inventory;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.inventory.event.InventoriesFolderGenerationEvent;
import me.blueslime.bukkitmeteor.inventory.handlers.DefaultInventory;
import me.blueslime.bukkitmeteor.inventory.inventory.MeteorInventory;
import me.blueslime.inventoryhandlerapi.InventoryHandlerAPI;
import me.blueslime.utilitiesapi.reflection.utils.storage.PluginStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public class Inventories implements Service {
    private final PluginStorage<String, MeteorInventory> menuStorage = PluginStorage.initAsConcurrentHash();
    private final BukkitMeteorPlugin plugin;
    private boolean generateFolder = true;

    public Inventories(BukkitMeteorPlugin plugin) {
        this.plugin = plugin;
        InventoryHandlerAPI.setCustomPrefix("bkt-mtr-");
        InventoryHandlerAPI.setCustomIdentifierPrefix("bkt-ite-");
        InventoryHandlerAPI.register(plugin);
    }

    public Inventories disableFolder() {
        generateFolder = false;
        return this;
    }

    @Override
    public void initialize() {
        shutdown();

        File folder = new File(
                plugin.getDataFolder(),
                "inventories"
        );

        if (!folder.exists()) {
            if (!generateFolder) {
                return;
            }
            InventoriesFolderGenerationEvent event = new InventoriesFolderGenerationEvent(folder);

            plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            if (folder.mkdirs()) {
                plugin.getLogs().info("Created inventories folder.");
            }
        }

        folder = new File(
            plugin.getDataFolder(),
            "inventories"
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
            plugin.getLogs().info("Registered inventory with id: " + identifier);

            menuStorage.add(
                identifier,
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

    @Override
    public void shutdown() {

    }

    /**
     * Get a specified menu using the file name
     * @param key is the file name (including .yml)
     * @return null if the menu don't exist
     */
    public MeteorInventory getSpecifiedInventory(String key) {
        if (key == null) {
            plugin.getLogs().error("Invalid inventory null key");
            return null;
        }
        return menuStorage.get(
            key.toLowerCase(Locale.ENGLISH)
        );
    }

    public PluginStorage<String, MeteorInventory> getInventoryStorage() {
        return menuStorage;
    }
}

