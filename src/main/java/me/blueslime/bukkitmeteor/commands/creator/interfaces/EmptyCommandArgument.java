package me.blueslime.bukkitmeteor.commands.creator.interfaces;

import me.blueslime.bukkitmeteor.commands.sender.Sender;

/**
 * Interface for handling empty (no-argument) commands.
 */
@FunctionalInterface
public interface EmptyCommandArgument extends ArgumentHandler {
    void send(Sender sender);

    @Override
    default boolean handle(Sender sender, String[] args) {
        if (args.length == 0) {
            send(sender);
            return true;
        }
        return false;
    }
}