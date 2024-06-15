package me.blueslime.bukkitmeteor.implementation.registered;

import me.blueslime.bukkitmeteor.implementation.module.Module;

public class RegistrationData {
    private final String identifier;
    private final Class<?> clazz;
    private final Module module;

    private RegistrationData(Class<?> clazz) {
        this(clazz, null);
    }

    private RegistrationData(Module module, Class<?> clazz) {
        this(module, clazz, null);
    }

    private RegistrationData(Class<?> clazz, String identifier) {
        this(null, clazz, identifier);
    }

    private RegistrationData(Module module, Class<?> clazz, String identifier) {
        if (clazz == null) {
            throw new NullPointerException("Result cannot be null in a @Register");
        }
        this.identifier = identifier;
        this.module = module;
        this.clazz = clazz;
    }

    public static RegistrationData fromData(Class<?> clazz) {
        return new RegistrationData(clazz);
    }

    public static RegistrationData fromData(Class<?> clazz, String identifier) {
        return new RegistrationData(clazz, identifier);
    }

    public static RegistrationData fromData(Module module, Class<?> clazz) {
        return new RegistrationData(module, clazz);
    }

    public static RegistrationData fromData(Module module, Class<?> clazz, String identifier) {
        return new RegistrationData(module, clazz, identifier);
    }

    public Class<?> getInstance() {
        return clazz;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Module getParentModule() {
        return module;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public RegistrationData clone() {
        return new RegistrationData(clazz, identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationData that = (RegistrationData) o;
        if (clazz != that.clazz) {
            return false;
        }
        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + clazz.hashCode();
        return result;
    }

    public Module getParent() {
        return module;
    }
}

