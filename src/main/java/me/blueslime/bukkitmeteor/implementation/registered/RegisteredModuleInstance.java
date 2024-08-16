package me.blueslime.bukkitmeteor.implementation.registered;

import me.blueslime.bukkitmeteor.implementation.module.RegisteredModule;

public class RegisteredModuleInstance implements RegisteredModule {
    private static final RegisteredModuleInstance instance = new RegisteredModuleInstance();

    public static RegisteredModuleInstance getInstance() {
        return instance;
    }

    @Override
    public void unregister() {

    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
