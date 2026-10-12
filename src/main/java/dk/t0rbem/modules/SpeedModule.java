package dk.t0rbem.modules;

import java.util.List;
import java.util.Locale;

public class SpeedModule extends TextHudModule {
    private double lastX, lastZ, speed;
    private boolean hasLast;

    public SpeedModule() { super("Speed", "Viser din hastighed i blokke per sekund.", false, 4, 68); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        double x = mc.player.getX(), z = mc.player.getZ();
        if (hasLast) {
            double raw = Math.hypot(x - lastX, z - lastZ) * 20.0;
            speed = speed * 0.7 + raw * 0.3; // let udjævning
        }
        lastX = x;
        lastZ = z;
        hasLast = true;
    }

    @Override
    public void onDisable() { hasLast = false; }

    @Override
    protected List<String> lines() { return List.of(String.format(Locale.ROOT, "Speed: %.1f b/s", speed)); }
}
