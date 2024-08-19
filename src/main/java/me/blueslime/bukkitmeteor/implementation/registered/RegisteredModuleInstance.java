package me.blueslime.bukkitmeteor.implementation.registered;

import me.blueslime.bukkitmeteor.implementation.module.PersistentModule;

public class RegisteredModuleInstance implements PersistentModule {
    private static final RegisteredModuleInstance instance = new RegisteredModuleInstance();

    public static RegisteredModuleInstance getInstance() {
        return instance;
    }
}
