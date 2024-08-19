package me.blueslime.bukkitmeteor.implementation.module;

public interface PersistentModule extends RegisteredModule {
    @Override
    default boolean isPersistent() {
        return true;
    }

    @Override
    default void unregisterImplementedModule() {

    }
}
