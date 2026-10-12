package dk.t0rbem.modules;

import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionModule extends TextHudModule {
    public PotionModule() { super("Potions", "Viser aktive effekter og tid tilbage.", true, 9999, 4); }

    @Override
    protected List<String> lines() {
        List<String> out = new ArrayList<>();
        if (mc.player == null) return out;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String name = e.getEffectType().value().getName().getString();
            String lvl = e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "";
            out.add(name + lvl + " " + (e.isInfinite() ? "inf" : time(e.getDuration())));
        }
        return out;
    }

    private static String time(int ticks) {
        int s = ticks / 20;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }
}
