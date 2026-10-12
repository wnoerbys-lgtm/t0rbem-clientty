package dk.t0rbem.modules;

import com.google.gson.JsonObject;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.ModeSetting;
import net.minecraft.client.gui.DrawContext;

/** Et flytbart HUD-element. */
public abstract class HudModule extends Module {
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFF55FF55, 0xFF55FFFF, 0xFFFFAA00, 0xFFFF5555, 0xFFFF77CC, 0xFFFFFF55
    };

    public int x, y;
    private final int defX, defY;

    protected final BoolSetting background = add(new BoolSetting("Background", true));
    protected final ModeSetting color = add(new ModeSetting("Color", "White",
            "White", "Green", "Aqua", "Gold", "Red", "Pink", "Yellow"));

    protected HudModule(String name, String description, boolean enabled, int x, int y) {
        super(name, description, Category.HUD, enabled);
        this.x = x;
        this.y = y;
        this.defX = x;
        this.defY = y;
    }

    /** Opdater data før størrelse/tegning. */
    protected void prepare() {}

    public abstract int width();
    public abstract int height();
    protected abstract void draw(DrawContext ctx, int x, int y);

    protected boolean wantsBackground() { return background.get(); }

    protected int textColor() { return COLORS[color.index()]; }

    public void resetPosition() {
        x = defX;
        y = defY;
    }

    public final void render(DrawContext ctx) {
        prepare();
        int w = width(), h = height();
        if (w <= 0 || h <= 0) return;
        x = Math.max(0, Math.min(x, ctx.getScaledWindowWidth() - w));
        y = Math.max(0, Math.min(y, ctx.getScaledWindowHeight() - h));
        if (wantsBackground()) ctx.fill(x, y, x + w, y + h, 0x66000000);
        draw(ctx, x, y);
    }

    public boolean contains(double mx, double my) {
        prepare();
        return mx >= x && mx < x + width() && my >= y && my < y + height();
    }

    @Override
    public JsonObject save() {
        JsonObject o = super.save();
        o.addProperty("x", x);
        o.addProperty("y", y);
        return o;
    }

    @Override
    public void load(JsonObject o) {
        super.load(o);
        try {
            if (o.has("x")) x = o.get("x").getAsInt();
            if (o.has("y")) y = o.get("y").getAsInt();
        } catch (Exception ignored) {}
    }
}
