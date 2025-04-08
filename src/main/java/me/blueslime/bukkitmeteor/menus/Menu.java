package me.blueslime.bukkitmeteor.menus;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.utils.list.ReturnableArrayList;
import me.blueslime.utilitiesapi.item.ItemWrapper;
import me.blueslime.utilitiesapi.item.dynamic.executor.DynamicExecutor;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.tools.PlaceholderParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;

public abstract class Menu {
    private final FileConfiguration configuration;
    private final BukkitMeteorPlugin plugin;
    private final File file;

    public Menu(
        BukkitMeteorPlugin plugin,
        FileConfiguration configuration,
        File file
    ) {
        this.configuration = configuration;
        this.plugin = plugin;
        this.file = file;

        load();
    }

    public void load() {

    }

    public abstract void openMenu(Player player, TextReplacer replacer);

    public void openMenu(Player player) {
        openMenu(player, TextReplacer.builder());
    }

    public void openMenu(Player player, String extraData) {
        openMenu(player);
    }

    public FileConfiguration getConfiguration() {
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

    public DynamicExecutor getItemExecutor(TextReplacer replacer) {
        return (item) -> {
            ItemWrapper original = item.getWrapper();
            ItemWrapper wrapper = original.clone();

            Player player = item.getPlayer();

            replacer.replace("<player_name>", player.getName()).replace("<heart>", "❤");

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
