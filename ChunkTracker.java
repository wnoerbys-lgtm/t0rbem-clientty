package dk.client67;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tæller hvor mange ticks der har været spillere i hver chunk.
 * Gemmes pr. server og dimension i config/67client-chunks.json.
 */
public class ChunkTracker {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, Map<Long, Integer>> DATA = new HashMap<>();
    private static boolean dirty;
    private static int sinceSave;

    public static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int unpackX(long k) { return (int) (k >> 32); }
    private static int unpackZ(long k) { return (int) k; }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("67client-chunks.json");
    }

    public static String worldKey(MinecraftClient mc) {
        ServerInfo info = mc.getCurrentServerEntry();
        String server = info != null ? info.address : "singleplayer";
        String dim = mc.world != null ? mc.world.getRegistryKey().getValue().toString() : "unknown";
        return server + "|" + dim;
    }

    public static Map<Long, Integer> current(MinecraftClient mc) {
        return DATA.computeIfAbsent(worldKey(mc), k -> new HashMap<>());
    }

    public static int get(Map<Long, Integer> map, int chunkX, int chunkZ) {
        return map.getOrDefault(pack(chunkX, chunkZ), 0);
    }

    public static void tick(MinecraftClient mc, boolean includeSelf) {
        if (mc.world == null) return;
        Map<Long, Integer> map = current(mc);
        Set<Long> seen = new HashSet<>();
        for (var player : mc.world.getPlayers()) {
            if (player == mc.player && !includeSelf) continue;
            int cx = ((int) Math.floor(player.getX())) >> 4;
            int cz = ((int) Math.floor(player.getZ())) >> 4;
            seen.add(pack(cx, cz));
        }
        for (long k : seen) map.merge(k, 1, Integer::sum);
        if (!seen.isEmpty()) dirty = true;

        if (++sinceSave >= 1200) { // ca. hvert minut
            sinceSave = 0;
            if (dirty) save();
        }
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (String world : root.keySet()) {
                Map<Long, Integer> m = new HashMap<>();
                JsonObject w = root.getAsJsonObject(world);
                for (String key : w.keySet()) {
                    String[] p = key.split(",");
                    m.put(pack(Integer.parseInt(p[0]), Integer.parseInt(p[1])), w.get(key).getAsInt());
                }
                DATA.put(world, m);
            }
        } catch (Exception e) {
            Client67.LOGGER.warn("Kunne ikke læse chunk-data", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (var world : DATA.entrySet()) {
                JsonObject w = new JsonObject();
                for (var e : world.getValue().entrySet()) {
                    w.addProperty(unpackX(e.getKey()) + "," + unpackZ(e.getKey()), e.getValue());
                }
                root.add(world.getKey(), w);
            }
            Files.writeString(file(), GSON.toJson(root));
            dirty = false;
        } catch (Exception e) {
            Client67.LOGGER.warn("Kunne ikke gemme chunk-data", e);
        }
    }
}
