package dk.client67.modules;

import dk.client67.settings.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;

public class FlightModule extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 0.1, 3.0, 0.1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical speed", 0.4, 0.1, 2.0, 0.1));

    public FlightModule() {
        super("Flight", "Flyv frit: WASD, Mellemrum op, Shift ned. (G slår til/fra)", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        p.setNoGravity(true);
        if (mc.currentScreen != null) {
            p.setVelocity(0, 0, 0);
            return;
        }

        double f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double s = (mc.options.rightKey.isPressed() ? 1 : 0) - (mc.options.leftKey.isPressed() ? 1 : 0);

        double yaw = Math.toRadians(p.getYaw());
        double vx = -Math.sin(yaw) * f - Math.cos(yaw) * s;
        double vz = Math.cos(yaw) * f - Math.sin(yaw) * s;
        double len = Math.hypot(vx, vz);
        if (len > 0) {
            vx = vx / len * speed.get();
            vz = vz / len * speed.get();
        }

        double vy = ((mc.options.jumpKey.isPressed() ? 1 : 0) - (mc.options.sneakKey.isPressed() ? 1 : 0))
                * vertical.get();

        p.setVelocity(vx, vy, vz);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setNoGravity(false);
    }
}
