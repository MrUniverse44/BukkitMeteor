package me.blueslime.bukkitmeteor.scoreboards.data;

import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.ChatColor;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// WORK IN PROGRESS, PLEASE WAIT FOR OUR OWN SCOREBOARD SYSTEM

public class ScoreboardHandler<T> {
    private static final Map<Class<?>, Field[]> PACKETS = new HashMap<>(8);

    protected static final String[] COLOR_CODES = Arrays.stream(ChatColor.values())
        .map(Object::toString)
        .toArray(String[]::new);

    private static final VersionType VERSION_TYPE = PluginConsumer.ofUnchecked(
        () -> {
            ScoreboardReflection reflection = ScoreboardReflection.get();

            if (reflection.isRepackaged()) {
                return VersionType.V1_17;
            } else if (reflection.isClassPresent(reflection.fetchClass(null, "ScoreboardServer$Action"))) {
                return VersionType.V1_13;
            } else if (reflection.isClassPresent(reflection.fetchClass(null, "IScoreboardCriteria$EnumScoreboardHealthDisplay"))) {
                return VersionType.V1_8;
            } else {
                return VersionType.V1_7;
            }
        },
        e -> {},
        () -> VersionType.V1_17
    );

    private static final Class<?> CHAT_COMPONENT_CLASS = ScoreboardReflection
        .get()
        .find("network.chat", "IChatBaseComponent", "Component");

    private static final Class<?> CHAT_FORMAT_ENUM = ScoreboardReflection
        .get()
        .find(null, "EnumChatFormat", "ChatFormatting");

    private static final MethodHandle PLAYER_CONNECTION = ScoreboardReflection
        .get()
        .getPlayerConnection();

    private static final MethodHandle SEND_PACKET = ScoreboardReflection
        .get()
        .getSendPackage();

    private static final Object RESET_FORMATTING = ScoreboardReflection
        .get()
        .findFromClass(CHAT_FORMAT_ENUM, ScoreboardReflection.Search.ENUM, "RESET", 21);

    private static final MethodHandle PLAYER_GET_HANDLE = ScoreboardReflection
            .get()
            .getPlayerHandle();

    public enum ObjectiveMode {
        CREATE, REMOVE, UPDATE
    }

    public enum TeamMode {
        CREATE, REMOVE, UPDATE, ADD_PLAYERS, REMOVE_PLAYERS
    }

    public enum ScoreboardAction {
        CHANGE, REMOVE
    }

    enum VersionType {
        V1_7, V1_8, V1_13, V1_17;

        public boolean isHigherOrEqual() {
            return VERSION_TYPE.ordinal() >= ordinal();
        }
    }

}
