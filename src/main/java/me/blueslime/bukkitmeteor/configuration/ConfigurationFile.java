package me.blueslime.bukkitmeteor.configuration;

import java.io.File;
import java.io.InputStream;

public class ConfigurationFile {
    private final File file;
    private final InputStream resource;

    private ConfigurationFile(File file, InputStream resource) {
        this.resource = resource;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public InputStream getResource() {
        return resource;
    }

    public static ConfigurationFile build(File file, InputStream resource) {
        return new ConfigurationFile(file, resource);
    }

    public static ConfigurationFile build(File file) {
        return new ConfigurationFile(file, null);
    }
}
