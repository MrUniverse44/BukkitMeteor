package me.blueslime.bukkitmeteor.scoreboards;

import com.xism4.sternalboard.SternalBoard;
import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.bukkitmeteor.implementation.registered.Register;
import me.blueslime.utilitiesapi.text.TextReplacer;
import me.blueslime.utilitiesapi.text.TextUtilities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Scoreboards {
    private final Map<UUID, SternalBoard> scoreboardMap = new ConcurrentHashMap<>();
    private static final TextReplacer EMPTY_REPLACER = TextReplacer.builder();
    private final BukkitMeteorPlugin plugin;
    private final boolean PLACEHOLDERS;

    public Scoreboards(BukkitMeteorPlugin plugin) {
        this.PLACEHOLDERS = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.plugin = plugin;
        Implements.register(this);
    }

    public SternalBoard fetchScoreboard(Player player) {
        return scoreboardMap.computeIfAbsent(
                player.getUniqueId(),
                key -> new SternalBoard(player)
        );
    }

    public SternalBoard getScoreboard(Player player) {
        return scoreboardMap.get(player.getUniqueId());
    }

    public void removeScoreboard(Player player) {
        if (scoreboardMap.containsKey(player.getUniqueId()) && player.isOnline()) {
            scoreboardMap.remove(player.getUniqueId()).delete();
            return;
        }
        scoreboardMap.remove(player.getUniqueId());
    }


    @Register(identifier = "placeholders")
    public boolean providePlaceholders() {
        return PLACEHOLDERS;
    }

    public void updateTitle(SternalBoard scoreboard, String title) {
        scoreboard.updateTitle(
            PLACEHOLDERS ?
                TextUtilities.colorize(PlaceholderAPI.setPlaceholders(scoreboard.getPlayer(), title)) :
                TextUtilities.colorize(title)
        );
    }

    public void setScoreboard(SternalBoard scoreboard, Player player, TextReplacer replacer, String title, String... lines) {
        if (title != null) {
            scoreboard.updateTitle(
                PLACEHOLDERS ?
                    TextUtilities.colorize(
                        PlaceholderAPI.setPlaceholders(
                            player,
                            replacer.apply(title)
                        )
                    ) :
                    TextUtilities.colorize(
                        replacer.apply(title)
                    )
            );
        }

        scoreboard.updateLines(
            lines
        );
    }

    public void setScoreboard(SternalBoard scoreboard, Player player, String title, String... lines) {
        setScoreboard(scoreboard, player, EMPTY_REPLACER, title, lines);
    }

    public void setScoreboard(SternalBoard scoreboard, Player player, String title, TextReplacer replacer) {
        setScoreboard(scoreboard, player, replacer, title, "");
    }

    public List<String> getScoreboardLines(List<String> lines) {
        return getScoreboardLines(lines, null, EMPTY_REPLACER);
    }

    public List<String> getScoreboardLines(Player player, List<String> lines) {
        return getScoreboardLines(lines, player, EMPTY_REPLACER);
    }

    public List<String> getScoreboardLines(List<String> lineList, Player player, TextReplacer replacer) {
        List<String> lines = new CopyOnWriteArrayList<>();
        StringBuilder white = new StringBuilder("&f");

        for (String line : lineList) {
            String replacedLine = PLACEHOLDERS ? PlaceholderAPI.setPlaceholders(player, line) : line;

            replacedLine = TextUtilities.colorize(
                replacer.apply(
                    replacedLine.replace("<player_name>", player.getName())
                            .replace("<player>", player.getName())
                            .replace("%player%", player.getName())
                )
            );

            if (!size(replacedLine)) {
                if (!lines.contains(replacedLine)) {
                    line = replacedLine;
                } else {
                    if (!size(white + replacedLine)) {
                        line = white + replacedLine;
                        white.append("&r");
                    }
                }
                if (size(line)) {
                    continue;
                }
                lines.add(
                    TextUtilities.colorize(line)
                );
            }
        }
        return lines;
    }

    public boolean size(String line) {
        line = TextUtilities.colorize(line);
        if (39 <= line.length()) {
            plugin.info(
                "&fLine: '" + line + "&f' has more than 39 characters, String length is longer than maximum allowed (" + line.length() + " > 39)",
                "This line will be hide for now, please fix this line."
            );
            return true;
        }
        return false;
    }

    public void remove(Player player) {
        scoreboardMap.remove(player.getUniqueId());
    }

    /**
     * Execute a consumer per scoreboard
     * @param scoreboardConsumer to execute
     */
    public void forEach(Consumer<SternalBoard> scoreboardConsumer) {
        find().forEach(scoreboardConsumer::accept);
    }

    /**
     * Scoreboard value set.
     * @return creates a new {@link HashSet}.
     */
    public Set<SternalBoard> find() {
        return new HashSet<>(scoreboardMap.values());
    }

    @Register
    public Scoreboards provideScoreboards() {
        return this;
    }
}
