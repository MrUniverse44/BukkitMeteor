package me.blueslime.bukkitmeteor.commands.issues;

public class CommandWrongSenderException extends Exception {

    private final boolean player;
    private final boolean found;

    public CommandWrongSenderException(boolean isPlayer) {
        super("Command wrong sender");
        this.player = isPlayer;
        this.found = true;
    }

    public CommandWrongSenderException() {
        super("Command wrong sender");
        this.player = false;
        this.found = false;
    }

    public boolean isFound() {
        return found;
    }

    public boolean isPlayer() {
        return player;
    }
}
