package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;
import dk.t0rbem.settings.NumberSetting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CoordsModule extends TextHudModule {
    private final NumberSetting decimals = add(new NumberSetting("Decimals", 1, 0, 2, 1));
    private final BoolSetting direction = add(new BoolSetting("Direction", true));
    private final BoolSetting nether = add(new BoolSetting("Nether coords", false));

    public CoordsModule() { super("Coordinates", "Viser XYZ, retning og nether-koordinater.", true, 4, 84); }

    @Override
    protected List<String> lines() {
        if (mc.player == null || mc.world == null) return List.of();
        String f = "%." + decimals.getInt() + "f";
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        List<String> out = new ArrayList<>();
        out.add(String.format(Locale.ROOT, "XYZ: " + f + " / " + f + " / " + f, x, y, z));
        if (direction.get()) out.add("Facing: " + mc.player.getHorizontalFacing().asString());
        if (nether.get()) {
            if (mc.world.getRegistryKey() == World.OVERWORLD) {
                out.add(String.format(Locale.ROOT, "Nether: " + f + " / " + f, x / 8.0, z / 8.0));
            } else if (mc.world.getRegistryKey() == World.NETHER) {
                out.add(String.format(Locale.ROOT, "Overworld: " + f + " / " + f, x * 8.0, z * 8.0));
            }
        }
        return out;
    }
}
