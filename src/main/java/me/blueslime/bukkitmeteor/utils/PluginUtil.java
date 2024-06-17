package me.blueslime.bukkitmeteor.utils;

public class PluginUtil {
    public static int getRows(int size) {
        if (size < 0) {
            return 9;
        }
        if (size < 7) {
            return size * 9;
        }
        if (size > 7) {
            if (size > 50) {
                return 54;
            }
            if (size > 40) {
                return 45;
            }
            if (size > 30) {
                return 36;
            }
            if (size > 20) {
                return 27;
            }
            if (size > 10) {
                return 18;
            }
        }
        return 9;
    }
}
