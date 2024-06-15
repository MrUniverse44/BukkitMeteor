package me.blueslime.bukkitmeteor.configuration.custom;

import me.blueslime.bukkitmeteor.configuration.ConfigurationFile;
import me.blueslime.bukkitmeteor.configuration.ConfigurationHandler;
import me.blueslime.bukkitmeteor.configuration.ConfigurationProvider;
import me.blueslime.bukkitmeteor.configuration.ConfigurationType;
import me.blueslime.bukkitmeteor.configuration.handlers.CustomConfigurationHandler;
import me.blueslime.bukkitmeteor.utils.FileUtil;

public class CustomProvider implements ConfigurationProvider {
    @Override
    public ConfigurationHandler load(ConfigurationType type, ConfigurationFile file) {
        return new CustomConfigurationHandler(type, file);
    }

    @Override
    public void save(ConfigurationHandler config, ConfigurationFile file) throws Exception {
        if (config == null || file == null) {
            return;
        }

        FileUtil.saveResource(file.getFile(), file.getResource());

        if (config.getType() == ConfigurationType.JSON) {
            JsonConfiguration.save(config.getMainHandler().toSpecifiedConfiguration(), file.getFile());
        } else {
            YamlConfiguration.save(config.getMainHandler().toSpecifiedConfiguration(), file.getFile());
        }
    }
}
