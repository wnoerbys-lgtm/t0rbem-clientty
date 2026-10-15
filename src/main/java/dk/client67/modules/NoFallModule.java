package dk.client67.modules;

import dk.client67.settings.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFallModule extends Module {
    private final NumberSetting minFall = add(new NumberSetting("Min fall distance", 2.5, 1, 10, 0.5));

    public NoFallModule() {
        super("NoFall", "Undgår faldskade ved at fortælle serveren, at du står på jorden.", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p == null || p.isOnGround() || mc.getNetworkHandler() == null) return;
        if (p.fallDistance > minFall.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
        }
    }
}
