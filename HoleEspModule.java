package dk.client67.modules;

import dk.client67.render.Projector;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.ModeSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Finder 1x1-huller/skakter: lodrette søjler af luft med fast blok på alle fire sider.
 * Scanner én lodret skive pr. tick, så det ikke laver lag.
 * "Min depth" = 1 finder også enkelt-blok-huller (fx PvP-huller).
 */
public class HoleEspModule extends Module {
    private record Hole(int x, int y, int z, int len) {}

    private final ModeSetting style = add(new ModeSetting("Style", "3D", "3D", "2D"));
    private final NumberSetting minDepth = add(new NumberSetting("Min depth", 3, 1, 12, 1));
    private final NumberSetting range = add(new NumberSetting("Range", 20, 8, 32, 2));
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final BoolSetting labels = add(new BoolSetting("Depth labels", true));

    private final Projector proj = new Projector();
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    private List<Hole> holes = List.of();
    private final List<Hole> building = new ArrayList<>();
    private int slice, cx, cy, cz, r;

    public HoleEspModule() {
        super("Hole ESP", "Viser 1x1-huller og skakter gennem vægge.", Category.RENDER, false);
    }

    @Override
    public void onDisable() {
        holes = List.of();
        building.clear();
        slice = 0;
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        if (slice == 0) {
            building.clear();
            cx = (int) Math.floor(p.getX());
            cy = (int) Math.floor(p.getY());
            cz = (int) Math.floor(p.getZ());
            r = range.getInt();
        }

        int x = cx - r + slice;
        int bottom = mc.world.getBottomY();
        int top = bottom + mc.world.getHeight() - 1;
        int minY = Math.max(bottom, cy - r);
        int maxY = Math.min(top, cy + r);
        int min = minDepth.getInt();

        for (int z = cz - r; z <= cz + r; z++) {
            int run = 0, start = 0;
            for (int y = minY; y <= maxY; y++) {
                if (isShaftCell(x, y, z)) {
                    if (run == 0) start = y;
                    run++;
                } else {
                    if (run >= min) building.add(new Hole(x, start, z, run));
                    run = 0;
                }
            }
            if (run >= min) building.add(new Hole(x, start, z, run));
        }

        slice++;
        if (slice > r * 2) {
            holes = new ArrayList<>(building);
            slice = 0;
        }
    }

    private boolean isShaftCell(int x, int y, int z) {
        pos.set(x, y, z);
        if (!mc.world.getBlockState(pos).isAir()) return false;
        return solid(x + 1, y, z) && solid(x - 1, y, z) && solid(x, y, z + 1) && solid(x, y, z - 1);
    }

    private boolean solid(int x, int y, int z) {
        pos.set(x, y, z);
        BlockState s = mc.world.getBlockState(pos);
        return s.isSolidBlock(mc.world, pos);
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        if (mc.player == null) return;
        proj.update(ctx, pt);

        int drawn = 0;
        for (Hole h : holes) {
            if (drawn >= 200) break;
            int color = 0xFFFFCC33;
            int[] b = proj.drawBox(ctx, h.x(), h.y(), h.z(), h.x() + 1, h.y() + h.len(), h.z() + 1,
                    color, style.index() == 0, fill.get());
            if (b == null) continue;
            drawn++;
            if (labels.get()) {
                String text = h.len() + " deep";
                int tx = (b[0] + b[2]) / 2 - mc.textRenderer.getWidth(text) / 2;
                ctx.drawTextWithShadow(mc.textRenderer, text, tx, b[1] - 10, color);
            }
        }
    }
}
