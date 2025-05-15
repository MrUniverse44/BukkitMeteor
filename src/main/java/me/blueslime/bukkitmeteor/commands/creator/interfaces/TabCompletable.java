package me.blueslime.bukkitmeteor.commands.creator.interfaces;

import me.blueslime.bukkitmeteor.commands.sender.Sender;

import java.util.List;

/**
 * ArgumentHandler que además sabe ofrecer sugerencias de tab-complete.
 */
public interface TabCompletable {
    /**
     * @param sender quien está pidiendo las sugerencias
     * @param args   todos los tokens ya escritos (sin el comando)
     * @return lista de sugerencias (no nula)
     */
    List<String> complete(Sender sender, String[] args);
}
