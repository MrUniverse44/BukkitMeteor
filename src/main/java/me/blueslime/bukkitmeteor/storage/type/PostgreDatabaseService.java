package me.blueslime.bukkitmeteor.storage.type;

import me.blueslime.bukkitmeteor.storage.StorageDatabase;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageConstructor;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIdentifier;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageKey;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageObject;
import me.blueslime.bukkitmeteor.storage.interfaces.StorageIgnore;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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
     * Crea la conexión a PostgreSQL.
     * @param url para conectarse.
     * @param user de la sesión.
     * @param password de la sesión.
     * @param register para Implements.
     */
    public PostgreDatabaseService(String url, String user, String password, RegistrationType register) {
        this(url, user, password, register, null);
    }

    /**
     * Crea la conexión a PostgreSQL.
     * @param url para conectarse.
     * @param user de la sesión.
     * @param password de la sesión.
     * @param register para Implements.
     * @param identifier para Implements.
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
            logError("Error while connecting to the PostgreSQL", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logError("Error closing connection with PostgreSQL", e);
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
            logError("Can't save/update object at PostgreSQL at table " + tableName, e);
        }
    }

    private Map<String, Object> createColumnsFromObject(Object obj) {
        Map<String, Object> columns = new HashMap<>();
        for (var field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIgnore.class)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String columnName = field.getName();

                if (field.isAnnotationPresent(StorageKey.class)) {
                    var key = field.getAnnotation(StorageKey.class);
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
        // Se puede ampliar para tratar objetos complejos.
        return obj.toString();
    }

    private String extractIdentifier(StorageObject obj) {
        for (var field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(StorageIdentifier.class)) {
                field.setAccessible(true);
                return PluginConsumer.ofUnchecked(
                        () -> {
                            Object value = field.get(obj);
                            return value != null ? value.toString() : null;
                        },
                        e -> logError("Error getting field: " + field.getName(), e),
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
            logError("Error loading object with id: " + identifier, e);
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
            logError("Error al eliminar el objeto con id: " + identifier, e);
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
            logError("Can't load all objects", e);
        }
        return results;
    }

    private void ensureDatabaseConnected() {
        if (connection == null) {
            throw new IllegalStateException("No se ha establecido conexión con la base de datos.");
        }
    }

    /* ──────────────────────────────────────────────────────────────────────
       MÉTODOS DE INSTANCIACIÓN Y CONVERSIÓN
       ────────────────────────────────────────────────────────────────────── */

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object[] resolveConstructorArgs(Constructor<?> constructor, ResultSet resultSet, String identifier) {
        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            StorageKey paramAnnotation = param.getAnnotation(StorageKey.class);
            String paramName = (paramAnnotation != null && !paramAnnotation.key().isEmpty())
                    ? paramAnnotation.key()
                    : param.getName();
            String defaultValue = (paramAnnotation != null ? paramAnnotation.defaultValue() : null);
            Object value = null;
            try {
                if (param.isAnnotationPresent(StorageIdentifier.class)) {
                    value = identifier;
                } else if (isComplexObject(param.getType())) {
                    value = instantiateComplexObject(param.getType(), resultSet, identifier);
                } else {
                    value = resultSet.getObject(paramName);
                }
                if (value == null && defaultValue != null && !defaultValue.isEmpty()) {
                    value = convertValue(param.getType(), defaultValue);
                }
                // Conversión para float: si se espera Float y se obtiene Double.
                if ((param.getType().equals(Float.class) || param.getType().equals(float.class)) && value instanceof Double) {
                    value = ((Double) value).floatValue();
                }
                // Conversión para enums.
                if (param.getType().isEnum() && value instanceof String) {
                    try {
                        value = Enum.valueOf((Class<Enum>) param.getType(), (String) value);
                    } catch(Exception e) {
                        logError("Error with parameter " + paramName + " with value: " + value, e);
                    }
                }
                // Conversión para arrays si el valor es una List.
                if (param.getType().isArray() && value instanceof List<?> list) {
                    Object array = Array.newInstance(param.getType().getComponentType(), list.size());
                    for (int j = 0; j < list.size(); j++) {
                        Object element = list.get(j);
                        if ((param.getType().getComponentType().equals(Float.class) || param.getType().getComponentType().equals(float.class))
                                && element instanceof Double) {
                            element = ((Double) element).floatValue();
                        }
                        Array.set(array, j, element);
                    }
                    value = array;
                }
                // Conversión para colecciones si el valor es una List.
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
                        collection.add(element);
                    }
                    value = collection;
                }
            } catch (Exception e) {
                logError("Error resolving parameter: " + paramName, e);
            }
            values[i] = value;
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageObject> T instantiateObject(Class<?> clazz, ResultSet resultSet, String identifier) {
        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (constructor.isAnnotationPresent(StorageConstructor.class)) {
                    Object[] args = resolveConstructorArgs(constructor, resultSet, identifier);
                    return (T) constructor.newInstance(args);
                }
            }
        } catch (Exception e) {
            logError("Can't instantiate: " + clazz.getSimpleName(), e);
        }
        return null;
    }

    private Object instantiateComplexObject(Class<?> complexType, ResultSet resultSet, String identifier) {
        try {
            return instantiateObject(complexType, resultSet, identifier);
        } catch (Exception e) {
            logError("Can't instantiate object from type: " + complexType.getSimpleName(), e);
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
