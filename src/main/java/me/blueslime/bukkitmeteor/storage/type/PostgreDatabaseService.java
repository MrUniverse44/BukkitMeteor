package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PostgreDatabaseService extends StorageDatabase implements AdvancedModule {

    private final Connection connection;

    public PostgreDatabaseService(Connection connection) {
        this.connection = connection;
        registerImpl(StorageDatabase.class, this, true);
        registerImpl(PostgreDatabaseService.class, this, true);
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
        String tableName = clazz.getSimpleName();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder updateValues = new StringBuilder();
        String identifierValue = null;
        String identifierColumn = "id";

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
                    identifierColumn = name; // Guardar el nombre de la columna que contiene el identificador
                }

                if (field.isAnnotationPresent(StorageKey.class)) {
                    StorageKey key = field.getAnnotation(StorageKey.class);
                    if (!key.key().isEmpty()) {
                        name = key.key(); // Usar nombre de clave personalizada
                    }
                    if (value == null && !key.defaultValue().isEmpty()) {
                        value = convertValue(field.getType(), key.defaultValue());
                    }
                }

                columns.append(name).append(",");
                values.append("'").append(value).append("',");

                updateValues.append(name).append(" = EXCLUDED.").append(name).append(",");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        columns.setLength(columns.length() - 1);
        values.setLength(values.length() - 1);
        updateValues.setLength(updateValues.length() - 1);

        String query;
        if (identifierValue != null) {
            query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ") "
                    + "ON CONFLICT (" + identifierColumn + ") DO UPDATE SET " + updateValues + ";";
        } else {
            query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ");";
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public <T extends StorageObject> CompletableFuture<Optional<T>> loadByIdAsync(Class<T> clazz, String identifier) {
        return CompletableFuture.supplyAsync(() -> load(clazz, identifier));
    }

    @Override
    public <T extends StorageObject> Optional<T> loadByIdSync(Class<T> clazz, String identifier) {
        return load(clazz, identifier);
    }

    private <T extends StorageObject> Optional<T> load(Class<T> clazz, String identifier) {
        String tableName = clazz.getSimpleName();
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, identifier);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.ofNullable(instantiateObject(clazz, resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        String tableName = clazz.getSimpleName();
        String query = "DELETE FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, identifier);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<T> clazz, ResultSet resultSet) {
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
                        values[i] = resultSet.getObject(paramName);
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

