package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.AdvancedModule;
import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class PostgreDatabaseService extends StorageDatabase implements AdvancedModule {

    private Connection connection;
    private final String password;
    private final String user;
    private final String url;

    /**
     * Initialize postgre service
     * @param url postgre url
     * @param user user
     * @param password password
     * @param register to register this connection to the Implements
     */
    public PostgreDatabaseService(String url, String user, String password, RegistrationType register) {
        this.password = password;
        this.user = user;
        this.url = url;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            registerImpl(PostgreDatabaseService.class, this, true);
        }

        if (register.isDouble()) {
            registerImpl(StorageDatabase.class, this, true);
        }
    }

    /**
     * Initialize postgre service
     * @param url postgre url
     * @param user user
     * @param password password
     * @param register to register this connection to the Implements
     * @param identifier used for the Implements in {@link Implements#fetch(Class, String)}
     */
    public PostgreDatabaseService(String url, String user, String password, RegistrationType register, String identifier) {
        this.password = password;
        this.user = user;
        this.url = url;

        boolean isSet = identifier != null;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            if (isSet) {
                registerImpl(PostgreDatabaseService.class, identifier, this, true);
            } else {
                registerImpl(PostgreDatabaseService.class, this, true);
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

    public void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
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
        if (connection == null) {
            throw new IllegalStateException("No connection established. Call connect() first.");
        }
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
                    identifierColumn = name;
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
                    saveComplexObject(value);
                    columns.append(name).append(",");
                    String fieldId = getComplexObjectId(value);
                    if (fieldId == null) {
                        fieldId = field.getName();
                    }
                    values.append("'").append(fieldId).append("',");
                } else {
                    columns.append(name).append(",");
                    values.append("'").append(value).append("',");

                    updateValues.append(name).append(" = EXCLUDED.").append(name).append(",");
                }

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

    private void saveComplexObject(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        String tableName = clazz.getSimpleName();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder updateValues = new StringBuilder();
        String identifierValue = null;
        String identifierColumn = "id";

        for (Field complexField : clazz.getDeclaredFields()) {
            complexField.setAccessible(true);
            if (complexField.isAnnotationPresent(StorageIgnore.class)) {
                continue;
            }

            Object value = complexField.get(obj);
            String name = complexField.getName();

            if (complexField.isAnnotationPresent(StorageIdentifier.class)) {
                identifierValue = value.toString();
                identifierColumn = name;
            }

            if (complexField.isAnnotationPresent(StorageKey.class)) {
                StorageKey key = complexField.getAnnotation(StorageKey.class);
                if (!key.key().isEmpty()) {
                    name = key.key();
                }
                if (value == null && !key.defaultValue().isEmpty()) {
                    value = convertValue(complexField.getType(), key.defaultValue());
                }
            }

            columns.append(name).append(",");
            values.append("'").append(value).append("',");

            updateValues.append(name).append(" = EXCLUDED.").append(name).append(",");
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

    private String getComplexObjectId(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(StorageIdentifier.class)) {
                    field.setAccessible(true);
                    return field.get(obj).toString();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
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
        if (connection == null) {
            throw new IllegalStateException("No connection established. Call connect() first.");
        }
        String tableName = clazz.getSimpleName();
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, identifier);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.ofNullable(instantiateObject(clazz, resultSet, identifier));
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

     private <T extends StorageObject> Set<T> loadAll(Class<T> clazz) {
         if (connection == null) {
             throw new IllegalStateException("No connection established. Call connect() first.");
         }
         String tableName = clazz.getSimpleName();
         String query = "SELECT * FROM " + tableName;

         Set<T> results = new HashSet<>();
         try (PreparedStatement statement = connection.prepareStatement(query)) {
             ResultSet resultSet = statement.executeQuery();

             while (resultSet.next()) {
                 T object = instantiateObject(clazz, resultSet, resultSet.getString("id"));
                 if (object != null) {
                     results.add(object);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return results;
     }

    @Override
    public <T extends StorageObject> CompletableFuture<Set<T>> loadAllAsync(Class<T> clazz) {
        return CompletableFuture.supplyAsync(
            () -> loadAll(clazz)
        );
    }

    @Override
    public <T extends StorageObject> Set<T> loadAllSync(Class<T> clazz) {
        return loadAll(clazz);
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
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, ResultSet resultSet, String identifier) {
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

                        Object paramValue;

                        if (isComplexObject(parameters[i].getType())) {
                            paramValue = instantiateComplexObject(parameters[i].getType(), resultSet, identifier);
                        } else {
                            if (parameters[i].isAnnotationPresent(StorageIdentifier.class)) {
                                paramValue = identifier;
                            } else {
                                paramValue = resultSet.getObject(paramName);
                            }
                        }

                        if (paramValue == null && paramAnnotation != null && !paramAnnotation.defaultValue().isEmpty()) {
                            paramValue = convertValue(parameters[i].getType(), paramAnnotation.defaultValue());
                        }

                        values[i] = paramValue;
                    }

                    return (T) constructor.newInstance(values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object instantiateComplexObject(Class<?> complexType, ResultSet resultSet, String identifier) {
        try {
            return instantiateObject(complexType, resultSet, identifier);  // Llamada recursiva para manejar objetos complejos
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

