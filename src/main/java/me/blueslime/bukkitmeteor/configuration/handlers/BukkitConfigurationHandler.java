package me.blueslime.bukkitmeteor.configuration.handlers;

import me.blueslime.bukkitmeteor.configuration.ConfigurationFile;
import me.blueslime.bukkitmeteor.configuration.ConfigurationHandler;
import me.blueslime.bukkitmeteor.configuration.ConfigurationSection;
import me.blueslime.bukkitmeteor.configuration.ConfigurationType;
import me.blueslime.bukkitmeteor.configuration.custom.JsonConfiguration;
import me.blueslime.bukkitmeteor.utils.FileUtil;
import me.blueslime.bukkitmeteor.utils.PluginConsumer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class BukkitConfigurationHandler extends ConfigurationHandler {

    private ConfigurationSection configuration = new BukkitConfiguration();
    private final ConfigurationHandler main;

    public BukkitConfigurationHandler(ConfigurationType type, ConfigurationFile file) {
        super(type, file);
        this.main = this;
    }

    public BukkitConfigurationHandler(ConfigurationHandler main, ConfigurationSection file) {
        super(main.getType(), null);
        this.configuration = file;
        this.main = main;
    }

    @Override
    public void load(ConfigurationType type, ConfigurationFile file) {
        if (file != null) {
            configuration = loadConfig(file.getFile(), file.getResource());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toSpecifiedConfiguration() {
        return (T) configuration;
    }

    @Override
    public Object get(String path) { return configuration.get(path); }

    @Override
    public Object get(String path,Object def) { return configuration.get(path,def); }

    @Override
    public long getLong(String path,long def) { return configuration.getLong(path,def); }

    @Override
    public long getLong(String path) { return configuration.getLong(path); }

    @Override
    public List<Long> getLongList(String path) { return configuration.getLongList(path); }

    @Override
    public List<Boolean> getBooleanList(String path) { return configuration.getBooleanList(path); }

    @Override
    public List<Byte> getByteList(String path) { return configuration.getByteList(path); }

    @Override
    public List<Character> getCharList(String path) { return configuration.getCharList(path); }
    @Override
    public List<Float> getFloatList(String path) { return configuration.getFloatList(path); }

    private ConfigurationSection loadConfig(File file, InputStream resource) {
        if (!file.exists()) {
            FileUtil.saveResource(file, resource);
        }

        if (getType() == ConfigurationType.YAML) {
            return PluginConsumer.ofUnchecked(
                    () -> new BukkitConfiguration(YamlConfiguration.loadConfiguration(file)),
                    ignored -> {
                    },
                    BukkitConfiguration::new
            );
        }
        return PluginConsumer.ofUnchecked(
                () -> new CustomConfigurationHandler(JsonConfiguration.load(file)),
                ignored -> {},
                () -> new CustomConfigurationHandler(getType())
        );
    }

    @Override
    public List<?> getList(String path) {
        return configuration.getList(path);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return configuration.getList(path,def);
    }

    @Override
    public String getString(String path) {
        return configuration.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return configuration.getString(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return configuration.getStringList(path);
    }

    @Override
    public List<String> getKeys(String path, boolean getKeys) {
        List<String> rx = new ArrayList<>();

        ConfigurationSection section = configuration.getSection(path);

        if (section == null) {
            return rx;
        }
        rx.addAll(section.getKeys(path, getKeys));

        return Collections.unmodifiableList(rx);
    }

    @Override
    public ConfigurationHandler getSection(String path) {
        return new BukkitConfigurationHandler(main, configuration.getSection(path));
    }

    @Override
    public ConfigurationHandler createSection(String path) {
        return new BukkitConfigurationHandler(main, configuration.getSection(path));
    }

    @Override
    public List<Integer> getIntList(String path) {
        return configuration.getIntList(path);
    }

    @Override
    public int getInt(String path, int def) {
        return configuration.getInt(path,def);
    }

    @Override
    public int getInt(String path) {
        return configuration.getInt(path);
    }

    @Override
    public boolean contains(String path) {
        return configuration.contains(path);
    }

    @Override
    public boolean getStatus(String path) {
        return configuration.getBoolean(path);
    }

    @Override
    public boolean getStatus(String path, boolean def) {
        return configuration.getBoolean(path, def);
    }

    @Override
    public void set(String path, Object value) {
        configuration.set(path,value);
    }

    @Override
    public Set<String> getKeySet(boolean deep) {
        return new HashSet<>(configuration.getKeySet(deep));
    }

    @Override
    public ConfigurationHandler getMainHandler() {
        return main;
    }

    @Override
    public ConfigurationSection asSection() {
        return this;
    }
}
