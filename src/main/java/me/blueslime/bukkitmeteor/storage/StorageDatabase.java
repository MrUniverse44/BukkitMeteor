package me.blueslime.bukkitmeteor.storage;

import me.blueslime.bukkitmeteor.implementation.module.Service;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.utils.list.OptimizedList;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import me.blueslime.utilitiesapi.utils.executable.PluginExecutable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StorageDatabase implements Service {

    private final Map<Class<?>, Function<Collection<?>, ?>> collections = new ConcurrentHashMap<>();
    private final Map<Class<?>, PluginConsumer.PluginExecutableConsumer<Map<Object, Object>>> mapCreator = new ConcurrentHashMap<>();
    private final Map<Class<?>, Function<String, ?>> converters = new ConcurrentHashMap<>();

    public StorageDatabase() {
        converters.put(String.class, s -> s);
        converters.put(Integer.class, Integer::parseInt);
        converters.put(int.class, Integer::parseInt);
        converters.put(Boolean.class, Boolean::parseBoolean);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Double.class, Double::parseDouble);
        converters.put(double.class, Double::parseDouble);
        converters.put(Float.class, Float::parseFloat);
        converters.put(float.class, Float::parseFloat);
        converters.put(Long.class, Long::parseLong);
        converters.put(long.class, Long::parseLong);
        converters.put(Byte.class, Byte::parseByte);
        converters.put(byte.class, Byte::parseByte);
        converters.put(Short.class, Short::parseShort);
        converters.put(short.class, Short::parseShort);
        converters.put(Character.class, s -> s.charAt(0));
        converters.put(char.class, s -> s.charAt(0));
        converters.put(OptimizedList.class, s -> new OptimizedList<>());
        converters.put(List.class, s -> new ArrayList<>());
        converters.put(ArrayList.class, s -> new ArrayList<>());
        converters.put(Set.class, s -> new HashSet<>());
        converters.put(HashSet.class, s -> new HashSet<>());
        converters.put(LinkedList.class, s -> new LinkedList<>());
        converters.put(Vector.class, s -> new Vector<>());
        converters.put(ConcurrentHashMap.class, s -> new ConcurrentHashMap<>());
        converters.put(CopyOnWriteArrayList.class, s -> new CopyOnWriteArrayList<>());
        converters.put(Map.class, s -> new HashMap<>());
        converters.put(HashMap.class, s -> new HashMap<>());
        converters.put(BigInteger.class, BigInteger::new);
        converters.put(BigDecimal.class, BigDecimal::new);

        collections.put(ArrayList.class, ArrayList::new);
        collections.put(LinkedList.class, LinkedList::new);
        collections.put(Set.class, HashSet::new);
        collections.put(HashSet.class, HashSet::new);
        collections.put(CopyOnWriteArrayList.class, CopyOnWriteArrayList::new);
        collections.put(TreeSet.class, TreeSet::new);
        collections.put(OptimizedList.class, OptimizedList::new);

        mapCreator.put(HashMap.class, HashMap::new);
        mapCreator.put(Map.class, HashMap::new);
        mapCreator.put(ConcurrentHashMap.class, ConcurrentHashMap::new);
    }

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

    public Map<Object, Object> createMap(Class<?> clazz) {
        return mapCreator.getOrDefault(clazz, HashMap::new).accept();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object convertValue(Class<?> clazz, String value) {
        Function<String, ?> converter = converters.get(clazz);
        if (converter != null) {
            return converter.apply(value);
        }

        if (clazz.isEnum()) {
            return PluginConsumer.ofUnchecked(
                () -> Enum.valueOf((Class<Enum>) clazz, value),
                e -> logError("Can't find enum value for class: " + clazz.getSimpleName() + " value: " + value, e),
                () -> null
            );
        }
        return value;
    }

    public Object convertCollection(Class<? extends Collection<?>> clazz, Collection<?> value) {
        Function<Collection<?>, ?> converter = collections.get(clazz);
        if (converter != null) {
            return converter.apply(value);
        }

        return null;
    }

    public void registerDefaultValueConverter(Class<?> key, Function<String, ?> converter) {
        converters.put(key, converter);
    }

    public void registerCollectionConverter(Class<? extends Collection<?>> key, Function<Collection<?>, ?> converter) {
        collections.put(key, converter);
    }

    public void unregisterDefaultValueConverter(Class<?> clazz) {
        converters.remove(clazz);
    }

    public void unregisterCollectionConverter(Class<? extends Collection<?>> clazz) {
        collections.remove(clazz);
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
