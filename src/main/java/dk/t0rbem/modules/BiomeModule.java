package dk.t0rbem.modules;

import java.util.List;

public class BiomeModule extends TextHudModule {
    public BiomeModule() { super("Biome", "Viser det biome du står i.", false, 4, 126); }

    @Override
    protected List<String> lines() {
        if (mc.player == null || mc.world == null) return List.of();
        String name = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                .map(k -> k.getValue().getPath().replace('_', ' '))
                .orElse("unknown");
        return List.of("Biome: " + name);
    }
}
