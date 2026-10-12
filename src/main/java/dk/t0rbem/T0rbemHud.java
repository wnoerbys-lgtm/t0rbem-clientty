package dk.t0rbem;

import dk.t0rbem.modules.HudModule;
import dk.t0rbem.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class T0rbemHud {
    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!T0rbemClient.hudVisible || HudEditorScreen.active) return; // editoren tegner selv
        if (mc.player == null || mc.options.hudHidden) return;

        for (HudModule m : ModuleManager.hud()) {
            if (m.enabled) m.render(ctx);
        }
    }
}
