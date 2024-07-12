package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.implementation.Implements;

/**
 * This is a simple module interface
 * This is used for registerModule in the main class
 * to allow the usage with instance.getModule method
 * with this method you can get the instance of this module
 * in other classes.
 */
public interface Module {

    default void initialize() {

    }

    default void reload() {

    }

    default void shutdown() {
        unregister();
    }

    default void register() {
        Implements.register(this);
    }

    default void register(Object thisInstance) {
        Implements.register(thisInstance);
    }

    default void unregister() {
        Implements.unregister(this);
    }

    default boolean isPersistent() {
        return false;
    }
}
