package me.blueslime.bukkitmeteor.configuration;

import java.util.Locale;

public enum ConfigurationType {
    JSON,
    YAML;

    public static ConfigurationType detect(ConfigurationFile file) {
        String name = file.getFile().getName().toLowerCase(Locale.ENGLISH);

        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return YAML;
        }
        return JSON;
    }
}
