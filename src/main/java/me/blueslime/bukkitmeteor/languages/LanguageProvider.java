package me.blueslime.bukkitmeteor.languages;

import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.languages.objects.Locale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public interface LanguageProvider extends Service {

    FileConfiguration fromPlayerLocale(Player player);

    FileConfiguration fromLocaleCode(Locale locale);

}
