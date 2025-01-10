package me.blueslime.bukkitmeteor.commands.issues;

public class CommandArgumentNotFoundException extends Exception {

    private final Class<?> type;

    public CommandArgumentNotFoundException(Class<?> type) {
        super("Argument was not found");
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
