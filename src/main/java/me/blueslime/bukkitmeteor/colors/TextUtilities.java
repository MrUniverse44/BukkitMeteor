package me.blueslime.bukkitmeteor.colors;

import me.blueslime.utilitiesapi.color.ColorHandler;

public abstract class TextUtilities {
    public static String colorize(String message) {
        return convert(message);
    }

    public static String convert(String message) {
        return ColorHandler.convert(message);
    }
}