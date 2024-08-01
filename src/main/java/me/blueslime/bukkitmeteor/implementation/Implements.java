package me.blueslime.bukkitmeteor.implementation;

import me.blueslime.bukkitmeteor.implementation.data.Implement;
import me.blueslime.bukkitmeteor.implementation.error.IllegalMethodRegistration;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.bukkitmeteor.utils.PluginConsumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class Implements {
    private final Map<RegistrationData, Object> CLASS_MAP = new ConcurrentHashMap<>();

    public static void register(Object... classes) {
        inst().registerAll(classes);
    }

    public static void unregister(RegistrationData... all) {
        inst().unregisterAll(all);
    }

    public static void unregister(Module module) {
        inst().unregisterAll(module);
    }

    public static <T> T fetch(Class<T> clazz) {
        return inst().fetchClass(clazz);
    }

    public static <T> T fetch(Class<T> clazz, String identifier) {
        return inst().fetchClass(clazz, identifier);
    }

    public static <T> T setEntry(Class<T> clazz, T newValue) {
        return inst().update(clazz, newValue);
    }

    public static <T> T setEntry(Class<T> clazz, String identifier, T newValue) {
        return inst().update(clazz, identifier, newValue);
    }

    public void unregisterAll(RegistrationData... all) {
        for (RegistrationData datum : all) {
            CLASS_MAP.remove(datum);
        }
    }

    public void unregisterAll(Module module) {
        if (module.isPersistent()) {
            return;
        }
        List<RegistrationData> dataList = new ArrayList<>(CLASS_MAP.keySet());

        dataList.removeIf(data -> data.getParentModule() != null && data.getParentModule().equals(module));

        dataList.forEach(CLASS_MAP::remove);
    }

    public void registerAll(Object... classes) {
        for (Object clazz : classes) {
            registerClass(clazz);
        }
    }

    public void registerClass(Object instancedClass) {
        Class<?> clazz = instancedClass.getClass();

        while (clazz != null) {

            Field[] fields = clazz.getDeclaredFields();

            Module module = instancedClass instanceof Module ?
                    (Module) instancedClass : null;

            for (Field field : fields) {
                if (field.isAnnotationPresent(Implement.class)) {
                    field.setAccessible(true);
                    Implement implement = field.getAnnotation(Implement.class);
                    Class<?> fieldClazz = field.getType();
                    PluginConsumer.process(
                        () -> {
                            if (implement.identifier().isEmpty()) {
                                field.set(instancedClass, Implements.fetch(fieldClazz));
                            } else {
                                field.set(instancedClass, Implements.fetch(fieldClazz, implement.identifier()));
                            }
                        }
                    );
                }
                if (field.isAnnotationPresent(Register.class)) {
                    field.setAccessible(true);
                    Register data = field.getAnnotation(Register.class);
                    PluginConsumer.process(
                        () -> {
                            Object value = field.get(instancedClass);
                            if (value != null) {
                                if (data.identifier().isEmpty()) {
                                    CLASS_MAP.put(
                                        RegistrationData.fromData(
                                            module,
                                            field.getType()
                                        ),
                                        value
                                    );
                                } else {
                                    CLASS_MAP.put(
                                        RegistrationData.fromData(
                                            module,
                                            field.getType(),
                                            data.identifier()
                                        ),
                                        value
                                    );
                                }
                            }
                        }
                    );
                }
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Register.class)) {
                    if (method.getReturnType().equals(Void.TYPE)) {
                        continue;
                    }
                    method.setAccessible(true);
                    Register data = method.getAnnotation(Register.class);
                    PluginConsumer.process(
                            () -> {
                                Object value = method.invoke(instancedClass);
                                if (value != null) {
                                    if (data.identifier().isEmpty()) {
                                        CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType()), value);
                                    } else {
                                        CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType(), data.identifier()), value);
                                    }
                                } else {
                                    throw new IllegalMethodRegistration("Can't register a null value result from: " + method.getName() + " of " + method.getReturnType().getSimpleName());
                                }
                            },
                            ex -> {
                                throw new RuntimeException(new IllegalMethodRegistration(ex));
                            }
                    );
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Use this if you have a class with
     * @param clazz to create instance.
     */
    public static <T> T createInstance(Class<T> clazz) {
        return inst().create(clazz);
    }

    public <T> T create(Class<T> clazz) {
        // Get all class constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            T value = processConstructor(clazz, constructor);
            if (value != null) {
                return value;
            }
        }

        throw new RuntimeException("No suitable constructor found or not all parameters are annotated.");
    }

    private <T> T processConstructor(Class<T> clazz, Constructor<?> constructor) {
        Object[] parameters = new Object[constructor.getParameterCount()];
        boolean allAnnotated = true;

        // Iterator
        Parameter[] params = constructor.getParameters();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(Implement.class)) {
                // Gets the annotation
                Implement annotation = parameter.getAnnotation(Implement.class);
                // Sets the parameter to the implement result.
                if (annotation.identifier().isEmpty()) {
                    parameters[i] = Implements.fetch(parameter.getType());
                } else {
                    parameters[i] = Implements.fetch(parameter.getType(), annotation.identifier());
                }
            } else {
                allAnnotated = false; // No todos los parámetros están anotados
            }
        }

        if (allAnnotated) {
            // Call the constructor with the parameters
            try {
                return clazz.cast(constructor.newInstance(parameters));
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public static void addRegistrationData(RegistrationData data, Object value) {
        inst().CLASS_MAP.put(data, value);
    }

    public <T> T fetchClass(Class<T> clazz) {
        return fetchClass(RegistrationData.fromData(clazz));
    }

    public <T> T fetchClass(Class<T> clazz, String identifier) {
        return fetchClass(RegistrationData.fromData(clazz, identifier));
    }

    public <T> T update(Class<T> clazz, T newValue) {
        return update(RegistrationData.fromData(clazz), newValue);
    }

    public <T> T update(Class<T> clazz, String identifier, T newValue) {
        return update(RegistrationData.fromData(clazz, identifier), newValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T update(RegistrationData data, T newValue) {
        return (T) CLASS_MAP.put(
            data,
            newValue
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T fetchClass(RegistrationData data) {
        Object result = CLASS_MAP.get(data);
        if (result == null) {
            return null;
        }
        return (T) result;
    }

    private static Implements instance = null;

    private static Implements inst() {
        if (instance == null) {
            instance = new Implements();
        }
        return instance;
    }
}
