package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.implementation.Implements;
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
public class YamlDatabaseService extends StorageDatabase {

    private final File dataFolder;

    /**
     * Inicializa la conexión con la base de datos YAML.
     * @param register para registrar esta conexión en Implements
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
     * Inicializa la conexión con la base de datos YAML.
     * @param register para registrar esta conexión en Implements
     * @param identifier usado para la búsqueda en {@link Implements#fetch(Class, String)}
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

    /**
     * Guarda o actualiza un objeto de forma asíncrona.
     */
    @Override
    public CompletableFuture<Void> saveOrUpdateAsync(StorageObject obj) {
        return CompletableFuture.runAsync(() -> save(obj));
    }

    /**
     * Guarda o actualiza un objeto de forma síncrona.
     */
    @Override
    public void saveOrUpdateSync(StorageObject obj) {
        save(obj);
    }

    /**
     * Establishes a connection to the storage database.
     */
    @Override
    public void connect() {

    }

    private void save(StorageObject obj) {
        Class<?> clazz = obj.getClass();

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
                    if (value == null) {
                        throw new IllegalArgumentException("Field @StorageIdentifier can't be null.");
                    }
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
                logError("Can't save field: " + field.getName(), e);
            }
        }

        if (identifierValue == null) {
            throw new IllegalArgumentException("Object from class " + clazz.getSimpleName() + " don't have @StorageIdentifier.");
        }

        File classFolder = new File(dataFolder, clazz.getSimpleName());
        if (!classFolder.exists()) {
            classFolder.mkdirs();
        }

        File file = new File(classFolder, identifierValue + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        addMap.forEach(config::set);

        try {
            config.save(file);
        } catch (IOException e) {
            logError("Can't save file for object class: " + obj.getClass().getSimpleName(), e);
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
                logError("Can't get data from field: " + field.getName(), e);
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

    /**
     * Carga asíncrona por identificador.
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.supplyAsync(() -> loadByIdSync(clazz, identifier));
    }

    /**
     * Carga síncrona por identificador.
     */
    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        File classFolder = new File(dataFolder, clazz.getSimpleName());
        File file = new File(classFolder, identifier + ".yml");
        if (!file.exists()) {
            return Optional.empty();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        T object = instantiateObject(clazz, config, identifier);
        return Optional.ofNullable(object);
    }

    /**
     * Elimina asíncronamente por identificador.
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.runAsync(() -> delete(clazz, identifier));
    }

    /**
     * Elimina síncronamente por identificador.
     */
    @Override
    public <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier) {
        delete(clazz, identifier);
    }

    public <T extends StorageObject> void delete(Class<T> clazz, String identifier) {
        File classFolder = new File(dataFolder, clazz.getSimpleName());
        File file = new File(classFolder, identifier + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    private <T extends StorageObject> Set<T> loadAll(Class<T> clazz) {
        Set<T> set = new HashSet<>();
        File classFolder = new File(dataFolder, clazz.getSimpleName());
        if (!classFolder.exists()) {
            return set;
        }

        File[] files = classFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                // Se asume que el nombre del archivo (sin extensión) es el identificador
                String fileName = file.getName();
                String identifier = fileName.substring(0, fileName.lastIndexOf('.'));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                T object = instantiateObject(clazz, config, identifier);
                if (object != null) {
                    set.add(object);
                }
            }
        }

        return set;
    }

    /**
     * Carga asíncrona de todos los objetos.
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Set<T>> loadAllAsync(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> loadAll(clazz));
    }

    /**
     * Carga síncrona de todos los objetos.
     */
    @Override
    public <T extends StorageObject> Set<T> loadAllSync(Class<T> clazz) {
        return loadAll(clazz);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
       MÉTODOS DE INSTANCIACIÓN Y CONVERSIÓN (actualizados para imitar la versión ModernMongoDatabaseService)
       ────────────────────────────────────────────────────────────────────────────── */

    /**
     * Resuelve los argumentos del constructor anotado con @StorageConstructor
     * a partir de la configuración YAML.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object[] resolveConstructorArgs(Constructor<?> constructor, ConfigurationSection config, String identifier) {
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            StorageKey keyAnnotation = param.getAnnotation(StorageKey.class);
            String paramName = (keyAnnotation != null && !keyAnnotation.key().isEmpty())
                    ? keyAnnotation.key()
                    : param.getName();
            String defaultValue = (keyAnnotation != null ? keyAnnotation.defaultValue() : null);

            Object value = null;
            try {
                if (param.isAnnotationPresent(StorageIdentifier.class)) {
                    value = identifier;
                } else if (isComplexObject(param.getType())) {
                    ConfigurationSection section = config.getConfigurationSection(paramName);
                    if (section != null) {
                        value = instantiateObject(param.getType(), section, identifier);
                    } else {
                        value = config.get(paramName);
                    }
                } else {
                    value = config.get(paramName);
                }

                if (value == null && defaultValue != null && !defaultValue.isEmpty()) {
                    value = convertValue(param.getType(), defaultValue);
                }

                // Conversión para float: si se espera float y se obtiene Double, se convierte.
                if ((param.getType().equals(Float.class) || param.getType().equals(float.class)) && value instanceof Double) {
                    value = ((Double) value).floatValue();
                }

                // Conversión para enums.
                if (param.getType().isEnum() && value instanceof String) {
                    try {
                        value = Enum.valueOf((Class<Enum>) param.getType(), (String) value);
                    } catch(Exception e) {
                        logError("Can't find enum'" + value + "' from field " + paramName, e);
                    }
                }

                // Conversión para arrays si el valor es un List.
                if (param.getType().isArray() && value instanceof List<?> list) {
                    Object array = Array.newInstance(param.getType().getComponentType(), list.size());
                    for (int j = 0; j < list.size(); j++) {
                        Object element = list.get(j);
                        if ((param.getType().getComponentType().equals(Float.class) || param.getType().getComponentType().equals(float.class)) && element instanceof Double) {
                            element = ((Double) element).floatValue();
                        }
                        if (isComplexObject(param.getType().getComponentType()) && element instanceof ConfigurationSection) {
                            element = instantiateObject(param.getType().getComponentType(), (ConfigurationSection) element, identifier);
                        }
                        Array.set(array, j, element);
                    }
                    value = array;
                }

                // Conversión para colecciones si el valor es un List.
                if (Collection.class.isAssignableFrom(param.getType()) && value instanceof List<?> list) {
                    Collection<Object> collection;
                    if (Set.class.isAssignableFrom(param.getType())) {
                        collection = new HashSet<>();
                    } else {
                        collection = new ArrayList<>();
                    }
                    for (Object element : list) {
                        if ((param.getType().equals(Float.class) || param.getType().equals(float.class)) && element instanceof Double) {
                            element = ((Double) element).floatValue();
                        }
                        if (element instanceof ConfigurationSection) {
                            element = instantiateObject(param.getType(), (ConfigurationSection) element, identifier);
                        }
                        collection.add(element);
                    }
                    value = collection;
                }
            } catch (Exception e) {
                logError("Error resolving parameter: " + paramName, e);
            }
            args[i] = value;
        }
        return args;
    }

    /**
     * Instancia un objeto de la clase especificada a partir de la configuración YAML.
     */
    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, ConfigurationSection config, String identifier) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.isAnnotationPresent(StorageConstructor.class)) {
                try {
                    Object[] args = resolveConstructorArgs(constructor, config, identifier);
                    return (T) constructor.newInstance(args);
                } catch (Exception e) {
                    logError("Can't instance YAML instance: " + clazz.getSimpleName(), e);
                }
            }
        }
        logError("Can't find constructor with @StorageConstructor at: " + clazz.getSimpleName(), null);
        return null;
    }

    @Override
    public void closeConnection() {
        // No se requiere acción para YAML.
    }
}
