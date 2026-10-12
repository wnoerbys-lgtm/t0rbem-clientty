package dk.t0rbem.modules;

import dk.t0rbem.T0rbemClient;
import dk.t0rbem.settings.NumberSetting;

public class ZoomModule extends Module {
    private final NumberSetting zoomFov = add(new NumberSetting("Zoom FOV", 30, 10, 60, 1));
    private int savedFov = -1;

    public ZoomModule() {
        super("Zoom", "Hold C nede for at zoome.", Category.UTILITY, true);
    }

    @Override
    public void onTick() {
        var fov = mc.options.getFov();
        if (T0rbemClient.zoomKey.isPressed()) {
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
