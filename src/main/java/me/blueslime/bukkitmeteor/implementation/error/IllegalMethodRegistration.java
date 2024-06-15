package me.blueslime.bukkitmeteor.implementation.error;

public class IllegalMethodRegistration extends Exception {
    public IllegalMethodRegistration(Exception origin) {
        super("Can't register a method with arguments because can't be invoked with the @Register annotation.", origin);
    }
}
