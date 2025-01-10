package me.blueslime.bukkitmeteor.commands.function;

import me.blueslime.bukkitmeteor.commands.issues.CommandArgumentNotFoundException;

@FunctionalInterface
public interface CommandFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param argument The function argument
     * @return the function result
     */
    R apply(T argument) throws CommandArgumentNotFoundException;

}

