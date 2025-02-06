package me.blueslime.bukkitmeteor.storage.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIdentifier;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIgnore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class JsonDatabaseService extends StorageDatabase {

    private final File dataFolder;
    private final Gson gson;

    /**
     * Json Database
     *
     * @param register to the implements
     */
    public JsonDatabaseService(RegistrationType register) {
        this.dataFolder = new File(fetch(File.class, "folder"), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }
        if (register.isDouble() || register.isOnlyThis()) {
            registerImpl(JsonDatabaseService.class, this, true);
        }
        if (register.isDouble()) {
            registerImpl(StorageDatabase.class, this, true);
        }
    }

    /**
     * Json Database
     *
     * @param register   to the implements.
     * @param identifier for the implements {@link Implements#fetch(Class, String)}
     */
    public JsonDatabaseService(RegistrationType register, String identifier) {
        this.dataFolder = new File(fetch(File.class, "folder"), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        boolean isSet = identifier != null;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }
        if (register.isDouble() || register.isOnlyThis()) {
            if (isSet) {
                registerImpl(JsonDatabaseService.class, identifier, this, true);
            } else {
                registerImpl(JsonDatabaseService.class, this, true);
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
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> saveOrUpdateAsync(StorageObject obj) {
        return CompletableFuture.runAsync(() -> save(obj));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveOrUpdateSync(StorageObject obj) {
        save(obj);
    }

    /**
     * No se requiere acción para la conexión en JSON.
     */
    @Override
    public void connect() { }

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
                logError("Can't save field " + field.getName() + " of the object.", e);
            }
        }

        if (identifierValue == null) {
            throw new IllegalArgumentException("Object from class " + clazz.getSimpleName() + " don't have @StorageIdentifier.");
        }

        File classFolder = new File(dataFolder, clazz.getSimpleName());
        if (!classFolder.exists()) {
            classFolder.mkdirs();
        }

        File file = new File(classFolder, identifierValue + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(addMap, writer);
        } catch (IOException e) {
            logError("Can't save json object.", e);
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
                logError("Can't get data from field " + field.getName(), e);
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
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.supplyAsync(() -> loadByIdSync(clazz, identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        File classFolder = new File(dataFolder, clazz.getSimpleName());
        File file = new File(classFolder, identifier + ".json");
        if (!file.exists()) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> jsonMap = gson.fromJson(reader, type);
            T object = instantiateObject(clazz, jsonMap, identifier);
            return Optional.ofNullable(object);
        } catch (IOException e) {
            logError("Can't load json object.", e);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.runAsync(() -> delete(clazz, identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier) {
        delete(clazz, identifier);
    }

    public <T extends StorageObject> void delete(Class<T> clazz, String identifier) {
        File classFolder = new File(dataFolder, clazz.getSimpleName());
        File file = new File(classFolder, identifier + ".json");
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
        File[] files = classFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String identifier = fileName.substring(0, fileName.lastIndexOf('.'));
                try (FileReader reader = new FileReader(file)) {
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> jsonMap = gson.fromJson(reader, type);
                    T object = instantiateObject(clazz, jsonMap, identifier);
                    if (object != null) {
                        set.add(object);
                    }
                } catch (IOException e) {
                    logError("Can't load json object: " + file.getName(), e);
                }
            }
        }
        return set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Set<T>> loadAllAsync(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> loadAll(clazz));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> Set<T> loadAllSync(Class<T> clazz) {
        return loadAll(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, Map<String, Object> jsonMap, String identifier) {
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
                        if (parameters[i].isAnnotationPresent(StorageIdentifier.class)) {
                            value = identifier;
                        } else if (isComplexObject(parameters[i].getType())) {
                            Object section = jsonMap.get(paramName);
                            if (section instanceof Map) {
                                value = instantiateObject(parameters[i].getType(), (Map<String, Object>) section, identifier);
                            } else {
                                value = section;
                            }
                        } else {
                            value = jsonMap.get(paramName);
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
            logError("Can't instantiate json object.", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() {

    }
}

