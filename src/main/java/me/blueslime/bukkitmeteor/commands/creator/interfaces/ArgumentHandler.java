package me.blueslime.bukkitmeteor.commands.creator.interfaces;

import me.blueslime.bukkitmeteor.commands.sender.Sender;

public interface ArgumentHandler {
    boolean handle(Sender sender, String[] args);
}
