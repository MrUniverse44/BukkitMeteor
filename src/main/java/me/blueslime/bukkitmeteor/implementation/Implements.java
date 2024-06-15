package me.blueslime.bukkitmeteor.implementation;

import me.blueslime.bukkitmeteor.implementation.error.IllegalMethodRegistration;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;
import me.blueslime.bukkitmeteor.utils.PluginConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class Implements {
    private final Map<RegistrationData, Object> CLASS_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("RedundantCast")
    public static void register(Object... classes) {
        inst().registerAll((Module)null, classes);
    }

    public static void register(Module module, Object... classes) {
        inst().registerAll(module, classes);
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

    public void unregisterAll(RegistrationData... all) {
        for (RegistrationData datum : all) {
            CLASS_MAP.remove(datum);
        }
    }

    public void unregisterAll(Module module) {
        List<RegistrationData> dataList = new ArrayList<>(CLASS_MAP.keySet());

        dataList.removeIf(data -> data.getParent() != null && data.getParent() == module);

        dataList.forEach(CLASS_MAP::remove);
    }

    public void registerAll(Object... classes) {
        for (Object clazz : classes) {
            registerClass(clazz);
        }
    }

    public void registerAll(Module module, Object... classes) {
        if (module == null) {
            registerAll(classes);
            return;
        }
        for (Object clazz : classes) {
            registerClass(module, clazz);
        }
    }

    public void registerClass(Object clazz) {
        registerClass(null, clazz);
    }

    public void registerClass(Module module, Object instancedClass) {
        Class<?> clazz = instancedClass.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Register.class)) {
                field.setAccessible(true);
                Register data = field.getAnnotation(Register.class);
                PluginConsumer.processUnchecked(
                    () -> {
                        Object value = field.get(instancedClass);
                        if (data.identifier().isEmpty()) {
                            CLASS_MAP.put(RegistrationData.fromData(module, field.getType()), value);
                        } else {
                            CLASS_MAP.put(RegistrationData.fromData(module, field.getType(), data.identifier()), value);
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
                            if (data.identifier().isEmpty()) {
                                CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType()), value);
                            } else {
                                CLASS_MAP.put(RegistrationData.fromData(module, method.getReturnType(), data.identifier()), value);
                            }
                        },
                        ex -> {
                            throw new RuntimeException(new IllegalMethodRegistration(ex));
                        }
                );
            }
        }
    }

    public <T> T fetchClass(Class<T> clazz) {
        return fetchClass(RegistrationData.fromData(clazz));
    }

    public <T> T fetchClass(Class<T> clazz, String identifier) {
        return fetchClass(RegistrationData.fromData(clazz, identifier));
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
