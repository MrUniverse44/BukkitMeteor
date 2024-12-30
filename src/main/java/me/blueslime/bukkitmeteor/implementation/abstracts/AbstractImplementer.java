package me.blueslime.bukkitmeteor.implementation.abstracts;

import me.blueslime.bukkitmeteor.builder.impls.EmptyImplement;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;

public abstract class AbstractImplementer {
    private static Implements instance = null;

    protected static Implements inst() {
        if (instance == null) {
            instance = new Implements();
        }
        return instance;
    }

    // Public Static Methods for Registering, Unregistering and Fetching Classes
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
        return inst().fetchClass(clazz, fetch(EmptyImplement.class));
    }

    public static <T> T fetch(Class<T> clazz, String identifier) {
        return inst().fetchClass(clazz, identifier, fetch(EmptyImplement.class));
    }

    public static <T> T setEntry(Class<T> clazz, T newValue) {
        return inst().update(clazz, newValue);
    }

    public static <T> T setEntry(Class<T> clazz, String identifier, T newValue) {
        return inst().update(clazz, identifier, newValue);
    }

    public static <T> T setEntry(Class<T> clazz, T newValue, boolean persist) {
        return inst().update(clazz, newValue, persist);
    }

    public static <T> T setEntry(Class<T> clazz, String identifier, T newValue, boolean persist) {
        return inst().update(clazz, identifier, newValue, persist);
    }

    public static <T> T setEntry(Class<T> clazz) {
        return inst().update(clazz, createInstance(clazz));
    }

    public static <T> T setEntry(Class<T> clazz, String identifier) {
        return inst().update(clazz, identifier, createInstance(clazz));
    }

    public static <T> T setEntry(Class<T> clazz, boolean persist) {
        return inst().update(clazz, createInstance(clazz), persist);
    }

    public static <T> T setEntry(Class<T> clazz, String identifier, boolean persist) {
        return inst().update(clazz, identifier, createInstance(clazz), persist);
    }

    /**
     * Creates an instance of a given class.
     *
     * @param clazz the class to create an instance of.
     * @return the created instance.
     */
    public static <T> T createInstance(Class<T> clazz) {
        return inst().create(clazz);
    }
}
