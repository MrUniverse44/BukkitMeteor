package me.blueslime.bukkitmeteor;

import me.blueslime.bukkitmeteor.actions.Actions;
import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.bukkitmeteor.configuration.ConfigurationFile;
import me.blueslime.bukkitmeteor.configuration.ConfigurationHandler;
import me.blueslime.bukkitmeteor.configuration.ConfigurationProvider;
import me.blueslime.bukkitmeteor.configuration.ConfigurationType;
import me.blueslime.bukkitmeteor.configuration.custom.JsonConfiguration;
import me.blueslime.bukkitmeteor.configuration.handlers.BukkitConfigurationHandler;
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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class BukkitMeteorPlugin extends JavaPlugin implements ConfigurationProvider, MeteorLogger {
    private final Map<LoggerType, String> logMap = new EnumMap<>(LoggerType.class);
    private final Map<Class<?>, Module> moduleMap = new HashMap<>();

    public abstract void onEnable();

    protected void initialize() {
        new Actions(this);
        new Scoreboards(this);

        registerModules();

        loadModules();
    }

    @Override
    public void onDisable() {
        Implements.unregister(RegistrationData.fromData(Scoreboards.class));
        shutdown();
    }

    public BukkitMeteorPlugin registerModule(Module... modules) {
        if (modules != null && modules.length >= 1) {
            for (Module module : modules) {
                moduleMap.put(module.getClass(), module);
            }
        }
        return this;
    }

    public void finish() {
        getLogger().info("Registered " + moduleMap.size() + " module(s).");
    }

    private void loadModules() {
        for (Module module : moduleMap.values()) {
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

    @Override
    public ConfigurationHandler load(ConfigurationType type, ConfigurationFile file) {
        return new BukkitConfigurationHandler(type, file);
    }

    @Override
    public void save(ConfigurationHandler config, ConfigurationFile file) throws Exception {
        if (config == null || file == null) {
            return;
        }

        FileUtil.saveResource(file.getFile(), file.getResource());

        if (config.getType() == ConfigurationType.YAML) {
            FileConfiguration configuration = config.toSpecifiedConfiguration();

            configuration.save(file.getFile());
        } else {
            JsonConfiguration.save(config.toSpecifiedConfiguration(), file.getFile());
        }
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
    public ConfigurationHandler provideSettings() {
        InputStream src = FileUtil.build("settings.yml");
        return getConfigurations().load(
            ConfigurationType.YAML,
            ConfigurationFile.build(
                new File(getDataFolder(), "settings.yml"),
                src != null ? src : getResource("settings.yml")
            )
        );
    }

    @Register
    public Menus provideMenus() {
        Menus menus = new Menus(this);
        menus.initialize();
        return menus;
    }

    @Register
    public CustomInventoryProvider provideInventories() {
        CustomInventoryProvider inventories = new CustomInventoryProvider(this);
        inventories.initialize();
        return inventories;
    }

    @Register
    public ConfigurationProvider getConfigurations() {
        return this;
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

    public boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }
}
