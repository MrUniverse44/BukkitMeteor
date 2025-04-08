package me.blueslime.bukkitmeteor.utils;

import me.blueslime.bukkitmeteor.colors.TextUtilities;
import me.blueslime.bukkitmeteor.logs.LoggerType;
import me.blueslime.bukkitmeteor.logs.MeteorLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.util.EnumMap;
import java.util.Map;

public class ForcedLogs implements MeteorLogger {

    private final Map<LoggerType, String> logMap = new EnumMap<>(LoggerType.class);

    @Override
    public void send(String... message) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        for (String s : message) {
            console.sendMessage(
                TextUtilities.colorize(s)
            );
        }
    }

    @Override
    public MeteorLogger setPrefix(LoggerType log, String prefix) {
        logMap.put(log, prefix);
        return this;
    }

    @Override
    public String getPrefix(LoggerType prefix) {
        return logMap.computeIfAbsent(
            prefix,
            (k) -> prefix.getDefaultPrefix("MeteorLogs")
        );
    }

    @Override
    public void build() {

    }
}
