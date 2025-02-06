package me.blueslime.bukkitmeteor.storage;

import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.utils.list.OptimizedList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class StorageDatabase implements Service {

    /**
     * Asynchronously loads an object by its identifier.
     *
     * @param clazz      The class type of the object.
     * @param identifier The unique identifier of the object.
     * @param <T>        The type of the storage object.
     * @return A CompletableFuture containing an Optional of the loaded object.
     */
    public abstract <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier);

    /**
     * Synchronously loads an object by its identifier.
     *
     * @param clazz      The class type of the object.
     * @param identifier The unique identifier of the object.
     * @param <T>        The type of the storage object.
     * @return An Optional containing the loaded object.
     */
    public abstract <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier);

    /**
     * Asynchronously deletes an object by its identifier.
     *
     * @param clazz      The class type of the object.
     * @param identifier The unique identifier of the object.
     * @param <T>        The type of the storage object.
     * @return A CompletableFuture representing the deletion process.
     */
    public abstract <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier);

    /**
     * Synchronously deletes an object by its identifier.
     *
     * @param clazz      The class type of the object.
     * @param identifier The unique identifier of the object.
     * @param <T>        The type of the storage object.
     */
    public abstract <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier);

    /**
     * Asynchronously loads all objects of a given type.
     *
     * @param clazz The class type of the objects.
     * @param <T>   The type of the storage object.
     * @return A CompletableFuture containing a set of all loaded objects.
     */
    public abstract <T extends StorageObject> CompletableFuture<Set<T>> loadAllAsync(Class<T> clazz);

    /**
     * Synchronously loads all objects of a given type.
     *
     * @param clazz The class type of the objects.
     * @param <T>   The type of the storage object.
     * @return A set containing all loaded objects.
     */
    public abstract <T extends StorageObject> Set<T> loadAllSync(Class<T> clazz);

    /**
     * Asynchronously saves or updates an object in the storage.
     *
     * @param obj The object to save or update.
     * @return A CompletableFuture representing the operation.
     */
    public abstract CompletableFuture<Void> saveOrUpdateAsync(StorageObject obj);

    /**
     * Synchronously saves or updates an object in the storage.
     *
     * @param obj The object to save or update.
     */
    public abstract void saveOrUpdateSync(StorageObject obj);

    /**
     * Establishes a connection to the storage database.
     */
    public abstract void connect();

    /**
     * Closes the connection to the storage database.
     */
    public abstract void closeConnection();

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object convertValue(Class<?> clazz, String value) {
        if (clazz == String.class) {
            return value;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return Integer.parseInt(value);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (clazz == Double.class || clazz == double.class) {
            return Double.parseDouble(value);
        }
        if (clazz == Float.class || clazz == float.class) {
            return Float.parseFloat(value);
        }
        if (clazz == Long.class || clazz == long.class) {
            return Long.parseLong(value);
        }
        if (clazz == Byte.class || clazz == byte.class) {
            return Byte.parseByte(value);
        }
        if (clazz == Short.class || clazz == short.class) {
            return Short.parseShort(value);
        }
        if (clazz == Character.class || clazz == char.class) {
            return value.charAt(0);
        }
        if (clazz == OptimizedList.class) {
            return new OptimizedList<>();
        }
        if (clazz == List.class || clazz == ArrayList.class) {
            return new ArrayList<>();
        }
        if (clazz == Set.class || clazz == HashSet.class) {
            return new HashSet<>();
        }
        if (clazz == LinkedList.class) {
            return new LinkedList<>();
        }
        if (clazz == Vector.class) {
            return new Vector();
        }
        if (clazz == ConcurrentHashMap.class) {
            return new ConcurrentHashMap<>();
        }
        if (clazz == CopyOnWriteArrayList.class) {
            return new CopyOnWriteArrayList<>();
        }
        if (clazz == Map.class || clazz == HashMap.class) {
            return new HashMap<>();
        }
        if (clazz == BigInteger.class) {
            return new BigInteger(value);
        }
        if (clazz == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (clazz.isEnum()) {
            return Enum.valueOf((Class<Enum>)clazz, value);
        }
        return value;
    }

    protected void logError(String message, Exception e) {
        fetch(MeteorLogger.class).error(e, message);
    }

    protected boolean isComplexObject(Class<?> clazz) {
        if (!clazz.isPrimitive() && !clazz.getName().startsWith("java.lang")) {
            boolean annotatedFields = Arrays.stream(clazz.getDeclaredFields()).anyMatch(
                    field -> field.isAnnotationPresent(StorageKey.class)
            );
            boolean annotatedConstructor = Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(
                    constructor -> constructor.isAnnotationPresent(StorageConstructor.class)
            );
            return annotatedFields || annotatedConstructor;
        }
        return false;
    }
}
