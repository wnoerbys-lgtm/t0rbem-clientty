package dk.client67;

import dk.client67.modules.HudModule;
import dk.client67.modules.Module;
import dk.client67.modules.ModuleManager;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.ModeSetting;
import dk.client67.settings.NumberSetting;
import dk.client67.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 67client GUI: flydende vindue med sidebar, glidende kontakter, animerede kort,
 * smooth scroll, temaer og partikler.
 */
public class ClickGuiScreen extends Screen {
    private static final int SIDEBAR = 86, HEADER = 28, FOOTER = 16, CARD = 22, GAP = 5;
    private static final int BG = 0xF00B0F14, SIDE_BG = 0xFF0F151C;
    private static final int CARD_BG = 0xFF141B22, CARD_HOVER = 0xFF1B252E;
    private static final int TEXT = 0xFFE6EDF3, DIM = 0xFF8B97A3, OFF = 0xFF2A333C;

    private enum Tab {
        HUD("HUD", Module.Category.HUD),
        UTILITY("Utility", Module.Category.UTILITY),
        RENDER("Render", Module.Category.RENDER),
        CLIENT("Client", null);

        final String label;
        final Module.Category category;

        Tab(String label, Module.Category category) {
            this.label = label;
            this.category = category;
        }
    }

    private static final class Hit {
        final int x, y, w, h;
        final boolean content;
        Runnable left, right;
        NumberSetting slider;
        int barX, barW;

        Hit(int x, int y, int w, int h, boolean content) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.content = content;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static Tab tab = Tab.HUD;
    private static final Set<Module> EXPANDED = new HashSet<>();

    private final List<Hit> hits = new ArrayList<>();
    private final Map<Object, Float> anims = new HashMap<>();
    private final List<float[]> particles = new ArrayList<>();
    private final Random random = new Random();

    private Hit dragHit;
    private String tip;
    private long last;
    private float dt, openT, scroll, scrollTarget, tabY = -1;
    private int contentH;
    private int wx, wy, ww, wh, cx, cy, cw, ch;

    public ClickGuiScreen() {
        super(Text.literal(Client67.NAME));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void removed() { Config.save(); }

    // ---------- hjælpere ----------

    private static void rect(DrawContext ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x + 1, y, x + w - 1, y + h, c);
        ctx.fill(x, y + 1, x + w, y + h - 1, c);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean inContent(double mx, double my) {
        return in(mx, my, cx - 2, cy, cw + 4, ch);
    }

    private float anim(Object key, float target, float speed) {
        float cur = anims.getOrDefault(key, target);
        cur += (target - cur) * Math.min(1f, dt * speed);
        anims.put(key, cur);
        return cur;
    }

    private Hit addHit(int x, int y, int w, int h, boolean content) {
        Hit hit = new Hit(x, y, w, h, content);
        hits.add(hit);
        return hit;
    }

    private void drawSwitch(DrawContext ctx, int x, int y, float t) {
        rect(ctx, x, y, 22, 10, Theme.mix(OFF, Theme.accent(), t));
        rect(ctx, x + 1 + (int) (t * 12), y + 1, 8, 8, 0xFFFFFFFF);
    }

    private static void toggleOpen(Module m) {
        if (!EXPANDED.remove(m)) EXPANDED.add(m);
    }

    // ---------- render ----------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        dt = last == 0 ? 0f : Math.min(0.05f, (now - last) / 1000f);
        last = now;
        openT = Math.min(1f, openT + dt / 0.25f);
        float e = 1f - (float) Math.pow(1f - openT, 3);

        ctx.fill(0, 0, width, height, ((int) (0x99 * e)) << 24);
        if (Theme.particles) drawParticles(ctx);

        ww = Math.min(width - 24, 440);
        wh = Math.min(height - 24, 270);
        wx = (width - ww) / 2;
        wy = (height - wh) / 2 + (int) ((1f - e) * 20);
        cx = wx + SIDEBAR + 8;
        cy = wy + HEADER + 2 + 6;
        cw = ww - SIDEBAR - 16;
        ch = wh - HEADER - 2 - FOOTER - 12;

        hits.clear();
        tip = null;

        rect(ctx, wx - 3, wy - 3, ww + 6, wh + 6, 0x33000000);
        rect(ctx, wx - 1, wy - 1, ww + 2, wh + 2, Theme.mix(0xFF0B0F14, Theme.accent(), 0.5f));
        rect(ctx, wx, wy, ww, wh, BG);

        drawHeader(ctx);
        drawSidebar(ctx, mouseX, mouseY);
        drawContent(ctx, mouseX, mouseY);
        drawFooter(ctx);
    }

    private void drawParticles(DrawContext ctx) {
        if (particles.isEmpty()) {
            for (int i = 0; i < 70; i++) {
                particles.add(new float[]{
                        random.nextFloat() * width, random.nextFloat() * height,
                        (random.nextFloat() - 0.5f) * 8f, 6f + random.nextFloat() * 14f,
                        1 + random.nextInt(2), 0.15f + random.nextFloat() * 0.35f});
            }
        }
        int accent = Theme.accent() & 0xFFFFFF;
        for (int i = 0; i < particles.size(); i++) {
            float[] p = particles.get(i);
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            if (p[1] > height) { p[1] = -2; p[0] = random.nextFloat() * width; }
            if (p[0] < 0) p[0] = width;
            if (p[0] > width) p[0] = 0;
            int a = (int) (p[5] * 255);
            int col = (a << 24) | (i % 3 == 0 ? accent : 0xFFFFFF);
            int s = (int) p[4];
            ctx.fill((int) p[0], (int) p[1], (int) p[0] + s, (int) p[1] + s, col);
        }
    }

    private void drawHeader(DrawContext ctx) {
        for (int i = 1; i < ww - 1; i += 3) {
            float t = (float) i / ww;
            int x2 = Math.min(wx + ww - 1, wx + i + 3);
            ctx.fill(wx + i, wy + 1, x2, wy + HEADER, Theme.mix(0xFF0B0F14, Theme.at(t), 0.38f));
            ctx.fill(wx + i, wy + HEADER, x2, wy + HEADER + 2, Theme.at(t));
        }
        ctx.drawTextWithShadow(textRenderer, Client67.NAME, wx + 10, wy + 10, 0xFFFFFFFF);

        int active = 0;
        for (Module m : ModuleManager.all()) if (m.enabled) active++;
        String s = active + " active";
        ctx.drawTextWithShadow(textRenderer, s, wx + ww - 10 - textRenderer.getWidth(s), wy + 10, 0xFFE0E8F0);
    }

    private void drawSidebar(DrawContext ctx, int mx, int my) {
        int sx = wx + 1, sy = wy + HEADER + 2;
        int sh = wh - HEADER - 2 - FOOTER;
        ctx.fill(sx, sy, sx + SIDEBAR, sy + sh, SIDE_BG);

        Tab[] tabs = Tab.values();
        float target = sy + 8 + tab.ordinal() * 28;
        if (tabY < 0) tabY = target;
        tabY += (target - tabY) * Math.min(1f, dt * 16f);

        rect(ctx, sx + 5, (int) tabY, SIDEBAR - 10, 22, Theme.mix(0xFF0F151C, Theme.accent(), 0.28f));
        ctx.fill(sx + 5, (int) tabY + 3, sx + 7, (int) tabY + 19, Theme.accent());

        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            int ty = sy + 8 + i * 28;
            boolean sel = t == tab;
            boolean hov = in(mx, my, sx + 5, ty, SIDEBAR - 10, 22);
            float s = anim(t, sel ? 1f : 0f, 14f);
            if (hov && !sel) rect(ctx, sx + 5, ty, SIDEBAR - 10, 22, 0x22FFFFFF);
            ctx.drawTextWithShadow(textRenderer, t.label, sx + 14, ty + 7, Theme.mix(DIM, TEXT, s));

            if (t.category != null) {
                int on = 0;
                for (Module m : ModuleManager.in(t.category)) if (m.enabled) on++;
                if (on > 0) {
                    String n = String.valueOf(on);
                    ctx.drawTextWithShadow(textRenderer, n, sx + SIDEBAR - 12 - textRenderer.getWidth(n), ty + 7,
                            Theme.accent());
                }
            }
            Hit h = addHit(sx + 5, ty, SIDEBAR - 10, 22, false);
            h.left = () -> { tab = t; scroll = 0; scrollTarget = 0; };
        }
    }

    private void drawFooter(DrawContext ctx) {
        int y = wy + wh - FOOTER;
        ctx.fill(wx + 1, y, wx + ww - 1, y + FOOTER - 1, 0xFF0A0E13);
        String t = tip != null ? tip : "Klik: til/fra  |  Højreklik: indstillinger  |  Scroll: rul";
        ctx.drawTextWithShadow(textRenderer, t, wx + 10, y + 4, DIM);
    }

    private void drawContent(DrawContext ctx, int mx, int my) {
        ctx.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);
        int start = cy - (int) scroll;
        int y = start;
        if (tab == Tab.CLIENT) {
            y = drawClientItems(ctx, cx, y, cw, mx, my);
        } else {
            for (Module m : ModuleManager.in(tab.category)) y = drawModule(ctx, cx, y, cw, m, mx, my);
        }
        ctx.disableScissor();

        contentH = y - start;
        float max = Math.max(0, contentH - ch);
        scrollTarget = Math.max(0, Math.min(scrollTarget, max));
        scroll += (scrollTarget - scroll) * Math.min(1f, dt * 14f);
        scroll = Math.max(0, Math.min(scroll, max));

        if (max > 0) {
            int bh = Math.max(16, (int) (ch * (float) ch / contentH));
            int by = cy + (int) ((ch - bh) * (scroll / max));
            ctx.fill(cx + cw + 3, by, cx + cw + 5, by + bh, Theme.accent());
        }
    }

    // ---------- moduler ----------

    private int settingsHeight(Module m) {
        int h = 4;
        for (Setting s : m.settings()) h += s instanceof NumberSetting ? 28 : 20;
        return h;
    }

    private int drawModule(DrawContext ctx, int x, int y, int w, Module m, int mx, int my) {
        boolean open = EXPANDED.contains(m);
        boolean has = !m.settings().isEmpty();
        float ex = anim("x:" + m.name, open ? 1f : 0f, 14f);
        float on = anim(m, m.enabled ? 1f : 0f, 14f);
        int setH = settingsHeight(m);
        int h = CARD + (has ? (int) (setH * ex) : 0);

        boolean hov = inContent(mx, my) && in(mx, my, x, y, w, CARD);
        rect(ctx, x, y, w, h, hov ? CARD_HOVER : CARD_BG);
        ctx.fill(x, y + 3, x + 2, y + CARD - 3, Theme.mix(OFF, Theme.accent(), on));
        ctx.drawTextWithShadow(textRenderer, m.name, x + 9, y + 7, Theme.mix(DIM, TEXT, on));
        drawSwitch(ctx, x + w - 30, y + 6, on);
        if (has) ctx.drawTextWithShadow(textRenderer, open ? "v" : ">", x + w - 44, y + 7, DIM);
        if (hov) tip = m.description;

        Hit main = addHit(x, y, w, CARD, true);
        main.left = m::toggle;
        if (has) {
            main.right = () -> toggleOpen(m);
            Hit chev = addHit(x + w - 52, y, 20, CARD, true);
            chev.left = () -> toggleOpen(m);
            chev.right = () -> toggleOpen(m);

            if (ex > 0.01f) {
                ctx.enableScissor(x, y + CARD, x + w, y + h);
                int sy = y + CARD + 2;
                for (Setting s : m.settings()) sy = drawSetting(ctx, x + 6, sy, w - 12, s, ex > 0.98f);
                ctx.disableScissor();
            }
        }
        return y + h + GAP;
    }

    private int drawSetting(DrawContext ctx, int x, int y, int w, Setting s, boolean interactive) {
        if (s instanceof NumberSetting n) {
            ctx.drawTextWithShadow(textRenderer, n.name, x, y + 2, DIM);
            String v = n.display();
            ctx.drawTextWithShadow(textRenderer, v, x + w - textRenderer.getWidth(v), y + 2, TEXT);
            ctx.fill(x, y + 17, x + w, y + 20, 0xFF28323B);
            int fw = (int) (w * n.progress());
            ctx.fill(x, y + 17, x + fw, y + 20, Theme.accent());
            rect(ctx, x + fw - 2, y + 14, 5, 9, 0xFFFFFFFF);
            if (interactive) {
                Hit hit = addHit(x, y, w, 28, true);
                hit.slider = n;
                hit.barX = x;
                hit.barW = w;
            }
            return y + 28;
        }

        ctx.drawTextWithShadow(textRenderer, s.name, x, y + 6, DIM);
        if (s instanceof BoolSetting b) {
            float t = anim(s, b.get() ? 1f : 0f, 14f);
            drawSwitch(ctx, x + w - 22, y + 5, t);
            if (interactive) addHit(x, y, w, 20, true).left = b::toggle;
        } else if (s instanceof ModeSetting mo) {
            String v = mo.get();
            int pw = textRenderer.getWidth(v) + 12;
            rect(ctx, x + w - pw, y + 3, pw, 14, 0xFF1F2A33);
            ctx.drawTextWithShadow(textRenderer, v, x + w - pw + 6, y + 6, Theme.accent());
            if (interactive) addHit(x, y, w, 20, true).left = mo::cycle;
        }
        return y + 20;
    }

    // ---------- client-fanen ----------

    private int drawClientItems(DrawContext ctx, int x, int y, int w, int mx, int my) {
        y = actionRow(ctx, x, y, w, mx, my, "Edit HUD layout", "Open", Theme.accent(),
                "Træk HUD-elementerne hen hvor du vil have dem.",
                () -> MinecraftClient.getInstance().setScreen(new HudEditorScreen()));
        y = toggleRow(ctx, x, y, w, mx, my, "HUD overlay", Client67.hudVisible,
                "Slå hele HUD'en til eller fra.",
                () -> Client67.hudVisible = !Client67.hudVisible);
        y = actionRow(ctx, x, y, w, mx, my, "Reset HUD positions", "Reset", 0xFFFF6B6B,
                "Sæt alle HUD-elementer tilbage til standardpositionerne.",
                () -> { for (HudModule h : ModuleManager.hud()) h.resetPosition(); });
        y = actionRow(ctx, x, y, w, mx, my, "Theme", Theme.NAMES[Theme.index], Theme.accent(),
                "Skift farvetema. Prøv Rainbow!", Theme::cycle);
        y = toggleRow(ctx, x, y, w, mx, my, "Particles", Theme.particles,
                "Flydende partikler i baggrunden.", () -> Theme.particles = !Theme.particles);

        String[] info = {
                "Right Shift  -  åbn GUI",
                "H  -  skjul/vis HUD",
                "G  -  Flight",
                "F4  -  Freecam (indbygget)",
                "C  -  Zoom (hold)",
                "Config: config/67client.json"
        };
        int h = info.length * 11 + 12;
        rect(ctx, x, y, w, h, CARD_BG);
        for (int i = 0; i < info.length; i++) {
            ctx.drawTextWithShadow(textRenderer, info[i], x + 9, y + 7 + i * 11, DIM);
        }
        return y + h + GAP;
    }

    private int actionRow(DrawContext ctx, int x, int y, int w, int mx, int my,
                          String label, String value, int valueColor, String tooltip, Runnable action) {
        boolean hov = inContent(mx, my) && in(mx, my, x, y, w, CARD);
        rect(ctx, x, y, w, CARD, hov ? CARD_HOVER : CARD_BG);
        ctx.fill(x, y + 3, x + 2, y + CARD - 3, valueColor);
        ctx.drawTextWithShadow(textRenderer, label, x + 9, y + 7, TEXT);
        int pw = textRenderer.getWidth(value) + 12;
        rect(ctx, x + w - pw - 6, y + 4, pw, 14, 0xFF1F2A33);
        ctx.drawTextWithShadow(textRenderer, value, x + w - pw, y + 7, valueColor);
        if (hov) tip = tooltip;
        addHit(x, y, w, CARD, true).left = action;
        return y + CARD + GAP;
    }

    private int toggleRow(DrawContext ctx, int x, int y, int w, int mx, int my,
                          String label, boolean state, String tooltip, Runnable action) {
        boolean hov = inContent(mx, my) && in(mx, my, x, y, w, CARD);
        float t = anim("row:" + label, state ? 1f : 0f, 14f);
        rect(ctx, x, y, w, CARD, hov ? CARD_HOVER : CARD_BG);
        ctx.fill(x, y + 3, x + 2, y + CARD - 3, Theme.mix(OFF, Theme.accent(), t));
        ctx.drawTextWithShadow(textRenderer, label, x + 9, y + 7, Theme.mix(DIM, TEXT, t));
        drawSwitch(ctx, x + w - 30, y + 6, t);
        if (hov) tip = tooltip;
        addHit(x, y, w, CARD, true).left = action;
        return y + CARD + GAP;
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

        if (click.button() == 0 && !in(mx, my, wx, wy, ww, wh)) {
            close();
            return true;
        }

        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(mx, my)) continue;
            if (h.content && !inContent(mx, my)) continue;

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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget -= (float) (verticalAmount * 22);
        return true;
    }
}
