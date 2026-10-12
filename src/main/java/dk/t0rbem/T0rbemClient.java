package dk.t0rbem;

import dk.t0rbem.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T0rbemClient implements ClientModInitializer {
    public static final String NAME = "t0rbem client";
    public static final String ID = "t0rbem";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    public static KeyBinding guiKey;
    public static KeyBinding hudKey;
    public static KeyBinding zoomKey;

    public static boolean hudVisible = true;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.t0rbem.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        hudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.t0rbem.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.t0rbem.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ModuleManager.init();
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(ID, "hud"),
                T0rbemHud::render);

        LOGGER.info("{} indlæst", NAME);
    }

    private void onTick(MinecraftClient mc) {
        while (hudKey.wasPressed()) hudVisible = !hudVisible;
        while (guiKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
        }
        if (mc.player != null) ModuleManager.tick();
    }
}
