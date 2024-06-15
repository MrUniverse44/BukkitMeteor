package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.implementation.Implements;

public interface Module {

    default void initialize() {

    }

    default void reload() {

    }

    default void shutdown() {
        unregister();
    }

    default void register() {
        Implements.register(this, this);
    }

    default void unregister() {
        Implements.unregister(this);
    }
}
