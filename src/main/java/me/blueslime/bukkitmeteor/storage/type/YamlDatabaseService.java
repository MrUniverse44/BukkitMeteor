package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class YamlDatabaseService extends StorageDatabase implements AdvancedModule {
    private final File dataFolder;

    /**
     * Initialize YAML database connection
     * @param register Whether to register this service in Implements
     */
    public YamlDatabaseService(RegistrationType register) {
        this.dataFolder = new File(fetch(File.class, "folder"), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            registerImpl(YamlDatabaseService.class, this, true);
        }

        if (register.isDouble()) {
            registerImpl(StorageDatabase.class, this, true);
        }
    }

    /**
     * Initialize YAML database connection
     * @param register to register this connection to the Implements
     * @param identifier used for the Implements in {@link Implements#fetch(Class, String)}
     */
    public YamlDatabaseService(RegistrationType register, String identifier) {
        this.dataFolder = new File(fetch(File.class, "folder"), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        boolean isSet = identifier != null;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            if (isSet) {
                registerImpl(YamlDatabaseService.class, identifier, this, true);
            } else {
                registerImpl(YamlDatabaseService.class, this, true);
            }
        }

        if (register.isDouble()) {
            if (isSet) {
                registerImpl(StorageDatabase.class, identifier, this, true);
            } else {
                registerImpl(StorageDatabase.class, this, true);
            }
        }
    }

    @Override
    public CompletableFuture<Void> saveOrUpdateAsync(StorageObject obj) {
        return CompletableFuture.runAsync(() -> save(obj));
    }

    @Override
    public void saveOrUpdateSync(StorageObject obj) {
        save(obj);
    }

    @Override
    public void connect() {
        /* Empty body because Yaml doesn't need this */
    }

    private void save(StorageObject obj) {
        Class<?> clazz = obj.getClass();
        File file = new File(dataFolder, clazz.getSimpleName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String identifierValue = null;

        Map<String, Object> addMap = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(StorageIgnore.class)) {
                    continue;
                }

                Object value = field.get(obj);
                String name = field.getName();

                if (field.isAnnotationPresent(StorageIdentifier.class)) {
                    identifierValue = value.toString();
                }

                if (field.isAnnotationPresent(StorageKey.class)) {
                    StorageKey key = field.getAnnotation(StorageKey.class);
                    if (!key.key().isEmpty()) {
                        name = key.key();
                    }
                    if (value == null && !key.defaultValue().isEmpty()) {
                        value = convertValue(field.getType(), key.defaultValue());
                    }
                }

                if (isComplexObject(field.getType())) {
                    addMap.put(name, handleComplexObject(value));
                } else {
                    addMap.put(name, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String prefix = identifierValue != null ? identifierValue + "." : "";
        addMap.forEach((key, value) -> config.set(prefix + key, value));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object handleComplexObject(Object obj) {
        if (obj == null) {
            return null;
        }

        Class<?> objClass = obj.getClass();

        if (obj instanceof Iterable) {
            List<Object> list = new ArrayList<>();
            for (Object element : (Iterable<?>) obj) {
                list.add(handleComplexObject(element));
            }
            return list;
        }

        if (objClass.isArray()) {
            return IntStream.range(0, Array.getLength(obj))
                    .mapToObj(i -> handleComplexObject(Array.get(obj, i)))
                    .collect(Collectors.toList());
        }

        if (obj instanceof Map) {
            Map<Object, Object> resultMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                resultMap.put(handleComplexObject(entry.getKey()), handleComplexObject(entry.getValue()));
            }
            return resultMap;
        }

        if (isPrimitiveOrWrapper(objClass) || objClass.equals(String.class)) {
            return obj;
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();

        for (Field field : objClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                resultMap.put(field.getName(), handleComplexObject(fieldValue));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return resultMap;
    }


    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) ||
                type.equals(Integer.class) ||
                type.equals(Character.class) ||
                type.equals(Byte.class) ||
                type.equals(Short.class) ||
                type.equals(Double.class) ||
                type.equals(Long.class) ||
                type.equals(Float.class);
    }


    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.supplyAsync(() -> loadByIdSync(clazz, identifier));
    }

    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        File file = new File(dataFolder, clazz.getSimpleName() + ".yml");
        if (!file.exists()) {
            return Optional.empty();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section;


        if (identifier == null || identifier.isEmpty()) {
            section = config;
        } else {
            section = config.getConfigurationSection(identifier);
        }

        return Optional.ofNullable(instantiateObject(clazz, section, identifier));
    }

    @Override
    public <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.runAsync(() -> delete(clazz, identifier));
    }

    @Override
    public <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier) {
        File file = new File(dataFolder, clazz.getSimpleName() + ".yml");
        if (file.exists() && (identifier == null || identifier.isEmpty())) {

            file.delete();
        }
    }

    public <T extends StorageObject> void delete(Class<T> clazz, String identifier) {
        File file = new File(dataFolder, clazz.getSimpleName() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, ConfigurationSection config, String identifier) {
        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (constructor.isAnnotationPresent(StorageConstructor.class)) {
                    Parameter[] parameters = constructor.getParameters();
                    Object[] values = new Object[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        StorageKey paramAnnotation = parameters[i].getAnnotation(StorageKey.class);
                        String paramName = (paramAnnotation != null && !paramAnnotation.key().isEmpty())
                                ? paramAnnotation.key()
                                : parameters[i].getName();

                        Object value;

                        if (isComplexObject(parameters[i].getType())) {
                            ConfigurationSection section = config.getConfigurationSection(paramName);

                            if (section == null) {
                                value = config.get(paramName);
                            } else {
                                value = instantiateObject(parameters[i].getType(), section, identifier);
                            }
                        } else if (parameters[i].isAnnotationPresent(StorageIdentifier.class)) {
                            value = identifier;
                        } else {
                            value = config.get(paramName);
                        }

                        if (value == null && paramAnnotation != null && !paramAnnotation.defaultValue().isEmpty()) {
                            value = convertValue(parameters[i].getType(), paramAnnotation.defaultValue());
                        }

                        values[i] = value;
                    }

                    return (T) constructor.newInstance(values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void closeConnection() {
        // No action needed for YAML
    }
}

