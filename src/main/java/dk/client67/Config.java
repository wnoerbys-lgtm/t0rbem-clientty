package dk.client67;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.client67.modules.Module;
import dk.client67.modules.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gemmer og indlæser moduler, indstillinger og HUD-positioner i config/67client.json. */
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("67client.json");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            if (root.has("hudVisible")) Client67.hudVisible = root.get("hudVisible").getAsBoolean();
            if (root.has("theme")) Theme.index = Math.max(0, Math.min(Theme.NAMES.length - 1, root.get("theme").getAsInt()));
            if (root.has("particles")) Theme.particles = root.get("particles").getAsBoolean();
            if (root.has("modules")) {
                JsonObject mods = root.getAsJsonObject("modules");
                for (Module m : ModuleManager.all()) {
                    if (mods.has(m.name)) m.load(mods.getAsJsonObject(m.name));
                }
            }
        } catch (Exception e) {
            Client67.LOGGER.warn("Kunne ikke læse config, bruger standardværdier", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("hudVisible", Client67.hudVisible);
            root.addProperty("theme", Theme.index);
            root.addProperty("particles", Theme.particles);
            JsonObject mods = new JsonObject();
            for (Module m : ModuleManager.all()) mods.add(m.name, m.save());
            root.add("modules", mods);
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception e) {
            Client67.LOGGER.warn("Kunne ikke gemme config", e);
        }
    }
}
