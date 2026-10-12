package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.Module;
import dk.t0rbem.modules.ModuleManager;
import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.ModeSetting;
import dk.t0rbem.settings.NumberSetting;
import dk.t0rbem.settings.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Venstreklik = slå modul til/fra. Højreklik = vis/skjul indstillinger.
 * Immediate-mode: alle klikbare områder genopbygges hver frame.
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 124, ROW = 14, GAP = 8, X0 = 14, Y0 = 14;
    private static final int ACCENT = 0xFF1D8F6A, BG = 0xDD0E1217, HOVER = 0x22FFFFFF;

    private static final Set<Module> EXPANDED = new HashSet<>();

    private static final class Hit {
        final int x, y, w, h;
        Runnable left, right;
        NumberSetting slider;
        int barX, barW;

        Hit(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragHit;
    private String tip;

    public ClickGuiScreen() {
        super(Text.literal(T0rbemClient.NAME));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void removed() { Config.save(); }

    private Hit addHit(int x, int y, int w, int h) {
        Hit hit = new Hit(x, y, w, h);
        hits.add(hit);
        return hit;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- tegning ----------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x77000000);
        hits.clear();
        tip = null;

        int px = X0;
        for (Module.Category cat : Module.Category.values()) {
            drawModulePanel(ctx, px, cat, mouseX, mouseY);
            px += W + GAP;
        }
        drawClientPanel(ctx, px, mouseX, mouseY);

        String t = tip != null ? tip : "Venstreklik: til/fra  |  Højreklik: indstillinger";
        ctx.drawTextWithShadow(textRenderer, t, (width - textRenderer.getWidth(t)) / 2, height - 16, 0xFFCCCCCC);
    }

    private int heightOf(Module m) {
        int h = ROW;
        if (EXPANDED.contains(m)) {
            for (Setting s : m.settings()) h += s instanceof NumberSetting ? 22 : ROW;
        }
        return h;
    }

    private void drawModulePanel(DrawContext ctx, int px, Module.Category cat, int mx, int my) {
        List<Module> mods = ModuleManager.in(cat);
        int total = ROW + 2;
        for (Module m : mods) total += heightOf(m);

        ctx.fill(px, Y0, px + W, Y0 + total, BG);
        ctx.fill(px, Y0, px + W, Y0 + ROW + 2, ACCENT);
        ctx.drawTextWithShadow(textRenderer, cat.label, px + 5, Y0 + 4, 0xFFFFFFFF);

        int y = Y0 + ROW + 2;
        for (Module m : mods) y = drawModule(ctx, px, y, m, mx, my);
    }

    private int drawModule(DrawContext ctx, int px, int y, Module m, int mx, int my) {
        boolean hov = in(mx, my, px, y, W, ROW);
        if (m.enabled) ctx.fill(px, y, px + W, y + ROW, 0x551D8F6A);
        if (hov) {
            ctx.fill(px, y, px + W, y + ROW, HOVER);
            tip = m.description;
        }
        ctx.drawTextWithShadow(textRenderer, m.name, px + 5, y + 3, m.enabled ? 0xFF7CFFB2 : 0xFFBBBBBB);

        boolean has = !m.settings().isEmpty();
        boolean open = EXPANDED.contains(m);
        if (has) {
            String arrow = open ? "v" : ">";
            ctx.drawTextWithShadow(textRenderer, arrow, px + W - textRenderer.getWidth(arrow) - 5, y + 3, 0xFF888888);
        }

        Hit h = addHit(px, y, W, ROW);
        h.left = m::toggle;
        if (has) h.right = () -> toggleOpen(m);

        y += ROW;
        if (open) {
            for (Setting s : m.settings()) y = drawSetting(ctx, px, y, s, mx, my);
        }
        return y;
    }

    private int drawSetting(DrawContext ctx, int px, int y, Setting s, int mx, int my) {
        int sx = px + 4, sw = W - 4;

        if (s instanceof NumberSetting n) {
            int h = 22;
            ctx.fill(sx, y, px + W, y + h, 0x33000000);
            ctx.drawTextWithShadow(textRenderer, n.name, sx + 4, y + 3, 0xFFAAAAAA);
            String v = n.display();
            ctx.drawTextWithShadow(textRenderer, v, px + W - textRenderer.getWidth(v) - 4, y + 3, 0xFFFFFFFF);
            int bx = sx + 4, bw = sw - 8;
            ctx.fill(bx, y + 15, bx + bw, y + 18, 0xFF333333);
            ctx.fill(bx, y + 15, bx + (int) (bw * n.progress()), y + 18, ACCENT);
            Hit hit = addHit(sx, y, sw, h);
            hit.slider = n;
            hit.barX = bx;
            hit.barW = bw;
            return y + h;
        }

        ctx.fill(sx, y, px + W, y + ROW, 0x33000000);
        if (in(mx, my, sx, y, sw, ROW)) ctx.fill(sx, y, px + W, y + ROW, HOVER);
        ctx.drawTextWithShadow(textRenderer, s.name, sx + 4, y + 3, 0xFFAAAAAA);

        String value;
        int valueColor = 0xFFFFFFFF;
        Runnable action;
        if (s instanceof BoolSetting b) {
            value = b.get() ? "ON" : "OFF";
            valueColor = b.get() ? 0xFF55FF88 : 0xFFFF5555;
            action = b::toggle;
        } else if (s instanceof ModeSetting mo) {
            value = mo.get();
            action = mo::cycle;
        } else {
            return y + ROW;
        }
        ctx.drawTextWithShadow(textRenderer, value, px + W - textRenderer.getWidth(value) - 4, y + 3, valueColor);
        addHit(sx, y, sw, ROW).left = action;
        return y + ROW;
    }

    private void drawClientPanel(DrawContext ctx, int px, int mx, int my) {
        int rows = 3;
        int total = ROW + 2 + rows * ROW;
        ctx.fill(px, Y0, px + W, Y0 + total, BG);
        ctx.fill(px, Y0, px + W, Y0 + ROW + 2, ACCENT);
        ctx.drawTextWithShadow(textRenderer, "Client", px + 5, Y0 + 4, 0xFFFFFFFF);

        int y = Y0 + ROW + 2;
        y = clientRow(ctx, px, y, mx, my, "Edit HUD layout", null, 0xFFFFFFFF,
                "Træk HUD-elementerne hen hvor du vil have dem.",
                () -> mc.setScreen(new HudEditorScreen()));
        y = clientRow(ctx, px, y, mx, my, "HUD",
                T0rbemClient.hudVisible ? "ON" : "OFF",
                T0rbemClient.hudVisible ? 0xFF55FF88 : 0xFFFF5555,
                "Slå hele HUD'en til eller fra.",
                () -> T0rbemClient.hudVisible = !T0rbemClient.hudVisible);
        clientRow(ctx, px, y, mx, my, "Reset HUD positions", null, 0xFFFFFFFF,
                "Sæt alle HUD-elementer tilbage til standardpositionerne.",
                () -> { for (HudModule h : ModuleManager.hud()) h.resetPosition(); });
    }

    private int clientRow(DrawContext ctx, int px, int y, int mx, int my,
                          String label, String value, int valueColor, String tooltip, Runnable action) {
        if (in(mx, my, px, y, W, ROW)) {
            ctx.fill(px, y, px + W, y + ROW, HOVER);
            tip = tooltip;
        }
        ctx.drawTextWithShadow(textRenderer, label, px + 5, y + 3, 0xFFDDDDDD);
        if (value != null) {
            ctx.drawTextWithShadow(textRenderer, value, px + W - textRenderer.getWidth(value) - 5, y + 3, valueColor);
        }
        addHit(px, y, W, ROW).left = action;
        return y + ROW;
    }

    private static void toggleOpen(Module m) {
        if (!EXPANDED.remove(m)) EXPANDED.add(m);
    }

    // ---------- input ----------

    private static void applySlider(Hit h, double mouseX) {
        NumberSetting n = h.slider;
        double p = Math.max(0, Math.min(1, (mouseX - h.barX) / h.barW));
        n.set(n.min + p * (n.max - n.min));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        for (Hit h : hits) {
            if (!h.contains(mx, my)) continue;
            if (click.button() == 0) {
                if (h.slider != null) {
                    dragHit = h;
                    applySlider(h, mx);
                } else if (h.left != null) {
                    h.left.run();
                }
            } else if (click.button() == 1 && h.right != null) {
                h.right.run();
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragHit != null) {
            applySlider(dragHit, click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragHit = null;
        return super.mouseReleased(click);
    }
}
