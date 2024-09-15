package me.blueslime.bukkitmeteor.menus;

import me.blueslime.utilitiesapi.item.ItemWrapper;

public class ItemMenu {
    private final ItemWrapper wrapper;
    private final String path;

    public ItemMenu(ItemWrapper wrapper, String path) {
        this.wrapper = wrapper;
        this.path = path;
    }

    public boolean isPresent() {
        return wrapper != null;
    }

    public ItemWrapper getWrapper() {
        return wrapper;
    }

    public String getPath() {
        return path;
    }
}
