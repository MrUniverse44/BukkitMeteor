package me.blueslime.bukkitmeteor.configuration;

import java.util.List;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("unused")
public interface ConfigurationSection extends Configuration {
    ConfigurationSection getMainHandler();

    ConfigurationSection asSection();

    <T> T toSpecifiedConfiguration();

    /**
     * Gives a list
     * @param path Path of the list
     * @return List
     */
    List<?> getList(String path);

    /**
     * Gives a list
     * @param path Path of the list
     * @param def Default List
     * @return List. If path-list exists gives the list of the path
     * If the path list doesn't exist, gives the default list
     */
    List<?> getList(String path, List<?> def);

    /**
     * Gives a String-Text
     * @param path String Path Location
     * @return String
     */
    String getString(String path);

    /**
     * Gives a String-Text
     * @param path String Path Location
     * @param def If the path doesn't exist, this will be the default result or null
     * @return String
     */
    String getString(String path, String def);

    /**
     * Gives a StringList
     * @param path Path of the list
     * @return String List
     */
    List<String> getStringList(String path);

    /**
     * Gives an Integer List
     * @param path Integer List Location Path
     * @return Integer List
     */
    List<Integer> getIntList(String path);

    long getLong(String path, long def);

    long getLong(String path);

    List<Long> getLongList(String path);

    List<Boolean> getBooleanList(String path);

    List<Byte> getByteList(String path);

    List<Character> getCharList(String path);

    List<Float> getFloatList(String path);

    Object get(String path);

    Object get(String path, Object def);

    int getInt(String path, int def);

    int getInt(String path);

    boolean contains(String path);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     * @param path Path Location
     * @return Boolean
     */
    boolean getBoolean(String path);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     * @param path Path Location
     * @param def Default Result if the path doesn't exist
     * @return Boolean
     */
    boolean getBoolean(String path, boolean def);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * false, but if the path exists it will return true.
     * @param path Path Location
     * @return Boolean
     */
    boolean getStatus(String path);

    /**
     * Check if the path exists, if the path doesn't exist this will return
     * the default boolean specified, but if the path exists it will return true.
     * @param path Path Location
     * @param def Default Result if the path doesn't exist
     * @return Boolean
     */
    boolean getStatus(String path, boolean def);

    /**
     * Set a path to a specified result
     * @param path Path Location
     * @param value Value of the path
     */
    void set(String path, Object value);

    /**
     * Get keys of a specified path
     * @param path Path of the content
     * @param getKeys get the content with keys
     * @return StringList
     */
    List<String> getKeys(String path, boolean getKeys);

    /**
     * Get a configuration handler section
     * @param path Section location
     * @return ConfigurationHandler
     */
    ConfigurationSection getSection(String path);

    /**
     * Create a new Configuration handler section
     * @param path Section location
     * @return ConfigurationHandler
     */
    ConfigurationSection createSection(String path);

    Set<String> getKeySet(boolean deep);

    Random getRandom();
}
