package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/** Træk HUD-elementerne hen hvor du vil have dem. ESC gemmer og lukker. */
public class HudEditorScreen extends Screen {
    public static boolean active;

    private HudModule dragging;
    private int offX, offY;

    public HudEditorScreen() {
        super(Text.literal("HUD editor"));
        active = true;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void removed() {
        active = false;
        Config.save();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x44000000);

        for (HudModule m : ModuleManager.hud()) {
            if (!m.enabled) continue;
            m.render(ctx);
            int w = m.width(), h = m.height();
            if (w <= 0 || h <= 0) continue;
            int c = m == dragging ? 0xFF55FF88 : 0xAAFFFFFF;
            ctx.fill(m.x - 1, m.y - 1, m.x + w + 1, m.y, c);
            ctx.fill(m.x - 1, m.y + h, m.x + w + 1, m.y + h + 1, c);
            ctx.fill(m.x - 1, m.y, m.x, m.y + h, c);
            ctx.fill(m.x + w, m.y, m.x + w + 1, m.y + h, c);
        }

        String hint = "Træk i elementerne - ESC gemmer og lukker";
        ctx.drawTextWithShadow(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height / 2 - 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            List<HudModule> list = ModuleManager.hud();
            for (int i = list.size() - 1; i >= 0; i--) {
                HudModule m = list.get(i);
                if (m.enabled && m.contains(click.x(), click.y())) {
                    dragging = m;
                    offX = (int) (click.x() - m.x);
                    offY = (int) (click.y() - m.y);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            dragging.x = (int) (click.x() - offX);
            dragging.y = (int) (click.y() - offY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }
}
