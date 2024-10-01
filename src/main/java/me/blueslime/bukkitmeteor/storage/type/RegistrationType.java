package me.blueslime.bukkitmeteor.storage.type;

public enum RegistrationType {
    DOUBLE_REGISTER,
    ONLY_THIS,
    DONT_REGISTER;

    public boolean isDouble() {
        return this == DOUBLE_REGISTER;
    }

    public boolean isOnlyThis() {
        return this == ONLY_THIS;
    }

}
