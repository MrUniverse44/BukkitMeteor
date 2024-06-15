package me.blueslime.bukkitmeteor.configuration;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public abstract class ConfigurationHandler implements ConfigurationSection {
    private final ConfigurationType type;

    public ConfigurationHandler(ConfigurationType type, ConfigurationFile file) {
        this.type = type;
        load(type, file);
    }

    public abstract void load(ConfigurationType type, ConfigurationFile file);

    public abstract <T> T toSpecifiedConfiguration();

    /**
     * Gives a list
     * @param path Path of the list
     * @return List
     */
    public abstract List<?> getList(String path);

    /**
     * Gives a list
     * @param path Path of the list
     * @param def Default List
     * @return List. If path-list exists gives the list of the path
     * If the path list doesn't exist, gives the default list
     */
    public abstract List<?> getList(String path, List<?> def);

    /**
     * Gives a String-Text
     * @param path String Path Location
     * @return String
     */
    public String getString(String path) {
        return getString(path, null);
    }

    /**
     * Gives a String-Text
     * @param path String Path Location
     * @param def If the path doesn't exist, this will be the default result or null
     * @return String
     */
    public abstract String getString(String path, String def);

    /**
     * Gives a StringList
     * @param path Path of the list
     * @return String List
     */
    public abstract List<String> getStringList(String path);

    /**
     * Gives an Integer List
     * @param path Integer List Location Path
     * @return Integer List
     */
    public abstract List<Integer> getIntList(String path);

    public abstract long getLong(String path, long def);

    public abstract long getLong(String path);

    public abstract List<Long> getLongList(String path);

    public abstract List<Boolean> getBooleanList(String path);

    public abstract List<Byte> getByteList(String path);

    public abstract List<Character> getCharList(String path);

    public abstract List<Float> getFloatList(String path);

    public abstract Object get(String path);

    public abstract Object get(String path, Object def);

    public abstract int getInt(String path, int def);

    public abstract int getInt(String path);

    public abstract boolean contains(String path);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     * @param path Path Location
     * @return Boolean
     */
    public boolean getBoolean(String path) {
        return getStatus(path);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     * @param path Path Location
     * @param def Default Result if the path doesn't exist
     * @return Boolean
     */
    public boolean getBoolean(String path, boolean def) {
        return getStatus(path, def);
    }

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     * @param path Path Location
     * @return Boolean
     */
    public abstract boolean getStatus(String path);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     * @param path Path Location
     * @param def Default Result if the path doesn't exist
     * @return Boolean
     */
    public abstract boolean getStatus(String path, boolean def);

    /**
     * Set a path to a specified result
     * @param path Path Location
     * @param value Value of the path
     */
    public abstract void set(String path,Object value);

    /**
     * Get keys of a specified path
     * @param path Path of the content
     * @param getKeys get the content with keys
     * @return StringList
     */
    public abstract List<String> getKeys(String path, boolean getKeys);

    /**
     * Get a configuration handler section
     * @param path Section location
     * @return ConfigurationHandler
     */
    public abstract ConfigurationSection getSection(String path);

    /**
     * Create a new Configuration handler section
     * @param path Section location
     * @return ConfigurationHandler
     */
    public abstract ConfigurationSection createSection(String path);

    public abstract Set<String> getKeySet(boolean deep);

    public ConfigurationType getType() {
        return type;
    }

    public Random getRandom() {
        return ThreadLocalRandom.current();
    }
}
