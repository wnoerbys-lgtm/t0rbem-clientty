package dk.t0rbem.modules;

import dk.t0rbem.T0rbemClient;
import java.util.List;

public class WatermarkModule extends TextHudModule {
    public WatermarkModule() { super("Watermark", "Viser klientens navn.", true, 4, 4); }

    @Override
    protected List<String> lines() { return List.of(T0rbemClient.NAME); }
}
