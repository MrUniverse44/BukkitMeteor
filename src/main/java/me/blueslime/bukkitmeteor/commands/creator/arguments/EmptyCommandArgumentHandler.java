package me.blueslime.bukkitmeteor.commands.creator.arguments;

import me.blueslime.bukkitmeteor.commands.creator.interfaces.ArgumentHandler;
import me.blueslime.bukkitmeteor.commands.sender.Sender;

/**
 * Interface for handling empty (no-argument) commands.
 */
public abstract class EmptyCommandArgumentHandler implements ArgumentHandler {

    /**
     * Execute this handler
     * @param sender of the command instance.
     */
    public abstract void send(Sender sender);

    @Override
    public boolean handle(Sender sender, String[] args) {
        if (args.length == 0) {
            send(sender);
            return true;
        }
        return false;
    }
}