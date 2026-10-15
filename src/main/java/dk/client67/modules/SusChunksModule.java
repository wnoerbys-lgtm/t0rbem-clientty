package dk.client67.modules;

import dk.client67.ChunkTracker;
import dk.client67.render.Projector;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.Map;

/**
 * Tegner chunk-grænser i verden: rød = sus (spillere i chunken længe), grøn = prime (meget længe).
 * Tidsgrænserne sættes i "Chunk Map"-modulet, som også står for selve tidtagningen.
 */
public class SusChunksModule extends Module {
    private final NumberSetting range = add(new NumberSetting("Range (chunks)", 4, 1, 10, 1));
    private final NumberSetting height = add(new NumberSetting("Height", 16, 4, 64, 4));
    private final BoolSetting showPrime = add(new BoolSetting("Show prime (green)", true));

    private final Projector proj = new Projector();

    public SusChunksModule() {
        super("Sus Chunks", "Markerer sus (rød) og prime (grøn) chunks i verden.", Category.RENDER, false);
    }

    @Override
    public void onEnable() {
        // tidtagningen ligger i Chunk Map
        Module map = ModuleManager.get("Chunk Map");
        if (map != null) map.setEnabled(true);
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        if (mc.player == null || mc.world == null) return;
        if (!(ModuleManager.get("Chunk Map") instanceof ChunkMapModule cm)) return;
        proj.update(ctx, pt);

        Map<Long, Integer> map = ChunkTracker.current(mc);
        int pcx = ((int) Math.floor(proj.ex)) >> 4;
        int pcz = ((int) Math.floor(proj.ez)) >> 4;
        double py = Math.floor(proj.ey);
        double h = height.get();
        int r = range.getInt();

        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int cx = pcx + dx, cz = pcz + dz;
                int ticks = ChunkTracker.get(map, cx, cz);

                int color;
                if (ticks >= cm.primeTicks()) {
                    if (!showPrime.get()) continue;
                    color = 0xFF33FF55;
                } else if (ticks >= cm.susTicks()) {
                    color = 0xFFFF3333;
                } else {
                    continue;
                }

                double x0 = cx * 16.0, z0 = cz * 16.0, x1 = x0 + 16, z1 = z0 + 16;
                proj.wireBox(ctx, x0, py - h, z0, x1, py + h, z1, color);
                // ekstra kvadrat i din højde, så chunken er let at se
                proj.lineWorld(ctx, x0, py, z0, x1, py, z0, color);
                proj.lineWorld(ctx, x1, py, z0, x1, py, z1, color);
                proj.lineWorld(ctx, x1, py, z1, x0, py, z1, color);
                proj.lineWorld(ctx, x0, py, z1, x0, py, z0, color);
            }
        }
    }
}
