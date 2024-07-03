package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;

public interface RegisteredModule extends Module {
    default void registerModule() {
        if (hasIdentifier()) {
            if (getIdentifier().isEmpty()) {
                Implements.addRegistrationData(
                        RegistrationData.fromData(this, getClass()), this
                );
            } else {
                Implements.addRegistrationData(
                        RegistrationData.fromData(this, getClass(), getIdentifier()), this
                );
            }
        } else {
            Implements.addRegistrationData(
                    RegistrationData.fromData(this, getClass()), this
            );
        }
    }

    default String getIdentifier() {
        return "";
    }

    default boolean hasIdentifier() {
        return false;
    }
}
