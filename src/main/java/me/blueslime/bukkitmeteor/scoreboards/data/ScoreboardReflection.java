package me.blueslime.bukkitmeteor.scoreboards.data;

import me.blueslime.utilitiesapi.utils.consumer.PluginConsumer;
import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class ScoreboardReflection {

    public static ScoreboardReflection INSTANCE;

    private final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer()
            .getClass()
            .getPackage()
            .getName();

    private final String NET_MINECRAFT = "net.minecraft";

    private final String NET_MINECRAFT_VERSION_PACKAGE = CRAFTBUKKIT_PACKAGE.replace(
        "org.bukkit.craftbukkit", NET_MINECRAFT + ".server"
    );

    private final MethodType VOID_METHOD_TYPE = MethodType.methodType(void.class);

    private final boolean NET_MINECRAFT_PACKET_REPACKAGED = isClassPresent(
        NET_MINECRAFT + ".network.protocol.Packet"
    );

    private final boolean NET_MINECRAFT_MOJANG_MAPPINGS = isClassPresent(
        NET_MINECRAFT + ".network.chat.Component"
    );

    public boolean isClassPresent(String location) {
        return find(location) != null;
    }

    public static ScoreboardReflection get() {
        if (INSTANCE == null) {
            INSTANCE = new ScoreboardReflection();
        }
        return INSTANCE;
    }

    public boolean isRepackaged() {
        return NET_MINECRAFT_PACKET_REPACKAGED;
    }

    public String fetchClass(String relocate, String className) {
        String packageDir = NET_MINECRAFT_PACKET_REPACKAGED ?
            relocate == null ? NET_MINECRAFT : NET_MINECRAFT + relocate
            : NET_MINECRAFT_VERSION_PACKAGE;

        return packageDir + "." + className;
    }

    public Class<?> find(String relocate, String spigotClass, String mojangClass) {
        return find(relocate, NET_MINECRAFT_MOJANG_MAPPINGS ? mojangClass : spigotClass);
    }

    public Class<?> find(String relocate, String className) {
        return find(fetchClass(relocate, className));
    }

    public Class<?> find(String className) {
        return PluginConsumer.ofUnchecked(
            () -> Class.forName(className),
            e -> {},
            () -> null
        );
    }

    public Object findFromClass(Class<?> clazz, Search search, String name) {
        return findFromClass(clazz, search, name, -1);
    }

    public Object findFromClass(Class<?> clazz, Search search, String name, int fallback) {
        if (clazz == null) {
            return null;
        }
        if (search == Search.ENUM) {
            return PluginConsumer.ofUnchecked(
                () -> Enum.valueOf(clazz.asSubclass(Enum.class), name.toUpperCase(Locale.ENGLISH)),
                e -> {},
                () -> {
                    if (fallback == -1) {
                        return null;
                    }
                    Object[] constants = clazz.getEnumConstants();
                    return constants.length > fallback ? constants[fallback] : null;
                }
            );
        }
        return null;
    }

    public MethodHandle getPlayerConnection() {
        Class<?> entityPlayerClass = find("server.level", "EntityPlayer", "ServerPlayer");

        Class<?> playerConnectionClass = find("server.network", "PlayerConnection", "ServerGamePacketListenerImpl");

        return PluginConsumer.ofUnchecked(
            () -> {
                Field playerConnectionField = Arrays.stream(entityPlayerClass.getFields())
                        .filter(field -> field.getType().isAssignableFrom(playerConnectionClass))
                        .findFirst().orElseThrow(NoSuchFieldException::new);
                return MethodHandles.lookup().unreflectGetter(playerConnectionField);
            },
            e -> {},
            () -> null
        );
    }

    public MethodHandle getSendPackage() {
        return PluginConsumer.ofUnchecked(
            () -> {
                Class<?> connection = find("server.network", "PlayerConnection", "ServerGamePacketListenerImpl");

                Method sendPacketMethod = Stream.concat(
                    Arrays.stream(connection.getSuperclass().getMethods()),
                    Arrays.stream(connection.getMethods())
                ).filter(
                    m -> m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == find("network.protocol", "Packet")
                ).findFirst().orElseThrow(NoSuchMethodException::new);

                return MethodHandles.lookup().unreflect(sendPacketMethod);
            },
            e -> {},
            () -> null
        );
    }

    public Optional<Class<?>> findOptionalMinecraftClass(String relocate, String className) {
        return findOptionalClass(fetchClass(relocate, className));
    }

    public Class<?> findCraftBukkitClass(String className) {
        return find(CRAFTBUKKIT_PACKAGE + '.' + className);
    }

    public Optional<Class<?>> findOptionalBukkitClass(String className) {
        return Optional.ofNullable(find(CRAFTBUKKIT_PACKAGE + '.' + className));
    }

    public Optional<Class<?>> findOptionalClass(String className) {
        return Optional.ofNullable(find(className));
    }

    public MethodHandle getPlayerHandle() {
        return PluginConsumer.ofUnchecked(
            () -> {
                Class<?> craftPlayerClass = findCraftBukkitClass("entity.CraftPlayer");
                Class<?> entityPlayerClass = find("server.level", "EntityPlayer", "ServerPlayer");

                return MethodHandles
                    .lookup()
                    .findVirtual(craftPlayerClass, "getHandle", MethodType.methodType(entityPlayerClass));
            },
            e -> {},
            () -> null
        );
    }

    public enum Search {
        ENUM
    }
}

