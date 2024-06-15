package me.blueslime.bukkitmeteor.configuration.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BukkitConfiguration implements me.blueslime.bukkitmeteor.configuration.ConfigurationSection {
    private final ConfigurationSection section;
    private final BukkitConfiguration main;

    public BukkitConfiguration(ConfigurationSection section) {
        this.section = section;
        this.main = this;
    }

    public BukkitConfiguration() {
        this.section = new YamlConfiguration();
        this.main = this;
    }

    public BukkitConfiguration(BukkitConfiguration main, ConfigurationSection section) {
        this.section = section == null ? new YamlConfiguration() : section;
        this.main = main;
    }

    @Override
    public me.blueslime.bukkitmeteor.configuration.ConfigurationSection getMainHandler() {
        return main;
    }

    @Override
    public me.blueslime.bukkitmeteor.configuration.ConfigurationSection asSection() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toSpecifiedConfiguration() {
        if (section == null) {
            return null;
        }
        return (T) section;
    }

    /**
     * Gives a list
     *
     * @param path Path of the list
     * @return List
     */
    @Override
    public List<?> getList(String path) {
        return section.getList(path);
    }

    /**
     * Gives a list
     *
     * @param path Path of the list
     * @param defList  Default List
     * @return List. If path-list exists gives the list of the path
     * If the path list doesn't exist, gives the default list
     */
    @Override
    public List<?> getList(String path, List<?> defList) {
        return section.getList(path, defList);
    }

    /**
     * Gives a String-Text
     *
     * @param path String Path Location
     * @return String
     */
    @Override
    public String getString(String path) {
        return section.getString(path);
    }

    /**
     * Gives a String-Text
     *
     * @param path String Path Location
     * @param defValue  If the path doesn't exist, this will be the default result or null
     * @return String
     */
    @Override
    public String getString(String path, String defValue) {
        return section.getString(path, defValue);
    }

    /**
     * Gives a StringList
     *
     * @param path Path of the list
     * @return String List
     */
    @Override
    public List<String> getStringList(String path) {
        return section.getStringList(path);
    }

    /**
     * Gives an Integer List
     *
     * @param path Integer List Location Path
     * @return Integer List
     */
    @Override
    public List<Integer> getIntList(String path) {
        return section.getIntegerList(path);
    }

    @Override
    public long getLong(String path, long defLong) {
        return section.getLong(path, defLong);
    }

    @Override
    public long getLong(String path) {
        return section.getLong(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        return section.getLongList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return section.getBooleanList(path);
    }

    @Override
    public List<Byte> getByteList(String path) {
        return section.getByteList(path);
    }

    @Override
    public List<Character> getCharList(String path) {
        return section.getCharacterList(path);
    }

    @Override
    public List<Float> getFloatList(String path) {
        return section.getFloatList(path);
    }

    @Override
    public Object get(String path) {
        return section.get(path);
    }

    @Override
    public Object get(String path, Object defObject) {
        return section.get(path, defObject);
    }

    @Override
    public int getInt(String path, int defValue) {
        return section.getInt(path, defValue);
    }

    @Override
    public int getInt(String path) {
        return section.getInt(path);
    }

    @Override
    public boolean contains(String path) {
        return section.contains(path);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     *
     * @param path Path Location
     * @return Boolean
     */
    @Override
    public boolean getBoolean(String path) {
        return section.getBoolean(path);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     *
     * @param path Path Location
     * @param defBoolean  Default Result if the path doesn't exist
     * @return Boolean
     */
    @Override
    public boolean getBoolean(String path, boolean defBoolean) {
        return section.getBoolean(path, defBoolean);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     *
     * @param path Path Location
     * @return Boolean
     */
    @Override
    public boolean getStatus(String path) {
        return section.getBoolean(path);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     *
     * @param path Path Location
     * @param defBoolean  Default Result if the path doesn't exist
     * @return Boolean
     */
    @Override
    public boolean getStatus(String path, boolean defBoolean) {
        return section.getBoolean(path, defBoolean);
    }

    /**
     * Set a path to a specified result
     *
     * @param path  Path Location
     * @param value Value of the path
     */
    @Override
    public void set(String path, Object value) {
        section.set(path, value);
    }

    /**
     * Get keys of a specified path
     *
     * @param path    Path of the content
     * @param getKeys get the content with keys
     * @return StringList
     */
    @Override
    public List<String> getKeys(String path, boolean getKeys) {
        List<String> rx = new ArrayList<>();

        ConfigurationSection section = this.section.getConfigurationSection(path);

        if (section == null) {
            return rx;
        }

        rx.addAll(section.getKeys(getKeys));

        return Collections.unmodifiableList(rx);
    }

    /**
     * Get a configuration handler section
     *
     * @param path Section location
     * @return ConfigurationHandler
     */
    @Override
    public me.blueslime.bukkitmeteor.configuration.ConfigurationSection getSection(String path) {
        return new BukkitConfiguration(this, section.getConfigurationSection(path));
    }

    /**
     * Create a new Configuration handler section
     *
     * @param path Section location
     * @return ConfigurationHandler
     */
    @Override
    public me.blueslime.bukkitmeteor.configuration.ConfigurationSection createSection(String path) {
        return new BukkitConfiguration(this, section.getConfigurationSection(path));
    }

    @Override
    public Set<String> getKeySet(boolean deep) {
        return section.getKeys(deep);
    }

    @Override
    public Random getRandom() {
        return ThreadLocalRandom.current();
    }
}