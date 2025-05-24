package me.blueslime.bukkitmeteor.events;

import me.blueslime.bukkitmeteor.implementation.module.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventExecutor<T extends Event> implements Listener, org.bukkit.plugin.EventExecutor {

    private final Set<Executor<Boolean, T>> cancellableConditions = new HashSet<>();
    private final EventPriority priority;
    private final Class<T> eventClass;

    private Consumer<T> cancelAction = null;
    private Consumer<T> execute = null;


    public EventExecutor(Class<T> eventClass, EventPriority priority) {
        this.eventClass = eventClass;
        this.priority = priority;
    }

    public void register() {
        getServer().getPluginManager().registerEvent(
            eventClass,
            this,
            priority,
            this,
            getMeteorPlugin()
        );
    }

    public EventExecutor<T> cancelIf(Executor<Boolean, T> condition) {
        cancellableConditions.add(condition);
        return this;
    }

    public EventExecutor<T> onCancelled(Consumer<T> consumer) {
        this.cancelAction = consumer;
        return this;
    }

    public EventExecutor<T> onExecute(Consumer<T> consumer) {
        this.execute = consumer;
        return this;
    }

    private void executeCancelled(T event) {
        if (cancelAction != null) {
            cancelAction.accept(event);
        }
    }

    private void execute(T event) {
        if (execute != null) {
            execute.accept(event);
        }
    }

    @Override
    public void execute(@NotNull org.bukkit.event.Listener listener, @NotNull Event event) throws EventException {
        if (eventClass.isInstance(event)) {
            T castedEvent = eventClass.cast(event);

            if (!cancellableConditions.isEmpty()) {
                for (Executor<Boolean, T> condition : cancellableConditions) {
                    boolean cancelled = condition.execute(castedEvent);
                    if (cancelled) {
                        if (castedEvent instanceof Cancellable cancellable) {
                            cancellable.setCancelled(true);
                            executeCancelled(castedEvent);
                            return;
                        }
                    }
                }
            }

            execute(castedEvent);
        }
    }
}
