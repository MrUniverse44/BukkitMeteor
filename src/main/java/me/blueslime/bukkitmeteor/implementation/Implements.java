package me.blueslime.bukkitmeteor.implementation;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.builder.impls.EmptyImplement;
import me.blueslime.bukkitmeteor.implementation.abstracts.AbstractImplementer;
import me.blueslime.bukkitmeteor.implementation.data.Implement;
import me.blueslime.bukkitmeteor.implementation.error.IllegalMethodRegistration;
import me.blueslime.bukkitmeteor.implementation.factory.ImplementFactory;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.implementation.registered.RegisteredModuleInstance;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Implements extends AbstractImplementer {

    private final Map<RegistrationData, Object> CLASS_MAP = new ConcurrentHashMap<>();

    public Map<RegistrationData, Object> getRegistrationMap() {
        return CLASS_MAP;
    }

    // Public Methods for Class Operations
    public void unregisterAll(RegistrationData... all) {
        for (RegistrationData datum : all) {
            CLASS_MAP.remove(datum);
        }
    }

    public void unregisterAll(Module module) {
        if (module.isPersistent()) {
            return;
        }

        if (module instanceof RegisteredModuleInstance) {
            return;
        }

        List<RegistrationData> dataList = new CopyOnWriteArrayList<>(CLASS_MAP.keySet());
        dataList.removeIf(data -> data.getParentModule() != null && data.getParentModule().equals(module));
        dataList.forEach(CLASS_MAP::remove);
    }

    /**
     * Creates an instance of the given class by finding a suitable constructor and injecting dependencies.
     *
     * @param clazz The class to instantiate.
     * @param <T>   The type of the class.
     * @return A new instance of the class.
     * @throws RuntimeException If no suitable constructor is found or if not all parameters are annotated.
     */
    public <T> T create(Class<T> clazz) {
        getLogs().info("Attempting to create an instance of class: " + clazz.getSimpleName());
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            getLogs().info("Evaluating constructor: " + constructor);
            T value = processConstructor(clazz, constructor);
            if (value != null) {
                registerAll(value);
                getLogs().info("Instance created and registered successfully: " + value);
                return value;
            }
        }

        String errorMessage = "No suitable constructor found or not all parameters are annotated. Class: " + clazz.getSimpleName();
        getLogs().info(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    /**
     * Processes the given constructor by injecting dependencies into its parameters.
     *
     * @param clazz       The class to instantiate.
     * @param constructor The constructor to process.
     * @param <T>         The type of the class.
     * @return A new instance of the class if successful; otherwise, returns null.
     */
    private <T> T processConstructor(Class<T> clazz, Constructor<?> constructor) {
        int paramCount = constructor.getParameterCount();
        Object[] parameters = new Object[paramCount];
        boolean allAnnotated = true;
        Parameter[] params = constructor.getParameters();

        // If there are no parameters, create the instance directly.
        if (paramCount == 0) {
            try {
                getLogs().info("No-argument constructor found. Creating instance directly.");
                constructor.setAccessible(true);
                T instance = clazz.cast(constructor.newInstance());
                getLogs().info("Instance created successfully without parameters: " + instance);
                return instance;
            } catch (Exception e) {
                getLogs().info("Error creating instance without parameters: " + e.getMessage());
                return null;
            }
        }

        // Process each parameter of the constructor.
        for (int i = 0; i < paramCount; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(Implement.class)) {
                Implement annotation = parameter.getAnnotation(Implement.class);
                getLogs().info("Parameter " + i + " annotated with @Implement. Identifier: " + annotation.identifier());
                try {
                    if (annotation.identifier().isEmpty()) {
                        parameters[i] = Implements.fetch(parameter.getType());
                    } else {
                        parameters[i] = Implements.fetch(parameter.getType(), annotation.identifier());
                    }
                    getLogs().info("Injected dependency for parameter " + i + ": " + parameters[i]);
                } catch (Exception e) {
                    getLogs().info("Error injecting dependency for parameter " + i + " (" + parameter.getType().getSimpleName() + "): " + e.getMessage());
                    return null;
                }
            } else {
                getLogs().info("Parameter " + i + " is not annotated with @Implement.");
                allAnnotated = false;
            }
        }

        if (!allAnnotated) {
            getLogs().info("Not all constructor parameters are annotated with @Implement. Skipping this constructor.");
            return null;
        }

        try {
            constructor.setAccessible(true);
            T instance = clazz.cast(constructor.newInstance(parameters));
            getLogs().info("Instance created successfully using constructor: " + constructor);
            return instance;
        } catch (Exception e) {
            getLogs().info("Error instantiating " + clazz.getSimpleName() + " using constructor " + constructor + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Registers all provided objects by invoking the registerClass method for each.
     *
     * @param classes The objects to register.
     */
    public void registerAll(Object... classes) {
        for (Object clazz : classes) {
            registerClass(clazz);
        }
    }

    /**
     * Registers an instance by processing its fields and methods, including those in its superclass hierarchy.
     *
     * @param instancedClass The instance to register.
     */
    public void registerClass(Object instancedClass) {
        Class<?> clazz = instancedClass.getClass();
        getLogs().info("Registering class: " + clazz.getSimpleName());

        // Support for superclasses.
        while (clazz != null) {
            handleFields(clazz, instancedClass);
            handleMethods(clazz, instancedClass);
            clazz = clazz.getSuperclass();
        }
    }

    public Logger getLogs() {
        return Implements.fetch(BukkitMeteorPlugin.class).getLogger();
    }

    /**
     * Processes all declared fields of the given class and registers or injects dependencies as needed.
     *
     * @param clazz          The class whose fields will be processed.
     * @param instancedClass The instance containing the fields.
     */
    private void handleFields(Class<?> clazz, Object instancedClass) {
        Field[] fields = clazz.getDeclaredFields();
        Module module = instancedClass instanceof Module ? (Module) instancedClass : null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Implement.class)) {
                processImplementField(field, instancedClass);
            }
            if (field.isAnnotationPresent(Register.class)) {
                processRegisterField(field, instancedClass, module);
            }
        }
    }

    /**
     * Processes all declared methods of the given class and registers them if annotated.
     *
     * @param clazz          The class whose methods will be processed.
     * @param instancedClass The instance containing the methods.
     */
    private void handleMethods(Class<?> clazz, Object instancedClass) {
        Method[] methods = clazz.getDeclaredMethods();
        Module module = instancedClass instanceof Module ? (Module) instancedClass : null;

        for (Method method : methods) {
            if (method.isAnnotationPresent(Register.class)) {
                processRegisterMethod(method, instancedClass, module);
            }
        }
    }

    /**
     * Injects a dependency into the annotated field.
     *
     * @param field          The field to process.
     * @param instancedClass The instance containing the field.
     */
    private void processImplementField(Field field, Object instancedClass) {
        field.setAccessible(true);
        Implement implement = field.getAnnotation(Implement.class);
        Class<?> fieldClazz = field.getType();

        PluginConsumer.process(() -> {
            if (implement.identifier().isEmpty()) {
                field.set(instancedClass, Implements.fetch(fieldClazz));
            } else {
                field.set(instancedClass, Implements.fetch(fieldClazz, implement.identifier()));
            }
            getLogs().info("Injected dependency into field " + field.getName() + ": " + field.get(instancedClass));
        }, e -> getLogs().info("Error injecting dependency into field " + field.getName() + ": " + e.getMessage())
        );
    }

    /**
     * Processes a field annotated with @Register and registers its value.
     *
     * @param field          The field to process.
     * @param instancedClass The instance containing the field.
     * @param module         The module instance, if available.
     */
    private void processRegisterField(Field field, Object instancedClass, Module module) {
        field.setAccessible(true);
        Register data = field.getAnnotation(Register.class);

        PluginConsumer.process(() -> {
            Object value = field.get(instancedClass);
            if (value != null) {
                if (data.identifier().isEmpty()) {
                    CLASS_MAP.put(RegistrationData.fromData(module, field.getType()), value);
                } else {
                    CLASS_MAP.put(RegistrationData.fromData(module, field.getType(), data.identifier()), value);
                }
            }
            getLogs().info("Registered field " + field.getName() + " with value: " + value);
        }, e -> getLogs().info("Error processing register field " + field.getName() + ": " + e.getMessage()));
    }

    /**
     * Processes a method annotated with @Register and registers its return value.
     *
     * @param method         The method to process.
     * @param instancedClass The instance containing the method.
     * @param module         The module instance, if available.
     */
    private void processRegisterMethod(Method method, Object instancedClass, Module module) {
        if (method.getReturnType().equals(Void.TYPE)) {
            getLogs().info("Skipping method " + method.getName() + " because it returns void.");
            return;
        }

        method.setAccessible(true);
        Register data = method.getAnnotation(Register.class);

        PluginConsumer.process(() -> {
            try {
                Object value = method.invoke(instancedClass);
                if (value != null) {
                    if (data.identifier().isEmpty()) {
                        CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType()), value);
                    } else {
                        CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType(), data.identifier()), value);
                    }
                    getLogs().info("Registered method " + method.getName() + " with value: " + value);
                } else {
                    String msg = "Cannot register a null result from method: " + method.getName() + " of type " + method.getReturnType().getSimpleName();
                    getLogs().info(msg);
                    throw new IllegalMethodRegistration(msg);
                }
            } catch (Exception ex) {
                getLogs().info("Error processing register method " + method.getName() + ": " + ex.getMessage());
                throw new RuntimeException(new IllegalMethodRegistration(ex));
            }
        });
    }


    @SuppressWarnings("unchecked")
    public <T> T fetchClass(RegistrationData data) {
        Object result = CLASS_MAP.get(data);
        if (result == null) {
            if (data.getInstance() == EmptyImplement.class) {
                CLASS_MAP.put(RegistrationData.fromData(EmptyImplement.class), EmptyImplement.INVOKE);
                return (T) EmptyImplement.INVOKE;
            }
            return null;
        }
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    public <T> T fetchClass(RegistrationData data, EmptyImplement implement) {
        Object result = CLASS_MAP.get(data);
        if (result == null) {
            if (data.getInstance() == EmptyImplement.class) {
                CLASS_MAP.put(RegistrationData.fromData(EmptyImplement.class), EmptyImplement.INVOKE);
            }

            if (implement == EmptyImplement.INVOKE) {
                result = createInstance(data.getInstance());

                CLASS_MAP.put(data, result);

                return (T) result;
            }
            return null;
        }
        return (T) result;
    }

    public <T> T fetchClass(Class<T> clazz) {
        return fetchClass(RegistrationData.fromData(clazz));
    }

    public <T> T fetchClass(Class<T> clazz, EmptyImplement implement) {
        return fetchClass(RegistrationData.fromData(clazz), implement);
    }

    public <T> T fetchClass(Class<T> clazz, String identifier, EmptyImplement implement) {
        return fetchClass(RegistrationData.fromData(clazz, identifier), implement);
    }

    @SuppressWarnings("unchecked")
    public <T> T update(RegistrationData data, T newValue) {
        return (T) CLASS_MAP.put(data, newValue);
    }

    public <T> T update(Class<T> clazz, T newValue) {
        return update(RegistrationData.fromData(clazz), newValue);
    }

    public <T> T update(Class<T> clazz, String identifier, T newValue) {
        return update(RegistrationData.fromData(clazz, identifier), newValue);
    }

    public <T> T update(Class<T> clazz, T newValue, boolean persist) {
        if (!persist) {
            return update(RegistrationData.fromData(clazz), newValue);
        }
        return update(RegistrationData.fromData(RegisteredModuleInstance.getInstance(), clazz), newValue);
    }

    public <T> T update(Class<T> clazz, String identifier, T newValue, boolean persist) {
        if (!persist) {
            return update(RegistrationData.fromData(clazz, identifier), newValue);
        }
        return update(RegistrationData.fromData(RegisteredModuleInstance.getInstance(), clazz, identifier), newValue);
    }

    public static void addRegistrationData(RegistrationData data, Object value) {
        inst().CLASS_MAP.put(data, value);
    }

    /**
     * register implement set, collection, map or list
     * @param type of the set parameter
     * @param values for this set
     */
    @SafeVarargs
    public static <T, V extends T> ImplementFactory<T, V> registerImpls(Class<T> type, V... values) {
        return new ImplementFactory<>(values);
    }

    /**
     * register implement set, collection, map or list
     * @param type of the set parameter
     * @param values for this set
     */
    @SafeVarargs
    public static <T, V extends T> ImplementFactory<T, V> registerImpls(Class<T> type, Class<V>... values) {
        return new ImplementFactory<>(createInstances(values));
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] createInstances(Class<T>[] values) {
        T[] instances = (T[]) Array.newInstance(values[0], values.length);
        for (int i = 0; i < values.length; i++) {
            instances[i] = createInstance(values[i]);
        }
        return instances;
    }
}
