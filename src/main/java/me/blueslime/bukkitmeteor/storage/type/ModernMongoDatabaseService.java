package me.blueslime.bukkitmeteor.storage.type;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.*;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bson.Document;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("unused")
public class ModernMongoDatabaseService extends StorageDatabase {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private final String databaseName;
    private final String uri;

    /**
     * Create your mongo database connection
     * @param uri to connect
     * @param databaseName for this session
     * @param register to the implements
     */
    public ModernMongoDatabaseService(String uri, String databaseName, RegistrationType register) {
        this(uri, databaseName, register, null);
    }

    /**
     * Create your mongo database connection
     * @param uri to connect
     * @param databaseName for this session
     * @param register to the implements
     * @param identifier for the implements
     */
    public ModernMongoDatabaseService(String uri, String databaseName, RegistrationType register, String identifier) {
        this.databaseName = databaseName;
        this.uri = uri;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            registerImpl(ModernMongoDatabaseService.class, identifier, this, true);
        }

        if (register.isDouble()) {
            registerImpl(StorageDatabase.class, identifier, this, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public MongoDatabase getDatabase() {
        return database;
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

    private void save(StorageObject obj) {
        ensureDatabaseConnected();

        Document document = createDocumentFromObject(obj);
        String identifierValue = extractIdentifier(obj);

        MongoCollection<Document> collection = database.getCollection(obj.getClass().getSimpleName());
        ReplaceOptions options = new ReplaceOptions().upsert(true);

        if (identifierValue != null) {
            collection.replaceOne(eq("_id", identifierValue), document, options);
        } else {
            collection.insertOne(document);
        }
    }

    private Document createDocumentFromObject(Object obj) {
        Document document = new Document();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIgnore.class)) {
                continue;
            }

            field.setAccessible(true);
            saveField(document, obj, field);
        }
        return document;
    }

    private String extractIdentifier(StorageObject obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIdentifier.class)) {
                field.setAccessible(true);
                return PluginConsumer.ofUnchecked(
                    () -> {
                        Object value = field.get(obj);
                        if (value == null) {
                            return null;
                        }
                        return value.toString();
                    },
                    e -> logError("Failed to extract identifier from: " + field.getName(), e),
                    () -> null
                );
            }
        }
        return null;
    }

    private void saveField(Document document, Object obj, Field field) {
        PluginConsumer.process(() -> {
            Object value = field.get(obj);
            String name = field.getName();

            if (field.isAnnotationPresent(StorageKey.class)) {
                StorageKey key = field.getAnnotation(StorageKey.class);
                String defValue = key.defaultValue();
                String defName = key.key();

                if (value == null && !defValue.isEmpty()) {
                    value = convertValue(field.getType(), defValue);
                }
                if (!defName.isEmpty()) {
                    name = defName;
                }
            }

            if (value != null && value.getClass().isArray()) {
                int length = Array.getLength(value);
                List<Object> listToStore = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(value, i);
                    if (element != null && isComplexObject(element.getClass())) {
                        listToStore.add(createDocumentFromObject(element));
                    } else {
                        listToStore.add(element);
                    }
                }
                value = listToStore;
            }

            if (value != null && value.getClass().isEnum()) {
                value = value.toString();
            }

            if (value instanceof Map<?, ?> originalMap) {
                Map<Object, Object> convertedMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
                    Object mapKey = entry.getKey();
                    Object mapValue = entry.getValue();

                    if (mapKey != null && isComplexObject(mapKey.getClass())) {
                        mapKey = createDocumentFromObject(mapKey);
                    }
                    if (mapValue != null && isComplexObject(mapValue.getClass())) {
                        mapValue = createDocumentFromObject(mapValue);
                    }
                    convertedMap.put(mapKey, mapValue);
                }
                value = convertedMap;
            }

            if (value instanceof Collection<?> collection) {
                List<Object> listToStore = new ArrayList<>();
                for (Object element : collection) {
                    if (element != null && isComplexObject(element.getClass())) {
                        listToStore.add(createDocumentFromObject(element));
                    } else {
                        listToStore.add(element);
                    }
                }
                value = listToStore;
            } else if (isComplexObject(field.getType())) {
                value = handleComplexObject(value);
            }

            document.append(name, value);
        }, e -> logError("Failed to save field: " + field.getName(), e));
    }

    private Document handleComplexObject(Object obj) {
        if (obj == null) {
            return null;
        }

        return createDocumentFromObject(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        ensureDatabaseConnected();

        return CompletableFuture.supplyAsync(() -> loadByIdSync(clazz, identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        ensureDatabaseConnected();

        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());
        Document document = collection.find(eq("_id", identifier)).first();

        return Optional.ofNullable(document != null ? instantiateObject(clazz, document, identifier) : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.runAsync(() -> deleteByIdSync(clazz, identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier) {
        ensureDatabaseConnected();

        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());
        collection.deleteOne(eq("_id", identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> CompletableFuture<Set<T>> loadAllAsync(Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> loadAllSync(clazz));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends StorageObject> Set<T> loadAllSync(Class<T> clazz) {
        ensureDatabaseConnected();

        Set<T> results = new HashSet<>();
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());

        for (Document document : collection.find()) {
            T obj = instantiateObject(clazz, document, document.getString("_id"));
            if (obj != null) {
                results.add(obj);
            }
        }

        return results;
    }

    private void ensureDatabaseConnected() {
        if (database == null) {
            throw new IllegalStateException("No database connection. Call connect() first.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, Document document, String identifier) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.isAnnotationPresent(StorageConstructor.class)) {
                try {
                    Object[] args = resolveConstructorArgs(constructor, document, identifier);
                    return (T) constructor.newInstance(args);
                } catch (Exception e) {
                    logError("Failed to instantiate object: " + clazz.getSimpleName(), e);
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object[] resolveConstructorArgs(Constructor<?> constructor, Document document, String identifier) {
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String name = param.isAnnotationPresent(StorageKey.class)
                    ? param.getAnnotation(StorageKey.class).key()
                    : param.getName();
            String defValue = param.isAnnotationPresent(StorageKey.class)
                    ? param.getAnnotation(StorageKey.class).defaultValue()
                    : null;

            if (param.isAnnotationPresent(StorageIdentifier.class)) {
                args[i] = identifier;
            } else if (isComplexObject(param.getType())) {
                args[i] = instantiateObject(param.getType(), document.get(name, Document.class), identifier);
            } else {
                args[i] = document.get(name);
                if ((param.getType().equals(Float.class) || param.getType().equals(float.class))
                        && args[i] instanceof Double) {
                    args[i] = ((Double) args[i]).floatValue();
                }
                if (args[i] == null && defValue != null && !defValue.isEmpty()) {
                    args[i] = convertValue(param.getType(), defValue);
                }
            }

            // Manejo de arrays
            if (args[i] != null && param.getType().isArray() && List.class.isAssignableFrom(args[i].getClass())) {
                List<?> list = (List<?>) args[i];
                List<Object> result = new ArrayList<>();
                Class<?> componentType = param.getType().getComponentType();
                for (Object element : list) {
                    if (element != null && isComplexObject(componentType)) {
                        if (element instanceof Document) {
                            // Usamos el componente esperado en lugar de element.getClass()
                            result.add(createObjectFromDocument((Document) element, componentType, identifier));
                        } else {
                            result.add(element);
                        }
                    }
                }
                args[i] = result.toArray((Object[]) Array.newInstance(componentType, result.size()));
            }

            // Manejo de Maps
            if (args[i] != null && Map.class.isAssignableFrom(param.getType())) {
                Object stored = args[i];
                Map<Object, Object> reconstructedMap = createMap(param.getType());

                // Extraemos los tipos genéricos para clave y valor
                Class<?> expectedKeyType = Object.class;
                Class<?> expectedValueType = Object.class;
                Type genericType = constructor.getGenericParameterTypes()[i];
                if (genericType instanceof ParameterizedType) {
                    Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                    if (typeArguments != null && typeArguments.length == 2) {
                        if (typeArguments[0] instanceof Class) {
                            expectedKeyType = (Class<?>) typeArguments[0];
                        }
                        if (typeArguments[1] instanceof Class) {
                            expectedValueType = (Class<?>) typeArguments[1];
                        }
                    }
                }

                if (stored instanceof Document mapDoc) {
                    for (String key : mapDoc.keySet()) {
                        Object mapValue = mapDoc.get(key);
                        if (mapValue instanceof Document && isComplexObject(expectedValueType)) {
                            mapValue = createObjectFromDocument((Document) mapValue, expectedValueType, identifier);
                        }
                        reconstructedMap.put(key, mapValue);
                    }
                } else if (stored instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) stored).entrySet()) {
                        Object mapKey = entry.getKey();
                        Object mapValue = entry.getValue();
                        if (mapKey instanceof Document && isComplexObject(expectedKeyType)) {
                            mapKey = createObjectFromDocument((Document) mapKey, expectedKeyType, identifier);
                        }
                        if (mapValue instanceof Document && isComplexObject(expectedValueType)) {
                            mapValue = createObjectFromDocument((Document) mapValue, expectedValueType, identifier);
                        }
                        reconstructedMap.put(mapKey, mapValue);
                    }
                }
                args[i] = reconstructedMap;
            }

            // Manejo de Enum
            if (args[i] != null && param.getType().isEnum() && args[i] instanceof String finalValue) {
                Class<? extends Enum> enumType = param.getType().asSubclass(Enum.class);
                args[i] = PluginConsumer.ofUnchecked(
                        () -> Enum.valueOf(enumType, finalValue),
                        e -> logError("Can't found enum constant for " + finalValue + " please contact the developer.", e),
                        () -> null
                );
            }

            // Manejo de Collections
            if (args[i] != null && Collection.class.isAssignableFrom(args[i].getClass())) {
                if (!Collection.class.isAssignableFrom(param.getType())) {
                    args[i] = null;
                    fetch(MeteorLogger.class).error(
                            "Required parameter for StorageConstructor is: " + param.getType().getSimpleName(),
                            "This parameter is using Storage Key: " + name,
                            "But in the object storage this key contains a Collection and the parameter is not a collection",
                            "By default we are not gonna assign value, so the object with field key: " + name + ", will be null",
                            "This is an error from your developer, please contact it to fix this."
                    );
                } else {
                    // Extraemos el tipo esperado para los elementos de la Collection
                    Class<?> expectedElementType = Object.class;
                    Type genericType = constructor.getGenericParameterTypes()[i];
                    if (genericType instanceof ParameterizedType) {
                        Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                        if (typeArguments != null && typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                            expectedElementType = (Class<?>) typeArguments[0];
                        }
                    }
                    Object finalValue = null;
                    Collection<?> collection = (Collection<?>) args[i];
                    if (List.class.isAssignableFrom(args[i].getClass())) {
                        List<Object> array = new ArrayList<>();
                        for (Object element : collection) {
                            if (element != null && isComplexObject(expectedElementType)) {
                                if (element instanceof Document) {
                                    array.add(createObjectFromDocument((Document) element, expectedElementType, identifier));
                                } else {
                                    array.add(element);
                                }
                            } else {
                                array.add(element);
                            }
                        }
                        finalValue = array;
                    } else if (Set.class.isAssignableFrom(args[i].getClass())) {
                        Set<Object> array = new HashSet<>();
                        for (Object element : collection) {
                            if (element != null && isComplexObject(expectedElementType)) {
                                if (element instanceof Document) {
                                    array.add(createObjectFromDocument((Document) element, expectedElementType, identifier));
                                } else {
                                    array.add(element);
                                }
                            } else {
                                array.add(element);
                            }
                        }
                        finalValue = array;
                    } else {
                        if (defValue != null) {
                            args[i] = convertValue(param.getType(), defValue);
                        }
                    }
                    args[i] = finalValue != null ? finalValue
                            : defValue != null ? convertValue(param.getType(), defValue) : null;
                }
            }
        }
        return args;
    }

    private <T> List<T> loadList(Document document, Field field, Class<T> elementClass, String identifier) {
        List<T> result = new ArrayList<>();
        String name = field.getName();

        if (field.isAnnotationPresent(StorageKey.class)) {
            StorageKey key = field.getAnnotation(StorageKey.class);
            String defName = key.key();
            if (!defName.isEmpty()) {
                name = defName;
            }
        }

        List<?> listFromDocument = document.getList(name, Object.class);
        if (listFromDocument != null) {
            for (Object element : listFromDocument) {
                if (element instanceof Document && isComplexObject(elementClass)) {
                    T obj = createObjectFromDocument((Document) element, elementClass, identifier);
                    result.add(obj);
                } else {
                    result.add(elementClass.cast(element));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T createObjectFromDocument(Document document, Class<T> clazz, String identifier) {
        Constructor<?> builder = PluginConsumer.ofUnchecked(
            () -> {
                for (Constructor<?> constructor : clazz.getConstructors()) {
                    if (constructor.isAnnotationPresent(StorageConstructor.class)) {
                        return constructor;
                    }
                }
                return null;
            },
            e -> logError("Can't find @StorageConstructor at class: " + clazz.getSimpleName(), e),
            () -> null
        );

        Object[] args = resolveConstructorArgs(builder, document, identifier);
        return PluginConsumer.ofUnchecked(
            () -> (T) builder.newInstance(args),
            e -> logError("Can't create instance of class: " + clazz.getSimpleName(), e),
            () -> null
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() {
        disconnect();
    }
}
