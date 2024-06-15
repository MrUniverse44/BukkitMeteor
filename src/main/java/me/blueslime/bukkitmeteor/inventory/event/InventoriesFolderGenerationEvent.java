package me.blueslime.bukkitmeteor.inventory.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class InventoriesFolderGenerationEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final File folder;

    public InventoriesFolderGenerationEvent(File folder) {
        this.folder = folder;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlerList;
    }

    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public File getFolder() {
        return folder;
    }
}


