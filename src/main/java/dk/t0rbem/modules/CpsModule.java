package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class CpsModule extends TextHudModule {
    private final BoolSetting showRight = add(new BoolSetting("Show right click", true));
    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private boolean lastLeft, lastRight;

    public CpsModule() { super("CPS", "Viser klik per sekund.", true, 4, 52); }

    @Override
    public void onTick() {
        boolean l = mc.options.attackKey.isPressed();
        boolean r = mc.options.useKey.isPressed();
        long now = System.currentTimeMillis();
        if (l && !lastLeft) left.add(now);
        if (r && !lastRight) right.add(now);
        lastLeft = l;
        lastRight = r;
        prune(left, now);
        prune(right, now);
    }

    private static void prune(Deque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
    }

    @Override
    protected List<String> lines() {
        return List.of(showRight.get()
                ? "CPS: " + left.size() + " | " + right.size()
                : "CPS: " + left.size());
    }
}
