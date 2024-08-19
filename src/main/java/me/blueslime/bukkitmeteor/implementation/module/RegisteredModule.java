package me.blueslime.bukkitmeteor.implementation.module;

/**
 * Registered Module is the same module but is automatically being registered
 * at the Implements when is registered in the main class with the registerModule method
 * or you can also register it by yourself in the same class using the registerModule method.
 */
public interface RegisteredModule extends Module {
    default String getIdentifier() {
        return "";
    }

    default boolean hasIdentifier() {
        return false;
    }
}
