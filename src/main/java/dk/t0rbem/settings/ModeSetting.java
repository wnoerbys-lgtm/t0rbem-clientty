package dk.t0rbem.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String def, String... modes) {
        super(name);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(def)) index = i;
        }
    }

    public int index() { return index; }
    public String get() { return modes[index]; }
    public void cycle() { index = (index + 1) % modes.length; }

    @Override
    public JsonElement save() { return new JsonPrimitive(index); }

    @Override
    public void load(JsonElement e) {
        int i = e.getAsInt();
        if (i >= 0 && i < modes.length) index = i;
    }
}
