package dk.client67;

import dk.client67.modules.Module;
import dk.client67.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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

public class Client67 implements ClientModInitializer {
    public static final String NAME = "67client";
    public static final String ID = "client67";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    public static KeyBinding guiKey;
    public static KeyBinding hudKey;
    public static KeyBinding zoomKey;
    public static KeyBinding flightKey;

    public static boolean hudVisible = true;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.client67.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        hudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.client67.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.client67.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        flightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.client67.flight", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

        ModuleManager.init();
        Config.load();
        ChunkTracker.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Config.save();
            ChunkTracker.save();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ChunkTracker.save());
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(ID, "hud"),
                Client67Hud::render);

        LOGGER.info("{} indlæst", NAME);
    }

    private void onTick(MinecraftClient mc) {
        while (hudKey.wasPressed()) hudVisible = !hudVisible;
        while (flightKey.wasPressed()) {
            Module flight = ModuleManager.get("Flight");
            if (flight != null) flight.toggle();
        }
        while (guiKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
        }
        if (mc.player != null) ModuleManager.tick();
    }
}
