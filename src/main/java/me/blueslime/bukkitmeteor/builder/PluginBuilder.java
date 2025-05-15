package me.blueslime.bukkitmeteor.builder;

import me.blueslime.bukkitmeteor.builder.impls.EmptyImplement;

public class PluginBuilder {
    private EmptyImplement implement = EmptyImplement.NULL;
    private String[] supportedLanguages = { "es", "en" };
    private boolean messageConfiguration = true;
    private boolean multilingual = false;
    private boolean inventories = true;
    private boolean menus = true;

    private PluginBuilder() {

    }

    public static PluginBuilder builder() {
        return new PluginBuilder();
    }

    public PluginBuilder generateMessageConfiguration(boolean messages) {
        this.messageConfiguration = messages;
        return this;
    }

    public PluginBuilder supportedLanguages(final String... supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
        return this;
    }

    public PluginBuilder multilingual(final boolean multilingual) {
        this.multilingual = multilingual;
        return this;
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

    public boolean isMultilingual() {
        return multilingual;
    }

    public String[] getSupportedLanguages() {
        return supportedLanguages;
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

    public boolean hasMessageFile() {
        return messageConfiguration;
    }
}
