package me.blueslime.bukkitmeteor.implementation.module;

import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.registered.RegistrationData;

/**
 * Registered Module is the same module but is automatically being registered
 * at the Implements when is registered in the main class with the registerModule method
 * or you can also register it by yourself in the same class using the registerModule method.
 */
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
