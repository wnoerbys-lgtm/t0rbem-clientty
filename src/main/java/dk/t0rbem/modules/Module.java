package dk.t0rbem.modules;

import com.google.gson.JsonObject;
import dk.t0rbem.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category {
        HUD("HUD"),
        UTILITY("Utility");

        public final String label;

        Category(String label) { this.label = label; }
    }

    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;
    protected final List<Setting> settings = new ArrayList<>();
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected Module(String name, String description, Category category, boolean enabled) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting> settings() { return settings; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public JsonObject save() {
        JsonObject o = new JsonObject();
        o.addProperty("enabled", enabled);
        JsonObject s = new JsonObject();
        for (Setting st : settings) s.add(st.name, st.save());
        o.add("settings", s);
        return o;
    }

    public void load(JsonObject o) {
        try {
            if (o.has("enabled")) setEnabled(o.get("enabled").getAsBoolean());
            if (o.has("settings")) {
                JsonObject s = o.getAsJsonObject("settings");
                for (Setting st : settings) {
                    if (s.has(st.name)) {
                        try { st.load(s.get(st.name)); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
