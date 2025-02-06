package me.blueslime.bukkitmeteor.colors;

import me.blueslime.utilitiesapi.color.ColorHandler;

import java.util.List;

@SuppressWarnings("unused")
public abstract class TextUtilities {

    /**
     * Convert a text in a color text
     * @param message for the conversion
     * @return converted messages with colors and hex colors
     * Supports &#CODE and &CODE formats.
     */
    public static String colorize(String message) {
        return convert(message);
    }

    /**
     * Convert a text in a color text
     * @param message for the conversion
     * @return converted messages with colors and hex colors
     * Supports &#CODE and &CODE formats.
     */
    public static String convert(String message) {
        return ColorHandler.convert(message);
    }

    /**
     * Colorize an entire list
     * @param stringList to colorize
     * @return colorized list
     */
    public static List<String> colorizeList(List<String> stringList) {
        stringList.replaceAll(TextUtilities::colorize);
        return stringList;
    }

}