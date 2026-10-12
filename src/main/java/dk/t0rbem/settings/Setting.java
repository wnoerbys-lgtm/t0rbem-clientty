package dk.t0rbem.settings;

import com.google.gson.JsonElement;

public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract JsonElement save();

    public abstract void load(JsonElement element);
}
