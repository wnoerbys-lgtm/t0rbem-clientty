package dk.t0rbem.modules;

import dk.t0rbem.HudEditorScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorModule extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int ROW = 18;

    public ArmorModule() { super("Armor", "Viser din rustning og holdbarhed.", true, 9999, 60); }

    private boolean hasAny() {
        if (mc.player == null) return false;
        for (EquipmentSlot s : SLOTS) if (!mc.player.getEquippedStack(s).isEmpty()) return true;
        return false;
    }

    @Override
    public int width() {
        return (hasAny() || HudEditorScreen.active) ? 54 : 0;
    }

    @Override
    public int height() {
        return (hasAny() || HudEditorScreen.active) ? ROW * 4 : 0;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y) {
        if (mc.player == null) return;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = mc.player.getEquippedStack(SLOTS[i]);
            if (stack.isEmpty()) continue;
            int ry = y + i * ROW;
            ctx.drawItem(stack, x + 1, ry + 1);
            if (stack.isDamageable()) {
                int left = stack.getMaxDamage() - stack.getDamage();
                ctx.drawTextWithShadow(mc.textRenderer, String.valueOf(left), x + 20, ry + 5, textColor());
            }
        }
    }
}
