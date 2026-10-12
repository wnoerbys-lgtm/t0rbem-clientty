package dk.t0rbem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.t0rbem.modules.Module;
import dk.t0rbem.modules.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gemmer og indlæser moduler, indstillinger og HUD-positioner i config/t0rbem-client.json. */
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("t0rbem-client.json");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            if (root.has("hudVisible")) T0rbemClient.hudVisible = root.get("hudVisible").getAsBoolean();
            if (root.has("modules")) {
                JsonObject mods = root.getAsJsonObject("modules");
                for (Module m : ModuleManager.all()) {
                    if (mods.has(m.name)) m.load(mods.getAsJsonObject(m.name));
                }
            }
        } catch (Exception e) {
            T0rbemClient.LOGGER.warn("Kunne ikke læse config, bruger standardværdier", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("hudVisible", T0rbemClient.hudVisible);
            JsonObject mods = new JsonObject();
            for (Module m : ModuleManager.all()) mods.add(m.name, m.save());
            root.add("modules", mods);
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception e) {
            T0rbemClient.LOGGER.warn("Kunne ikke gemme config", e);
        }
    }
}
