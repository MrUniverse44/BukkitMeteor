package me.blueslime.bukkitmeteor.utils.skin;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

import me.blueslime.bukkitmeteor.BukkitMeteorPlugin;
import me.blueslime.bukkitmeteor.implementation.Implements;
import me.blueslime.utilitiesapi.text.TextReplacer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SkinUtil {

    private static final Gson gson = new Gson();
    private static final Map<UUID, BufferedImage> headCache = new ConcurrentHashMap<>();
    private static final Map<UUID, CompletableFuture<BufferedImage>> pendingRequests = new ConcurrentHashMap<>();
    private static final JsonParser jsonParser = new JsonParser();

    public static CompletableFuture<BufferedImage> getPlayerHeadAsync(Player player) {
        return getPlayerHeadAsync(player, Implements.fetch(BukkitMeteorPlugin.class));
    }

    /**
     * Returns a CompletableFuture containing the player's head as a BufferedImage.
     * - If it's already cached in memory, returns CompletableFuture.completedFuture(...)
     * - If a PNG file exists on disk at WorldContainer/MeteorHeads/<UUID>.png, it loads and returns it.
     * - Otherwise, it makes a request to sessionserver.mojang.com, extracts the 8x8 subimage,
     * saves it to disk, and caches it.
     *
     * @param player to search head
     * @param plugin of the execution
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static CompletableFuture<BufferedImage> getPlayerHeadAsync(Player player, Plugin plugin) {
        UUID uuid = player.getUniqueId();

        BufferedImage cached = headCache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        CompletableFuture<BufferedImage> existing = pendingRequests.get(uuid);
        if (existing != null) {
            return existing;
        }

        CompletableFuture<BufferedImage> future = CompletableFuture.supplyAsync(() -> {
            try {
                File folder = new File(plugin.getServer().getWorldContainer(), "MeteorHeads");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                File pngFile = new File(folder, uuid.toString() + ".png");
                if (pngFile.exists()) {
                    try {
                        BufferedImage fromDisk = ImageIO.read(pngFile);
                        headCache.put(uuid, fromDisk);
                        return fromDisk;
                    } catch (Exception readEx) {
                        // * Corrupted file
                        pngFile.delete();
                    }
                }

                String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
                InputStream inputStream = new URL(url).openStream();
                JsonObject profile = jsonParser.parse(new InputStreamReader(inputStream)).getAsJsonObject();

                JsonArray properties = profile.getAsJsonArray("properties");
                JsonObject property = properties.get(0).getAsJsonObject();
                String value = property.get("value").getAsString();

                String decoded = new String(Base64.getDecoder().decode(value));
                JsonObject decodedJson = jsonParser.parse(decoded).getAsJsonObject();
                String skinUrl = decodedJson.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url")
                        .getAsString();

                BufferedImage fullSkin = ImageIO.read(new URL(skinUrl));
                BufferedImage head = fullSkin.getSubimage(8, 8, 8, 8);

                try {
                    ImageIO.write(head, "PNG", pngFile);
                } catch (Exception ignored) { }

                headCache.put(uuid, head);
                return head;

            } catch (Exception e) {
                throw new RuntimeException("Can't fetch minecraft skull from mojang-api for player " + uuid + ", ¿is your server ip blocked from mojang?", e);
            } finally {
                pendingRequests.remove(uuid);
            }
        });

        pendingRequests.put(uuid, future);
        return future;
    }

    public static TextReplacer convertToLines(BufferedImage image) {
        TextReplacer replacer = TextReplacer.builder();
        for (int y = 0; y < 8; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < 8; x++) {
                Color c = new Color(image.getRGB(x, y), true);
                line.append(String.format("&#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
            }
            replacer = replacer.replace("<player-head-" + y + ">", line.toString());
        }
        return replacer;
    }

}
