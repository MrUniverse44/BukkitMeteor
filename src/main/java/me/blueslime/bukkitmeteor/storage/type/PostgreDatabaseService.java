package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIdentifier;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIgnore;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class PostgreDatabaseService extends StorageDatabase {

    private Connection connection;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Create your postgre connection
     * @param url to connect
     * @param user of session
     * @param password of session
     * @param register to the implements
     */
    public PostgreDatabaseService(String url, String user, String password, RegistrationType register) {
        this(url, user, password, register, null);
    }

    /**
     * Create your postgre connection
     * @param url to connect
     * @param user of session
     * @param password of session
     * @param register to the implements
     * @param identifier for the implements
     */
    public PostgreDatabaseService(String url, String user, String password, RegistrationType register, String identifier) {
        this.url = url;
        this.user = user;
        this.password = password;

        if (register == null) {
            register = RegistrationType.DONT_REGISTER;
        }

        if (register.isDouble() || register.isOnlyThis()) {
            registerImpl(PostgreDatabaseService.class, identifier, this, true);
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
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logError("Error connecting to the database", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logError("Error closing database connection", e);
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

    private void save(StorageObject obj) {
        ensureDatabaseConnected();

        Map<String, Object> columns = createColumnsFromObject(obj);
        String identifierValue = extractIdentifier(obj);
        String tableName = obj.getClass().getSimpleName();

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        List<String> colNames = new ArrayList<>(columns.keySet());
        sql.append(String.join(", ", colNames));
        sql.append(") VALUES (");
        sql.append(String.join(", ", Collections.nCopies(colNames.size(), "?")));
        sql.append(")");

        if (identifierValue != null) {
            sql.append(" ON CONFLICT (_id) DO UPDATE SET ");
            List<String> updates = new ArrayList<>();
            for (String col : colNames) {
                if (col.equals("_id")) continue;
                updates.add(col + " = EXCLUDED." + col);
            }
            sql.append(String.join(", ", updates));
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int i = 1;
            for (String col : colNames) {
                stmt.setObject(i++, columns.get(col));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Error when the plugin was saving/updating an object", e);
        }
    }

    private Map<String, Object> createColumnsFromObject(Object obj) {
        Map<String, Object> columns = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIgnore.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String columnName = field.getName();

                if (field.isAnnotationPresent(StorageKey.class)) {
                    StorageKey key = field.getAnnotation(StorageKey.class);
                    if (!key.key().isEmpty()) {
                        columnName = key.key();
                    }
                    if (value == null && !key.defaultValue().isEmpty()) {
                        value = convertValue(field.getType(), key.defaultValue());
                    }
                }

                if (isComplexObject(field.getType())) {
                    value = handleComplexObject(value);
                }

                columns.put(columnName, value);
            } catch (Exception e) {
                logError("Error processing field: " + field.getName(), e);
            }
        }
        return columns;
    }

    private Object handleComplexObject(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    private String extractIdentifier(StorageObject obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIdentifier.class)) {
                field.setAccessible(true);
                return PluginConsumer.ofUnchecked(
                        () -> {
                            Object value = field.get(obj);
                            return value != null ? value.toString() : null;
                        },
                        e -> logError("Error extracting field value of: " + field.getName(), e),
                        () -> null
                );
            }
        }
        return null;
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
        String tableName = clazz.getSimpleName();
        String sql = "SELECT * FROM " + tableName + " WHERE _id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T obj = instantiateObject(clazz, rs, identifier);
                    return Optional.ofNullable(obj);
                }
            }
        } catch (SQLException e) {
            logError("Error at loading an object id", e);
        }
        return Optional.empty();
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
        String tableName = clazz.getSimpleName();
        String sql = "DELETE FROM " + tableName + " WHERE _id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("Error when plugin was deleting data from database", e);
        }
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
        String tableName = clazz.getSimpleName();
        String sql = "SELECT * FROM " + tableName;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("_id");
                T obj = instantiateObject(clazz, rs, id);
                if (obj != null) {
                    results.add(obj);
                }
            }
        } catch (SQLException e) {
            logError("Error when loading all objects", e);
        }
        return results;
    }

    private void ensureDatabaseConnected() {
        if (connection == null) {
            throw new IllegalStateException("Not connected to the database.");
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
            logError("Can't instantiate object", e);
        }
        return null;
    }

    private Object instantiateComplexObject(Class<?> complexType, ResultSet resultSet, String identifier) {
        try {
            return instantiateObject(complexType, resultSet, identifier);
        } catch (Exception e) {
            logError("Can't instantiate complex object", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() {
        disconnect();
    }
}
