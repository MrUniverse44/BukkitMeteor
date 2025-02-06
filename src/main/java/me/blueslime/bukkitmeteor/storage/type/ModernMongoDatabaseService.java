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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
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
        PluginConsumer.process(
            () -> {
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

                if (isComplexObject(field.getType())) {
                    value = handleComplexObject(value);
                }
                document.append(name, value);
            },
            e -> logError("Failed to save field: " + field.getName(), e)
        );
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

    @SuppressWarnings("unchecked")
    private Object[] resolveConstructorArgs(Constructor<?> constructor, Document document, String identifier) {
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String name = param.isAnnotationPresent(StorageKey.class) ? param.getAnnotation(StorageKey.class).key() : param.getName();
            String defValue = param.isAnnotationPresent(StorageKey.class) ? param.getAnnotation(StorageKey.class).defaultValue() : null;

            if (param.isAnnotationPresent(StorageIdentifier.class)) {
                args[i] = identifier;
            } else if (isComplexObject(param.getType())) {
                args[i] = instantiateObject(parameters[i].getType(), document.get(name, Document.class), identifier);
            } else {
                args[i] = document.get(name);

                if (args[i] == null && defValue != null && !defValue.isEmpty()) {
                    args[i] = convertValue(parameters[i].getType(), defValue);
                }
            }

            if (args[i] != null && List.class.isAssignableFrom(args[i].getClass())) {
                if (Set.class.isAssignableFrom(parameters[i].getType())) {
                    Class<?> setType = parameters[i].getType();
                    List<?> list = (List<?>) args[i];
                    if (HashSet.class.isAssignableFrom(setType)) {
                        args[i] = new HashSet<>(list);
                    } else if (LinkedHashSet.class.isAssignableFrom(setType)) {
                        args[i] = new LinkedHashSet<>(list);
                    } else if (TreeSet.class.isAssignableFrom(setType)) {
                        args[i] = new TreeSet<>(list);
                    } else {
                        try {
                            if (setType.equals(Set.class)) {
                                args[i] = new HashSet<>(list);
                            } else {
                                Set<Object> newSet = (Set<Object>) setType.getDeclaredConstructor().newInstance();
                                newSet.addAll(list);
                                args[i] = newSet;
                            }
                        } catch (Exception e) {
                            fetch(MeteorLogger.class).error(e, "Can't create a set instance of class: " + setType.getSimpleName() + ", please use another subclass of Set<> or other Collection method");
                        }
                    }
                }
            }
        }

        return args;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() {
        disconnect();
    }
}
