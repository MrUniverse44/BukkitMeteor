package me.blueslime.bukkitmeteor;

import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.bukkitmeteor.commands.list.OpenMenuCommand;
import me.blueslime.bukkitmeteor.conditions.Conditions;
import me.blueslime.bukkitmeteor.getter.MeteorGetter;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.module.RegisteredModule;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.bukkitmeteor.inventory.Inventories;
import me.blueslime.bukkitmeteor.logs.LoggerType;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;
import me.blueslime.bukkitmeteor.scoreboards.Scoreboards;
import me.blueslime.bukkitmeteor.utils.FileUtil;
import me.blueslime.utilitiesapi.item.nbt.PersistentDataNBT;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.command.ConsoleCommandSender;
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
public abstract class BukkitMeteorPlugin extends JavaPlugin implements MeteorLogger {
    private final Map<LoggerType, String> logMap = new EnumMap<>(LoggerType.class);
    private final Map<Class<?>, Module> moduleMap = new ConcurrentHashMap<>();

    public abstract void onEnable();

    protected void initialize(Object instance) {
        PersistentDataNBT.initialize(this);
        new MeteorGetter(this);

        new Actions(this);
        new Conditions(this);
        new Scoreboards(this);

        registerOwnModules();
        registerModules();

        loadModules();
        loadOwnModules();
    }

    private void registerOwnModules() {
        registerModule(
            Implements.fetch(Menus.class),
            Implements.fetch(Inventories.class)
        ).finishOwn();
    }

    @Override
    public void onDisable() {
        Implements.unregister(RegistrationData.fromData(Scoreboards.class));
        shutdown();
    }

    public BukkitMeteorPlugin registerModule(Module... modules) {
        if (modules != null && modules.length >= 1) {
            for (Module module : modules) {
                fetchDataEntries(module);
            }
        }
        return this;
    }

    @SafeVarargs
    public final BukkitMeteorPlugin registerModule(Class<? extends Module>... modules) {
        if (modules != null && modules.length >= 1) {
            for (Class<? extends Module> moduleClass : modules) {
                Module module = Implements.createInstance(moduleClass);
                fetchDataEntries(module);
            }
        }
        return this;
    }

    private void fetchDataEntries(Module module) {
        if (module == null) {
            return;
        }
        if (module instanceof RegisteredModule) {
            RegisteredModule registeredModule = (RegisteredModule) module;
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

    public void finish() {
        getLogger().info("Registered " + moduleMap.size() + " module(s).");
    }

    private void finishOwn() {
        getLogger().info("Registered " + moduleMap.size() + " origin module(s).");
    }

    private void loadOwnModules() {
        Implements.fetch(Menus.class).initialize();
        Implements.fetch(Inventories.class).initialize();
    }

    private void loadModules() {
        for (Module module : new HashSet<>(moduleMap.values())) {
            if (module instanceof Menus || module instanceof Inventories) {
                continue;
            }
            module.initialize();
        }
    }

    public abstract void registerModules();

    public void registerOpenMenuCommand() {
        Implements.createInstance(OpenMenuCommand.class).register();
    }

    public void reload() {
        for (Module module : new HashSet<>(moduleMap.values())) {
            module.reload();
        }
    }

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

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> module) {
        return (T) moduleMap.get(module);
    }

    public Map<Class<?>, Module> getModules() {
        return moduleMap;
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

    public boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }
}
