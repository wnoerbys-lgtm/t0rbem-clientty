package dk.t0rbem.modules;

import dk.t0rbem.HudEditorScreen;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** HUD-element der bare viser nogle tekstlinjer. */
public abstract class TextHudModule extends HudModule {
    private static final int LINE = 10, PAD = 2;
    private List<String> cached = List.of();

    protected TextHudModule(String name, String description, boolean enabled, int x, int y) {
        super(name, description, enabled, x, y);
    }

    protected abstract List<String> lines();

    @Override
    protected void prepare() {
        List<String> l = lines();
        if ((l == null || l.isEmpty()) && HudEditorScreen.active) l = List.of("[" + name + "]");
        cached = l == null ? List.of() : l;
    }

    @Override
    public int width() {
        int w = 0;
        for (String s : cached) w = Math.max(w, mc.textRenderer.getWidth(s));
        return cached.isEmpty() ? 0 : w + PAD * 2;
    }

    @Override
    public int height() {
        return cached.isEmpty() ? 0 : cached.size() * LINE + PAD + 1;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y) {
        int c = textColor();
        for (int i = 0; i < cached.size(); i++) {
            ctx.drawTextWithShadow(mc.textRenderer, cached.get(i), x + PAD, y + PAD + i * LINE, c);
        }
    }
}
