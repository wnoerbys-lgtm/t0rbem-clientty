package dk.t0rbem.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final List<HudModule> HUD = new ArrayList<>();

    public static void init() {
        // HUD (x/y er standardpositioner; for store tal klemmes ind til skærmkanten)
        register(new WatermarkModule());
        register(new FpsModule());
        register(new PingModule());
        register(new CpsModule());
        register(new SpeedModule());
        register(new CoordsModule());
        register(new BiomeModule());
        register(new ClockModule());
        register(new MemoryModule());
        register(new KeystrokesModule());
        register(new ArmorModule());
        register(new PotionModule());
        // Utility
        register(new AutoSprintModule());
        register(new FullbrightModule());
        register(new ZoomModule());
    }

    private static void register(Module m) {
        MODULES.add(m);
        if (m instanceof HudModule h) HUD.add(h);
    }

    public static List<Module> all() { return Collections.unmodifiableList(MODULES); }
    public static List<HudModule> hud() { return Collections.unmodifiableList(HUD); }

    public static List<Module> in(Module.Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.category == category) out.add(m);
        return out;
    }

    public static void tick() {
        for (Module m : MODULES) {
            if (m.enabled) m.onTick();
        }
    }
}
