package dk.t0rbem.modules;

import java.util.List;

public class FpsModule extends TextHudModule {
    public FpsModule() { super("FPS", "Viser billeder per sekund.", true, 4, 20); }

    @Override
    protected List<String> lines() { return List.of("FPS: " + mc.getCurrentFps()); }
}
