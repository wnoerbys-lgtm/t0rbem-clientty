package dk.client67.modules;

import dk.client67.render.Projector;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.ModeSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** ESP: bokse om spillere og mobs gennem vægge, som 3D-wireframe eller 2D-ramme. */
public class EspModule extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "3D", "3D", "2D"));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive mobs", false));
    private final BoolSetting items = add(new BoolSetting("Items", false));
    private final BoolSetting names = add(new BoolSetting("Names", true));
    private final BoolSetting fill = add(new BoolSetting("Fill (2D)", false));
    private final NumberSetting range = add(new NumberSetting("Range", 96, 16, 256, 8));

    private final Projector proj = new Projector();

    public EspModule() {
        super("ESP", "Viser spillere og mobs gennem vægge (3D eller 2D).", Category.RENDER, false);
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        if (mc.player == null || mc.world == null) return;
        proj.update(ctx, pt);

        Entity camera = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        double maxSq = range.get() * range.get();
        boolean wire = style.index() == 0;

        for (Entity e : mc.world.getEntities()) {
            if (e == camera) continue;
            int color = colorFor(e);
            if (color == 0) continue;

            double x = MathHelper.lerp(pt, e.lastRenderX, e.getX());
            double y = MathHelper.lerp(pt, e.lastRenderY, e.getY());
            double z = MathHelper.lerp(pt, e.lastRenderZ, e.getZ());

            double dx = x - proj.ex, dy = y - proj.ey, dz = z - proj.ez;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxSq) continue;

            double hw = e.getWidth() / 2.0, h = e.getHeight();
            int[] b = proj.drawBox(ctx, x - hw, y, z - hw, x + hw, y + h, z + hw, color, wire, fill.get());
            if (b != null && names.get()) {
                String name = e instanceof ItemEntity item
                        ? item.getStack().getName().getString()
                        : e.getName().getString();
                String label = name + " " + (int) Math.sqrt(distSq) + "m";
                int tx = (b[0] + b[2]) / 2 - mc.textRenderer.getWidth(label) / 2;
                ctx.drawTextWithShadow(mc.textRenderer, label, tx, b[1] - 10, color);
            }
        }
    }

    private int colorFor(Entity e) {
        if (e instanceof PlayerEntity) return players.get() ? 0xFF55FFFF : 0;
        if (e instanceof Monster) return hostile.get() ? 0xFFFF5555 : 0;
        if (e instanceof PassiveEntity) return passive.get() ? 0xFF55FF55 : 0;
        if (e instanceof ItemEntity) return items.get() ? 0xFFFFFF55 : 0;
        return 0;
    }
}
