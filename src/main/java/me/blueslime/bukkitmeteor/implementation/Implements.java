package me.blueslime.bukkitmeteor.implementation;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

        /* Feat: support for super class */
        while (clazz != null) {
            handleFields(clazz, instancedClass);
            handleMethods(clazz, instancedClass);
            clazz = clazz.getSuperclass();
        }
    }

    public <T> T create(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            T value = processConstructor(clazz, constructor);
            if (value != null) {
                registerAll(value);
                return value;
            }
        }

        throw new RuntimeException("No suitable constructor found or not all parameters are annotated.");
    }

    // Private Methods for Processing
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

    private void handleMethods(Class<?> clazz, Object instancedClass) {
        Method[] methods = clazz.getDeclaredMethods();
        Module module = instancedClass instanceof Module ? (Module) instancedClass : null;

        for (Method method : methods) {
            if (method.isAnnotationPresent(Register.class)) {
                processRegisterMethod(method, instancedClass, module);
            }
        }
    }

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
        });
    }

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
        });
    }

    private void processRegisterMethod(Method method, Object instancedClass, Module module) {
        if (method.getReturnType().equals(Void.TYPE)) {
            return;
        }

        method.setAccessible(true);
        Register data = method.getAnnotation(Register.class);

        PluginConsumer.process(() -> {
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
        }, ex -> {
            throw new RuntimeException(new IllegalMethodRegistration(ex));
        });
    }

    private <T> T processConstructor(Class<T> clazz, Constructor<?> constructor) {
        Object[] parameters = new Object[constructor.getParameterCount()];
        boolean allAnnotated = true;

        Parameter[] params = constructor.getParameters();
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (parameter.isAnnotationPresent(Implement.class)) {
                Implement annotation = parameter.getAnnotation(Implement.class);
                if (annotation.identifier().isEmpty()) {
                    parameters[i] = Implements.fetch(parameter.getType());
                } else {
                    parameters[i] = Implements.fetch(parameter.getType(), annotation.identifier());
                }
            } else {
                allAnnotated = false;
            }
        }

        if (allAnnotated) {
            try {
                return clazz.cast(constructor.newInstance(parameters));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T fetchClass(RegistrationData data) {
        Object result = CLASS_MAP.get(data);
        if (result == null) {
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
