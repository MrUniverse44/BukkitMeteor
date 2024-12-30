package me.blueslime.bukkitmeteor.implementation.executer;

import me.blueslime.bukkitmeteor.implementation.Implementer;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;
import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ModuleObjects<T extends Module> implements Implementer {

    private final Set<Exception> exceptionSet = new HashSet<>();
    private final Set<T> moduleSet = new HashSet<>();

    public ModuleObjects() {

    }

    @SafeVarargs
    public final ModuleObjects<T> at(T... module) {
        Collections.addAll(moduleSet, module);
        return this;
    }

    @SafeVarargs
    public final ModuleObjects<T> at(Class<? extends T>... module) {
        for (Class<? extends T> clazz : module) {
            PluginConsumer.process(
                () -> moduleSet.add(Implements.createInstance(clazz)),
                exceptionSet::add
            );
        }
        return this;
    }

    public ModuleObjects<T> onThrow(Consumer<Exception> consumer) {
        for (Exception exception : exceptionSet) {
            consumer.accept(exception);
        }
        return this;
    }

    public ModuleObjects<T> execute(Consumer<T> execute) {
        moduleSet.forEach(
            module -> PluginConsumer.process(
                () -> execute.accept(module), exceptionSet::add
            )
        );
        return this;
    }

}
