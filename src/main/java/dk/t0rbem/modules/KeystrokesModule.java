package dk.t0rbem.modules;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesModule extends HudModule {
    private static final int S = 20, GAP = 2, SPACE_H = 10;

    public KeystrokesModule() {
        super("Keystrokes", "Viser WASD og mellemrum.", true, 4, 9999);
        settings.remove(background); // tegner sine egne taster
    }

    @Override
    protected boolean wantsBackground() { return false; }

    @Override
    public int width() { return S * 3 + GAP * 2; }

    @Override
    public int height() { return (S + GAP) * 2 + SPACE_H; }

    @Override
    protected void draw(DrawContext ctx, int x, int y) {
        key(ctx, x + S + GAP, y, "W", mc.options.forwardKey);
        key(ctx, x, y + S + GAP, "A", mc.options.leftKey);
        key(ctx, x + S + GAP, y + S + GAP, "S", mc.options.backKey);
        key(ctx, x + (S + GAP) * 2, y + S + GAP, "D", mc.options.rightKey);

        int sy = y + (S + GAP) * 2;
        boolean down = mc.options.jumpKey.isPressed();
        ctx.fill(x, sy, x + width(), sy + SPACE_H, down ? 0xAAFFFFFF : 0x88000000);
        ctx.fill(x + 8, sy + 4, x + width() - 8, sy + 6, down ? 0xFF000000 : textColor());
    }

    private void key(DrawContext ctx, int x, int y, String label, KeyBinding key) {
        boolean down = key.isPressed();
        ctx.fill(x, y, x + S, y + S, down ? 0xAAFFFFFF : 0x88000000);
        int tx = x + (S - mc.textRenderer.getWidth(label)) / 2;
        ctx.drawText(mc.textRenderer, label, tx, y + 6, down ? 0xFF000000 : textColor(), false);
    }
}
