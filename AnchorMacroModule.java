package dk.client67.modules;

import dk.client67.settings.BoolSetting;
import dk.client67.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Tryk på tasten (standard Z): sørger for at du har en totem i offhand, placerer et respawn anchor
 * der hvor du kigger, og fylder det med glowstone. Selve sprængningen gør du selv.
 * Kræver et respawn anchor og glowstone i hotbaren.
 */
public class AnchorMacroModule extends Module {
    private enum State { IDLE, PLACE, CHARGE }

    private final BoolSetting requireTotem = add(new BoolSetting("Require totem", true));
    private final BoolSetting autoEquip = add(new BoolSetting("Auto-equip totem", true));
    private final BoolSetting restoreSlot = add(new BoolSetting("Restore slot", true));
    private final NumberSetting charges = add(new NumberSetting("Glowstone charges", 1, 1, 4, 1));

    private State state = State.IDLE;
    private int timer, chargesDone, prevSlot, anchorSlot, glowSlot;
    private BlockPos anchorPos;
    private BlockHitResult placeHit;

    public AnchorMacroModule() {
        super("Anchor Macro", "Tryk Z: placerer et respawn anchor og fylder det med glowstone (kræver totem).",
                Category.UTILITY, false);
    }

    @Override
    public void onDisable() { state = State.IDLE; }

    private void msg(String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("[67client] " + s), true);
    }

    private int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    /** Flytter en totem fra inventory/hotbar ind i offhand. */
    private boolean equipTotem() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                int slot = i < 9 ? 36 + i : i;
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40,
                        SlotActionType.SWAP, mc.player);
                return true;
            }
        }
        return false;
    }

    /** Kaldes når tasten trykkes. */
    public void trigger() {
        if (!enabled || state != State.IDLE) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            msg("Kig på en blok");
            return;
        }
        anchorSlot = findHotbar(Items.RESPAWN_ANCHOR);
        glowSlot = findHotbar(Items.GLOWSTONE);
        if (anchorSlot < 0) { msg("Intet respawn anchor i hotbar"); return; }
        if (glowSlot < 0) { msg("Ingen glowstone i hotbar"); return; }

        if (requireTotem.get() && !mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            if (!(autoEquip.get() && equipTotem())) {
                msg("Du har ingen totem");
                return;
            }
        }

        placeHit = hit;
        prevSlot = mc.player.getInventory().getSelectedSlot();
        timer = 0;
        state = State.PLACE;
    }

    private void finish() {
        if (restoreSlot.get() && mc.player != null) mc.player.getInventory().setSelectedSlot(prevSlot);
        state = State.IDLE;
    }

    @Override
    public void onTick() {
        if (state == State.IDLE) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            state = State.IDLE;
            return;
        }

        if (state == State.PLACE) {
            mc.player.getInventory().setSelectedSlot(anchorSlot);
            BlockPos target = placeHit.getBlockPos();
            BlockPos pos = mc.world.getBlockState(target).isReplaceable() ? target : target.offset(placeHit.getSide());

            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, placeHit);
            if (!result.isAccepted()) {
                msg("Kunne ikke placere anchor");
                finish();
                return;
            }
            anchorPos = pos;
            chargesDone = 0;
            timer = 0;
            state = State.CHARGE;
            return;
        }

        // CHARGE: én glowstone pr. tick
        BlockState bs = mc.world.getBlockState(anchorPos);
        if (!bs.isOf(Blocks.RESPAWN_ANCHOR)) {
            if (++timer > 6) {
                msg("Anchoret blev ikke placeret");
                finish();
            }
            return;
        }
        // Sikkerhed: hvis anchoret allerede er fyldt op, må vi ikke bruge mere glowstone på det
        int current = bs.get(RespawnAnchorBlock.CHARGES);
        if (chargesDone >= charges.getInt() || current >= 4) {
            finish();
            return;
        }
        mc.player.getInventory().setSelectedSlot(glowSlot);
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchorPos), Direction.UP, anchorPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        chargesDone++;
    }
}
