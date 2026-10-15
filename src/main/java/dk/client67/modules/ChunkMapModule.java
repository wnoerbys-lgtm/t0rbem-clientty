package dk.client67.modules;

import dk.client67.ChunkTracker;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.Map;

/**
 * Chunk-kort omkring dig. En chunk bliver rød når der har været spillere i den i
 * mere end X minutter (sus) og grøn efter mere end Y timer (prime).
 */
public class ChunkMapModule extends HudModule {
    private static final int FOOTER = 11;

    private final NumberSetting radius = add(new NumberSetting("Radius", 6, 3, 12, 1));
    private final NumberSetting cell = add(new NumberSetting("Cell size", 6, 3, 12, 1));
    private final NumberSetting susMinutes = add(new NumberSetting("Sus minutes", 20, 1, 120, 1));
    private final NumberSetting primeHours = add(new NumberSetting("Prime hours", 5, 1, 24, 1));
    private final BoolSetting includeSelf = add(new BoolSetting("Include yourself", false));

    public ChunkMapModule() {
        super("Chunk Map", "Sus chunks (rød) og prime chunks (grøn) ud fra tid med spillere i chunken.", true, 9999, 200);
    }

    @Override
    public void onTick() {
        ChunkTracker.tick(mc, includeSelf.get());
    }

    public int susTicks() { return susMinutes.getInt() * 1200; }
    public int primeTicks() { return primeHours.getInt() * 72000; }

    private int size() { return (radius.getInt() * 2 + 1) * cell.getInt(); }

    @Override
    public int width() { return mc.player == null ? 0 : size(); }

    @Override
    public int height() { return mc.player == null ? 0 : size() + FOOTER; }

    @Override
    protected void draw(DrawContext ctx, int x, int y) {
        if (mc.player == null || mc.world == null) return;

        int r = radius.getInt(), c = cell.getInt();
        int pcx = ((int) Math.floor(mc.player.getX())) >> 4;
        int pcz = ((int) Math.floor(mc.player.getZ())) >> 4;
        Map<Long, Integer> map = ChunkTracker.current(mc);

        int susTicks = susMinutes.getInt() * 1200;
        int primeTicks = primeHours.getInt() * 72000;

        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int ticks = ChunkTracker.get(map, pcx + dx, pcz + dz);
                int color;
                if (ticks >= primeTicks) color = 0xDD22CC44;      // prime = grøn
                else if (ticks >= susTicks) color = 0xDDDD2222;   // sus = rød
                else if (ticks > 0) color = 0x66AAAAAA;           // set, men ikke længe nok endnu
                else color = 0x33FFFFFF;
                int cx = x + (dx + r) * c, cy = y + (dz + r) * c;
                ctx.fill(cx, cy, cx + c - 1, cy + c - 1, color);
            }
        }

        // Din egen chunk markeres med hvid ramme
        int mx = x + r * c, my = y + r * c;
        ctx.fill(mx - 1, my - 1, mx + c, my, 0xFFFFFFFF);
        ctx.fill(mx - 1, my + c - 1, mx + c, my + c, 0xFFFFFFFF);
        ctx.fill(mx - 1, my, mx, my + c - 1, 0xFFFFFFFF);
        ctx.fill(mx + c - 1, my, mx + c, my + c - 1, 0xFFFFFFFF);

        int here = ChunkTracker.get(map, pcx, pcz) / 1200;
        String label = "Chunk " + pcx + "," + pcz + ": " + here + " min";
        ctx.drawTextWithShadow(mc.textRenderer, label, x, y + size() + 2, textColor());
    }
}
