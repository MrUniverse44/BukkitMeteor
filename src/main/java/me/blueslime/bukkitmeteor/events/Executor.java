package me.blueslime.bukkitmeteor.events;

public interface Executor<R, T> {
    R execute(T t);
}
