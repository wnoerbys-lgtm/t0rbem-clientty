package dk.client67;

import dk.client67.modules.HudModule;
import dk.client67.modules.Module;
import dk.client67.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class Client67Hud {
    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Client67.hudVisible || HudEditorScreen.active) return; // editoren tegner selv
        if (mc.player == null || mc.options.hudHidden) return;

        float pt = tickCounter.getTickProgress(false);
        for (Module m : ModuleManager.all()) {
            if (m.enabled && !(m instanceof HudModule)) m.renderHud(ctx, pt);
        }

        for (HudModule m : ModuleManager.hud()) {
            if (m.enabled) m.render(ctx);
        }
    }
}
