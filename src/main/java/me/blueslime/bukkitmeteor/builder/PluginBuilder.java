package me.blueslime.bukkitmeteor.builder;

import me.blueslime.bukkitmeteor.builder.impls.EmptyImplement;

public class PluginBuilder {
    private EmptyImplement implement = EmptyImplement.NULL;
    private boolean inventories = true;
    private boolean menus = true;

    private PluginBuilder() {

    }

    public static PluginBuilder builder() {
        return new PluginBuilder();
    }

    public PluginBuilder generateMenus(boolean generate) {
        this.menus = generate;
        return this;
    }

    public PluginBuilder generateInventories(boolean generate) {
        this.inventories = generate;
        return this;
    }

    public PluginBuilder modifyUnknownImplements(EmptyImplement implement) {
        this.implement = implement;
        return this;
    }

    public boolean isInventories() {
        return inventories;
    }

    public boolean isMenus() {
        return menus;
    }

    public EmptyImplement getImplement() {
        return implement;
    }
}
