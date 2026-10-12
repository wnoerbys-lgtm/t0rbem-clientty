package dk.t0rbem.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting {
    private boolean value;

    public BoolSetting(String name, boolean def) {
        super(name);
        this.value = def;
    }

    public boolean get() { return value; }
    public void set(boolean v) { value = v; }
    public void toggle() { value = !value; }

    @Override
    public JsonElement save() { return new JsonPrimitive(value); }

    @Override
    public void load(JsonElement e) { value = e.getAsBoolean(); }
}
