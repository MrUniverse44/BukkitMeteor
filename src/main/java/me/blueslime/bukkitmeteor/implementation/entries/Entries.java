package me.blueslime.bukkitmeteor.implementation.entries;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.module.Module;

import java.util.List;

@SuppressWarnings("unchecked")
public interface Entries {
    default BukkitMeteorPlugin registerModule(List<Module> modules) {
        return getMeteorPlugin().registerModule(modules.toArray(new Module[0]));
    }

    default BukkitMeteorPlugin registerModuleByClass(List<Class<? extends Module>> modules) {
        return getMeteorPlugin().registerModule(modules.toArray(new Class[0]));
    }

    default BukkitMeteorPlugin getMeteorPlugin() {
        return Implements.fetch(BukkitMeteorPlugin.class);
    }
}
