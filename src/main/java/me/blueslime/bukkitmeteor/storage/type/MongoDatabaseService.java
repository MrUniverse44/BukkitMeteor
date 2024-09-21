package me.blueslime.bukkitmeteor.storage.type;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.*;
import org.bson.Document;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import static com.mongodb.client.model.Filters.eq;

public class MongoDatabaseService extends StorageDatabase implements AdvancedModule {

    private final MongoDatabase database;

    public MongoDatabaseService(MongoDatabase database) {
        this.database = database;
        registerImpl(StorageDatabase.class, this, true);
    }

    @Override
    public CompletableFuture<Void> saveOrUpdateAsync(StorageObject obj) {
        return CompletableFuture.runAsync(() -> save(obj));
    }

    @Override
    public void saveOrUpdateSync(StorageObject obj) {
        save(obj);
    }

    private void save(StorageObject obj) {
        Class<?> clazz = obj.getClass();
        Document document = new Document();

        String identifierValue = null;
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(StorageIgnore.class)) {
                    continue;
                }
                Object value = field.get(obj);
                if (field.isAnnotationPresent(StorageIdentifier.class)) {
                    identifierValue = value.toString();
                }
                String name = field.getName();

                if (field.isAnnotationPresent(StorageKey.class)) {
                    StorageKey key = field.getAnnotation(StorageKey.class);
                    if (!key.key().isEmpty()) {
                        name = key.key();
                    }
                    if (value == null && !key.defaultValue().isEmpty()) {
                        value = convertValue(field.getType(), key.defaultValue());
                    }
                }
                document.append(name, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());

        if (identifierValue != null) {
            collection.replaceOne(eq("_id", identifierValue), document, new ReplaceOptions().upsert(true));
        } else {
            collection.insertOne(document);
        }
    }

    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());
            Document doc = collection.find(eq("_id", identifier)).first();

            if (doc != null) {
                return Optional.ofNullable(instantiateObject(clazz, doc));
            }
            return Optional.empty();
        });
    }

    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());
        Document doc = collection.find(eq("_id", identifier)).first();

        if (doc != null) {
            return Optional.ofNullable(instantiateObject(clazz, doc));
        }
        return Optional.empty();
    }

    @Override
    public <T extends StorageObject> CompletableFuture<Void> deleteByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.runAsync(() -> delete(clazz, identifier));
    }

    @Override
    public <T extends StorageObject> void deleteByIdSync(Class<T> clazz, String identifier) {
        delete(clazz, identifier);
    }

    private void delete(Class<?> clazz, String identifier) {
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName());
        collection.deleteOne(eq("_id", identifier));
    }

    @Override
    public void closeConnection() {
        /* Not needed for mongo. */
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<T> clazz, Document doc) {
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
                        values[i] = doc.get(paramName);
                        if (values[i] == null && paramAnnotation != null && !paramAnnotation.defaultValue().isEmpty()) {
                            values[i] = convertValue(parameters[i].getType(), paramAnnotation.defaultValue());
                        }
                    }

                    return (T) constructor.newInstance(values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

