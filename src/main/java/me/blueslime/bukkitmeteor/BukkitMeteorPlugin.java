package me.blueslime.bukkitmeteor;

import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.builder.PluginBuilder;
import me.blueslime.bukkitmeteor.builder.impls.EmptyImplement;
import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.bukkitmeteor.commands.CommandBuilder;
import me.blueslime.bukkitmeteor.commands.list.OpenMenuCommand;
import me.blueslime.bukkitmeteor.conditions.Conditions;
import me.blueslime.bukkitmeteor.getter.MeteorGetter;
import me.blueslime.bukkitmeteor.implementation.Implementer;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.module.RegisteredModule;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.logs.LoggerType;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;
import me.blueslime.bukkitmeteor.scoreboards.Scoreboards;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.type.*;
import me.blueslime.bukkitmeteor.utils.FileUtil;
import me.blueslime.utilitiesapi.item.nbt.PersistentDataNBT;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public abstract class BukkitMeteorPlugin extends JavaPlugin implements MeteorLogger, Implementer {
    private final Map<LoggerType, String> logMap = new EnumMap<>(LoggerType.class);
    private final Map<Class<?>, Module> moduleMap = new ConcurrentHashMap<>();
    private EmptyImplement implement = EmptyImplement.NULL;

    /**
     * Use here the {@link BukkitMeteorPlugin#initialize(Object)} method to load the entire plugin data.
     * or use {@link BukkitMeteorPlugin#initialize(Object, PluginBuilder)}
     */
    public abstract void onEnable();

    /**
     * Initialize the whole plugin
     * @param instance is the {@link JavaPlugin} instanced class.
     */
    protected void initialize(Object instance) {
        initialize(instance, PluginBuilder.builder());
    }

    /**
     * Initialize the whole plugin
     * @param instance is the {@link JavaPlugin} instanced class.
     * @param generateInventoryFolder inventories
     * @param generateMenuFolder menus
     */
    protected void initialize(Object instance, boolean generateInventoryFolder, boolean generateMenuFolder) {
        PersistentDataNBT.initialize(this);
        new MeteorGetter(this);

        new Actions(this);
        new Conditions(this);
        new Scoreboards(this);

        registerOwnModules(
            PluginBuilder.builder()
                .generateInventories(generateInventoryFolder)
                .generateMenus(generateMenuFolder)
        );
        registerDatabases();
        registerModules();

        loadModules();
        loadOwnModules();
    }

    /**
     * Initialize the whole plugin
     * @param instance is the {@link JavaPlugin} instanced class.
     * @param builder Plugin Builder Setup
     */
    protected void initialize(Object instance, PluginBuilder builder) {
        PersistentDataNBT.initialize(this);
        new MeteorGetter(this);

        new Actions(this);
        new Conditions(this);
        new Scoreboards(this);

        registerOwnModules(builder);
        registerDatabases();
        registerModules();

        loadModules();
        loadOwnModules();
    }

    /**
     * Register our own modules like: Menus and Inventories to the {@link BukkitMeteorPlugin#registerModule(Module...)}
     * @param builder Plugin Builder Setup
     */
    private void registerOwnModules(PluginBuilder builder) {
        this.implement = builder.getImplement();
        registerImpl(EmptyImplement.class, implement, true);
        registerModule(
            !builder.isMenus() ?
                fetch(Menus.class).disableFolderGeneration() :
                fetch(Menus.class),
            !builder.isInventories() ?
                fetch(Inventories.class).disableFolder() :
                fetch(Inventories.class)
        ).finishOwn();
    }

    @Override
    public void onDisable() {
        unregisterImpl(RegistrationData.fromData(Scoreboards.class));
        shutdown();
    }

    /**
     * @param modules to be registered in the plugin.
     * @return plugin instance
     */
    public BukkitMeteorPlugin registerModule(Module... modules) {
        if (modules != null && modules.length >= 1) {
            for (Module module : modules) {
                fetchDataEntries(module);
            }
        }
        return this;
    }

    /**
     * @param modules to be registered in the plugin.
     * @return plugin instance
     */
    @SafeVarargs
    public final BukkitMeteorPlugin registerModule(Class<? extends Module>... modules) {
        if (modules != null && modules.length >= 1) {
            for (Class<? extends Module> moduleClass : modules) {
                Module module = createInstance(moduleClass);
                fetchDataEntries(module);
            }
        }
        return this;
    }

    public CommandBuilder getCommands() {
        return fetch(CommandBuilder.class);
    }

    private void fetchDataEntries(Module module) {
        if (module == null) {
            return;
        }
        if (module instanceof RegisteredModule registeredModule) {
            if (registeredModule.hasIdentifier()) {
                if (registeredModule.getIdentifier().isEmpty()) {
                    Implements.addRegistrationData(
                        RegistrationData.fromData(registeredModule, registeredModule.getClass()), registeredModule
                    );
                } else {
                    Implements.addRegistrationData(
                        RegistrationData.fromData(registeredModule, registeredModule.getClass(), registeredModule.getIdentifier()), registeredModule
                    );
                }
            } else {
                Implements.addRegistrationData(
                    RegistrationData.fromData(registeredModule, registeredModule.getClass()), registeredModule
                );
            }
        }
        moduleMap.put(module.getClass(), module);
    }

    /**
     * Append all registered modules size in the console
     */
    public void finish() {
        getLogger().info("Registered " + moduleMap.size() + " module(s).");
    }

    private void finishOwn() {
        getLogger().info("Registered " + moduleMap.size() + " origin module(s).");
    }

    private void loadOwnModules() {
        fetch(Menus.class).initialize();
        fetch(Inventories.class).initialize();
    }

    private void loadModules() {
        for (Module module : new HashSet<>(moduleMap.values())) {
            if (module instanceof Menus || module instanceof Inventories) {
                continue;
            }
            module.initialize();
        }
    }

    /**
     * Here you can use the {@link #registerModule(Module...)} or {@link #registerModule(Class[])}
     * This method is automatically used internally.
     */
    public abstract void registerModules();

    /**
     * Here we register our databases
     * Here you can use the {@link BukkitMeteorPlugin#registerDatabase(StorageDatabase...)}
     */
    public void registerDatabases() {

    }

    public void registerDatabase(StorageDatabase... databases) {
        for (StorageDatabase database : databases) {
            database.connect();
        }
    }

    public void registerDatabase(ConfigurationSection section, String path) {
        registerDatabase(section, path, DatabaseType.YAML, RegistrationType.DOUBLE_REGISTER, false, "");
    }

    public void registerDatabase(ConfigurationSection section, String path, RegistrationType registrationType) {
        registerDatabase(section, path, DatabaseType.YAML, registrationType, false, "");
    }

    public void registerDatabase(ConfigurationSection section, String path, RegistrationType registrationType, String identifier) {
        registerDatabase(section, path, DatabaseType.YAML, registrationType, identifier != null && !identifier.isEmpty(), identifier);
    }

    public void registerDatabase(ConfigurationSection section, String path, DatabaseType defType, RegistrationType defRegister, boolean identifier, String id) {
        path = path.isEmpty() ? "" : path.endsWith(".") ? path : path + ".";

        String type = section.getString(path + "database");

        DatabaseType selectedDatabase = type != null ? DatabaseType.fromString(type) : defType;

        switch (selectedDatabase) {
            case POSTGRE -> {
                if (identifier) {
                    registerDatabase(
                        new PostgreDatabaseService(
                            section.getString(path + "postgre.uri"),
                            section.getString(path + "postgre.user"),
                            section.getString(path + "postgre.password"),
                            defRegister,
                            id
                        )
                    );
                    return;
                }
                registerDatabase(
                    new PostgreDatabaseService(
                        section.getString(path + "postgre.uri"),
                        section.getString(path + "postgre.user"),
                        section.getString(path + "postgre.password"),
                        defRegister
                    )
                );
            }
            case YAML -> {
                if (identifier) {
                    registerDatabase(
                        new YamlDatabaseService(
                            defRegister,
                            id
                        )
                    );
                    return;
                }
                registerDatabase(
                    new YamlDatabaseService(
                        defRegister
                    )
                );
            }
            case JSON -> {
                if (identifier) {
                    registerDatabase(
                        new JsonDatabaseService(
                            defRegister,
                            id
                        )
                    );
                    return;
                }
                registerDatabase(
                    new JsonDatabaseService(
                        defRegister
                    )
                );
            }
            case MARIADB -> {
                if (identifier) {
                    registerDatabase(
                        new MariaDatabaseService(
                            section.getString(path + "mariadb.url"),
                            section.getString(path + "mariadb.user"),
                            section.getString(path + "mariadb.password"),
                            defRegister,
                            id
                        )
                    );
                    return;
                }
                registerDatabase(
                    new MariaDatabaseService(
                        section.getString(path + "mariadb.url"),
                        section.getString(path + "mariadb.user"),
                        section.getString(path + "mariadb.password"),
                        defRegister
                    )
                );
            }
            case MONGODB -> {
                if (identifier) {
                    registerDatabase(
                        new ModernMongoDatabaseService(
                            section.getString(path + "mongodb.uri"),
                            section.getString(path + "mongodb.database"),
                            defRegister,
                            id
                        )
                    );
                    return;
                }
                registerDatabase(
                    new ModernMongoDatabaseService(
                        section.getString(path + "mongodb.uri"),
                        section.getString(path + "mongodb.database"),
                        defRegister
                    )
                );
            }
        }
    }

    /**
     * Here you can auto register a /open-meteor-menu command if you want.
     */
    public void registerOpenMenuCommand() {
        Implements.createInstance(OpenMenuCommand.class).register();
    }

    /**
     * This method reloads all other modules
     */
    public void reload() {
        for (Module module : new HashSet<>(moduleMap.values())) {
            module.reload();
        }
    }

    /**
     * This method shutdown all other modules
     */
    public void shutdown() {
        for (Module module : new HashSet<>(moduleMap.values())) {
            module.shutdown();
        }
    }

    /**
     * Loads a file from the main plugin data folder
     * @param fileName file
     * @param resource if the file don't exist, it supports a resource to be loaded in that file, it supports null
     * @return FileConfiguration
     */
    public FileConfiguration load(String fileName, String resource) {
        return load(new File(getDataFolder(), fileName), resource);
    }

    /**
     * Loads a FileConfiguration from a file
     * @param fetchFile file
     * @param resource if the file don't exist, it supports a resource to be loaded in that file, it supports null
     * @return FileConfiguration
     */
    public FileConfiguration load(File fetchFile, String resource) {
        if (resource == null) {
            FileUtil.saveResource(fetchFile, null);
            return YamlConfiguration.loadConfiguration(fetchFile);
        }

        InputStream src = FileUtil.build(resource);
        src = src == null ? getResource(resource) : src;

        FileUtil.saveResource(fetchFile, src);

        return YamlConfiguration.loadConfiguration(
            fetchFile
        );
    }

    /**
     * Save a Configuration file in a specified file
     * @param configuration file
     * @param file location to be saved
     * @param resource if the file don't exist, and you have a template, it supports null.
     */
    public void save(FileConfiguration configuration, File file, String resource) {
        if (configuration == null || file == null) {
            return;
        }

        InputStream src = FileUtil.build(resource);
        src = src == null ? getResource(resource) : src;

        FileUtil.saveResource(file, src);

        PluginConsumer.process(
            () -> configuration.save(file),
            Throwable::printStackTrace
        );
    }

    public MeteorLogger getLogs() {
        return this;
    }

    @Override
    public void send(String... messages) {
        ConsoleCommandSender sender = getServer().getConsoleSender();

        for (String message : messages) {
            String convert = TextUtilities.colorize(message);
            if (convert == null) {
                sender.sendMessage(
                    message
                );
            } else {
                sender.sendMessage(
                    convert
                );
            }
        }
    }

    @Override
    public void build() {
        // DO NOT NOTHING
    }


    /**
     * This method is actually deprecated
     * please use {@link Implements#fetch(Class)} or {@link Implements#fetch(Class, String)}
     * instead of this.
     * @param module to get
     * @return module instance
     * @param <T> type of module
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> module) {
        if (moduleMap.containsKey(module)) {
            return (T) moduleMap.get(module);
        }
        return Implements.fetch(module);
    }

    /**
     * Gets the module map
     * @return module list
     * Please use {@link Implements#getRegistrationMap()}
     */
    @Deprecated
    public Map<Class<?>, Module> getModules() {
        return moduleMap;
    }

    public EmptyImplement getImplement() {
        return implement;
    }

    @Override
    public MeteorLogger setPrefix(LoggerType log, String prefix) {
        logMap.put(log, prefix);
        return this;
    }

    @Override
    public String getPrefix(LoggerType prefix) {
        return logMap.computeIfAbsent(
                prefix,
                (k) -> prefix.getDefaultPrefix(getDescription().getName())
        );
    }

    /**
     * Checks if a plugin is enabled
     * @param pluginName to check
     * @return result
     */
    public boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }
}
