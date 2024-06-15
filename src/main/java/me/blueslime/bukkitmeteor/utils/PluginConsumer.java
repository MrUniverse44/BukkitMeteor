package me.blueslime.bukkitmeteor.utils;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface PluginConsumer<T> {

    T executeConsumer() throws Exception;

    interface PluginOutConsumer {
        void executeConsumer() throws Exception;
    }

    interface PluginExecutableConsumer<T> {
        T accept();
    }

    static  void process(PluginOutConsumer consumer) {
        try {
            consumer.executeConsumer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void process(String message, PluginOutConsumer consumer) {
        try {
            consumer.executeConsumer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static  void process(PluginOutConsumer consumer, Consumer<Exception> exception) {
        try {
            consumer.executeConsumer();
        } catch (Exception ex) {
            exception.accept(ex);
        }
    }

    static <T> T ofUnchecked(final PluginConsumer<T> template) {
        T results = null;
        try {
            results = template.executeConsumer();
        } catch (Exception ignored) {}
        return results;
    }

    static void processUnchecked(final PluginOutConsumer consumer) {
        try {
            consumer.executeConsumer();
        } catch (Exception ignored) {}
    }

    static <T> T ofUnchecked(PluginConsumer<T> template, Consumer<Exception> exception, PluginExecutableConsumer<T> defValue) {
        T results = null;

        try {
            results = template.executeConsumer();
        } catch (Exception var5) {
            if (defValue != null) {
                results = defValue.accept();
            }
            exception.accept(var5);
        }

        return results;
    }

    static <T> T ofUnchecked(final PluginConsumer<T> template, final Consumer<Exception> exception) {
        T results = null;
        try {
            results = template.executeConsumer();
        } catch (Exception ex) {
            exception.accept(ex);
        }
        return results;
    }

    static <T> T ofUnchecked(final PluginConsumer<T> template, T defValue) {
        T results = defValue;
        try {
            results = template.executeConsumer();
        } catch (Exception ignored) { }
        return results;
    }

    static <T> T ofUnchecked(String message, final PluginConsumer<T> template) {
        T results = null;
        try {
            results = template.executeConsumer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;
    }

    static <T> T ofUnchecked(String message, final PluginConsumer<T> template, T defValue) {
        T results = defValue;
        try {
            results = template.executeConsumer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;
    }

    static <T> PluginConsumer<T> of(PluginConsumer<T> c){ return c; }
}



