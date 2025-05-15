package me.blueslime.bukkitmeteor.languages.types;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.languages.LanguageProvider;
import me.blueslime.bukkitmeteor.languages.objects.Locale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class StaticLanguageProviderService implements LanguageProvider {

    public StaticLanguageProviderService() {
        initialize();
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void reload() {
        registerImpl(
            FileConfiguration.class,
            "messages.yml",
            fetch(BukkitMeteorPlugin.class).load(
                new File(getDataFolder(), "messages.yml"),
                "/messages.yml"
            ),
            true
        );
    }

    @Override
    public FileConfiguration fromPlayerLocale(Player player) {
        return fetch(FileConfiguration.class, "messages.yml");
    }

    @Override
    public FileConfiguration fromLocaleCode(Locale locale) {
        return fetch(FileConfiguration.class, "messages.yml");
    }

}
