package dk.client67.modules;

import dk.client67.Client67;
import java.util.List;

public class WatermarkModule extends TextHudModule {
    public WatermarkModule() { super("Watermark", "Viser klientens navn.", true, 4, 4); }

    @Override
    protected List<String> lines() { return List.of(Client67.NAME); }
}
