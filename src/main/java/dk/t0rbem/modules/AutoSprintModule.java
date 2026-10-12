package dk.t0rbem.modules;

public class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("Auto Sprint", "Sprinter automatisk når du går fremad.", Category.UTILITY, false);
    }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p == null) return;
        if (mc.options.forwardKey.isPressed()
                && !p.isSneaking()
                && !p.horizontalCollision
                && p.getHungerManager().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}
