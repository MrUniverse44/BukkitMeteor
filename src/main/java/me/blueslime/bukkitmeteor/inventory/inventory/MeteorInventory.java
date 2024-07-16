package me.blueslime.bukkitmeteor.inventory.inventory;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.utilitiesapi.item.ItemWrapper;
import me.blueslime.utilitiesapi.item.dynamic.executor.DynamicExecutor;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.tools.PlaceholderParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;

public abstract class MeteorInventory {
    private final ConfigurationSection configuration;
    private final BukkitMeteorPlugin plugin;
    private final File file;

    public MeteorInventory(BukkitMeteorPlugin plugin, ConfigurationSection configuration, File file) {
        this.configuration = configuration;
        this.plugin = plugin;
        this.file = file;

        load();
    }

    public void load() {

    }

    public abstract void setInventory(Player player, boolean clear);

    public void setInventory(Player player) {
        setInventory(player, true);
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public BukkitMeteorPlugin getPlugin() {
        return plugin;
    }

    public String getUniqueId() {
        return getFile().getName()
                .toLowerCase(Locale.ENGLISH)
                .replace(".yml", "");
    }

    public DynamicExecutor getItemExecutor() {
        return (item) -> {
            ItemWrapper original = item.getWrapper();
            ItemWrapper wrapper = original.clone();

            Player player = item.getPlayer();

            TextReplacer replacer = TextReplacer.builder()
                    .replace("<player_name>", player.getName())
                    .replace("<heart>", "❤");

            if (plugin.isPluginEnabled("PlaceholderAPI")) {
                wrapper.setName(
                        original.getName() != null ?
                                PlaceholderParser.parse(player, replacer.apply(original.getName())) :
                                original.getName()
                );

                wrapper.setLore(
                        new ReturnableArrayList<>(original.getLore()).replace(
                                line -> PlaceholderParser.parse(
                                        player,
                                        replacer.apply(line)
                                )
                        )
                );
            } else {
                wrapper.setName(
                    original.getName() != null ?
                        replacer.apply(original.getName()) :
                        original.getName()
                );

                wrapper.setLore(
                    new ReturnableArrayList<>(original.getLore()).replace(
                        replacer::apply
                    )
                );
            }
            return wrapper;
        };
    }

    public boolean hasPlaceholders() {
        return plugin.isPluginEnabled("PlaceholderAPI");
    }

    public File getFile() {
        return file;
    }
}
