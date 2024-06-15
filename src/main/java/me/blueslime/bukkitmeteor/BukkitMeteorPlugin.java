package me.blueslime.bukkitmeteor;

import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.bukkitmeteor.inventory.CustomInventoryProvider;
import me.blueslime.bukkitmeteor.logs.LoggerType;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.menus.Menus;
import me.blueslime.bukkitmeteor.scoreboards.Scoreboards;
import me.blueslime.bukkitmeteor.utils.FileUtil;
import me.blueslime.bukkitmeteor.utils.PluginConsumer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class BukkitMeteorPlugin extends JavaPlugin implements MeteorLogger {
    private final Map<LoggerType, String> logMap = new EnumMap<>(LoggerType.class);
    private final Map<Class<?>, Module> moduleMap = new HashMap<>();

    public abstract void onEnable();

    protected void initialize(Object instance) {
        Implements.register(instance);

        new Actions(this);
        new Scoreboards(this);

        registerOwnModules();
        registerModules();

        loadModules();
        loadOwnModules();
    }

    private void registerOwnModules() {
        registerModule(
            Implements.fetch(Menus.class),
            Implements.fetch(CustomInventoryProvider.class)
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
                if (module == null) {
                    continue;
                }
                moduleMap.put(module.getClass(), module);
            }
        }
        return this;
    }

    public void finish() {
        getLogger().info("Registered " + moduleMap.size() + " module(s).");
    }

    private void finishOwn() {
        getLogger().info("Registered " + moduleMap.size() + " origin module(s).");
    }

    private void loadOwnModules() {
        Implements.fetch(Menus.class).initialize();
        Implements.fetch(CustomInventoryProvider.class).initialize();
    }

    private void loadModules() {
        for (Module module : moduleMap.values()) {
            if (module instanceof Menus || module instanceof CustomInventoryProvider) {
                continue;
            }
            module.initialize();
        }
    }

    public abstract void registerModules();

    public void reload() {
        for (Module module : moduleMap.values()) {
            module.reload();
        }
    }

    public void shutdown() {
        for (Module module : moduleMap.values()) {
            module.shutdown();
        }
    }

    public FileConfiguration load(String fileName, String resource) {
        return load(new File(getDataFolder(), fileName), resource);
    }

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

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> module) {
        return (T) moduleMap.get(module);
    }

    public Map<Class<?>, Module> getModules() {
        return moduleMap;
    }

    @Register(identifier = "settings.yml")
    public FileConfiguration provideSettings() {
        return load(new File(getDataFolder(), "settings.yml"), "settings.yml");
    }

    @Register
    public Menus provideMenus() {
        return new Menus(this);
    }

    @Register
    public CustomInventoryProvider provideInventories() {
        return new CustomInventoryProvider(this);
    }

    @Register
    public MeteorLogger getLogs() {
        return this;
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
                (k) -> prefix.getDefaultPrefix()
        );
    }

    @Register
    public BukkitMeteorPlugin provideBukkitMeteorPlugin() {
        return this;
    }

    public boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }
}
