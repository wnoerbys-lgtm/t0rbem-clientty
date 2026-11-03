package dk.client67.modules;

import dk.client67.render.Projector;
import dk.client67.settings.BoolSetting;
import dk.client67.settings.ModeSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

/** Viser kister, tønder, shulkers m.m. gennem vægge i de chunks din klient har indlæst. */
public class StorageEspModule extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "3D", "3D", "2D"));
    private final BoolSetting chests = add(new BoolSetting("Chests", true));
    private final BoolSetting enderChests = add(new BoolSetting("Ender chests", true));
    private final BoolSetting barrels = add(new BoolSetting("Barrels", true));
    private final BoolSetting shulkers = add(new BoolSetting("Shulker boxes", true));
    private final BoolSetting furnaces = add(new BoolSetting("Furnaces", false));
    private final BoolSetting hoppers = add(new BoolSetting("Hoppers", false));
    private final BoolSetting dispensers = add(new BoolSetting("Dispensers/droppers", false));
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final BoolSetting names = add(new BoolSetting("Names", false));
    private final NumberSetting range = add(new NumberSetting("Range", 64, 16, 160, 8));

    private final Projector proj = new Projector();

    public StorageEspModule() {
        super("Storage ESP", "Viser kister, tønder, shulkers m.m. gennem vægge.", Category.RENDER, false);
    }

    private String nameFor(BlockEntity be) {
        if (be instanceof TrappedChestBlockEntity) return chests.get() ? "Trapped chest" : null;
        if (be instanceof ChestBlockEntity) return chests.get() ? "Chest" : null;
        if (be instanceof EnderChestBlockEntity) return enderChests.get() ? "Ender chest" : null;
        if (be instanceof BarrelBlockEntity) return barrels.get() ? "Barrel" : null;
        if (be instanceof ShulkerBoxBlockEntity) return shulkers.get() ? "Shulker" : null;
        if (be instanceof AbstractFurnaceBlockEntity) return furnaces.get() ? "Furnace" : null;
        if (be instanceof HopperBlockEntity) return hoppers.get() ? "Hopper" : null;
        if (be instanceof DispenserBlockEntity) return dispensers.get() ? "Dispenser" : null;
        return null;
    }

    private static int colorFor(String name) {
        return switch (name) {
            case "Chest" -> 0xFFFFAA00;
            case "Trapped chest" -> 0xFFFF5555;
            case "Ender chest" -> 0xFFAA55FF;
            case "Barrel" -> 0xFFCC8844;
            case "Shulker" -> 0xFFFF77CC;
            case "Furnace" -> 0xFFCCCCCC;
            case "Hopper" -> 0xFF888888;
            default -> 0xFF99AACC;
        };
    }

    @Override
    public void renderHud(DrawContext ctx, float pt) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        proj.update(ctx, pt);

        int pcx = ((int) Math.floor(p.getX())) >> 4;
        int pcz = ((int) Math.floor(p.getZ())) >> 4;
        int rc = (int) Math.ceil(range.get() / 16.0);
        double maxSq = range.get() * range.get();

        for (int cx = pcx - rc; cx <= pcx + rc; cx++) {
            for (int cz = pcz - rc; cz <= pcz + rc; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    String name = nameFor(be);
                    if (name == null) continue;

                    BlockPos bp = be.getPos();
                    double dx = bp.getX() + 0.5 - proj.ex;
                    double dy = bp.getY() + 0.5 - proj.ey;
                    double dz = bp.getZ() + 0.5 - proj.ez;
                    if (dx * dx + dy * dy + dz * dz > maxSq) continue;

                    int color = colorFor(name);
                    int[] b = proj.drawBox(ctx, bp.getX(), bp.getY(), bp.getZ(),
                            bp.getX() + 1, bp.getY() + 1, bp.getZ() + 1, color, style.index() == 0, fill.get());
                    if (b == null) continue;
                    if (names.get()) {
                        int tx = (b[0] + b[2]) / 2 - mc.textRenderer.getWidth(name) / 2;
                        ctx.drawTextWithShadow(mc.textRenderer, name, tx, b[1] - 10, color);
                    }
                }
            }
        }
    }
}
