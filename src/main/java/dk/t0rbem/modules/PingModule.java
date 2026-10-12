package dk.t0rbem.modules;

import java.util.List;

public class PingModule extends TextHudModule {
    public PingModule() { super("Ping", "Viser din ping til serveren.", true, 4, 36); }

    @Override
    protected List<String> lines() {
        if (mc.player == null || mc.getNetworkHandler() == null) return List.of("Ping: ?");
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return List.of("Ping: " + (entry == null ? "?" : entry.getLatency() + " ms"));
    }
}
