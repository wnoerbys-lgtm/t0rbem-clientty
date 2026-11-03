package dk.client67.modules;

import dk.client67.Client67;
import dk.client67.settings.NumberSetting;

public class ZoomModule extends Module {
    private final NumberSetting zoomFov = add(new NumberSetting("Zoom FOV", 30, 10, 60, 1));
    private int savedFov = -1;

    public ZoomModule() {
        super("Zoom", "Hold C nede for at zoome.", Category.RENDER, true);
    }

    @Override
    public void onTick() {
        var fov = mc.options.getFov();
        if (Client67.zoomKey.isPressed()) {
            if (savedFov == -1) savedFov = fov.getValue();
            fov.setValue(zoomFov.getInt());
        } else {
            restore();
        }
    }

    @Override
    public void onDisable() { restore(); }

    private void restore() {
        if (savedFov != -1) {
            mc.options.getFov().setValue(savedFov);
            savedFov = -1;
        }
    }
}
