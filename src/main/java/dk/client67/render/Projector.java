package dk.client67.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Regner verdenskoordinater om til skærmkoordinater (GUI-skala) og tegner 3D-wireframe
 * og 2D-bokse oven på verdenen uden mixins. Kald update() én gang pr. frame.
 * Bruger kameraets entitet, så det også virker i freecam.
 */
public class Projector {
    private static final double NEAR = 0.11;

    public double ex, ey, ez;
    public int sw, sh;
    private double fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;

    public void update(DrawContext ctx, float pt) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity cam = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        if (cam == null) return;

        double eyeOffset = cam.getEyeY() - cam.getY();
        ex = MathHelper.lerp(pt, cam.lastRenderX, cam.getX());
        ey = MathHelper.lerp(pt, cam.lastRenderY, cam.getY()) + eyeOffset;
        ez = MathHelper.lerp(pt, cam.lastRenderZ, cam.getZ());

        double yaw = Math.toRadians(cam.getYaw(pt));
        double pitch = Math.toRadians(cam.getPitch(pt));
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0;
        rz = -Math.sin(yaw);
        ux = -rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy;

        tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        aspect = (double) mc.getWindow().getFramebufferWidth() / Math.max(1, mc.getWindow().getFramebufferHeight());
        sw = ctx.getScaledWindowWidth();
        sh = ctx.getScaledWindowHeight();
    }

    private double depth(double x, double y, double z) {
        return (x - ex) * fx + (y - ey) * fy + (z - ez) * fz;
    }

    /** Punkt -> {x, y} på skærmen, eller null hvis det er bag kameraet. */
    public double[] project(double x, double y, double z) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth < 0.1) return null;
        double xn = (dx * rx + dy * ry + dz * rz) / depth / (tanHalf * aspect);
        double yn = (dx * ux + dy * uy + dz * uz) / depth / tanHalf;
        return new double[]{(xn + 1.0) / 2.0 * sw, (1.0 - yn) / 2.0 * sh};
    }

    // ---------- 3D-linjer ----------

    /** Linje mellem to verdenspunkter. Klippes mod kameraet og skærmkanten. */
    public void lineWorld(DrawContext ctx, double ax, double ay, double az, double bx, double by, double bz, int color) {
        double da = depth(ax, ay, az), db = depth(bx, by, bz);
        if (da < NEAR && db < NEAR) return;
        if (da < NEAR) {
            double t = (NEAR - da) / (db - da);
            ax += (bx - ax) * t; ay += (by - ay) * t; az += (bz - az) * t;
        } else if (db < NEAR) {
            double t = (NEAR - db) / (da - db);
            bx += (ax - bx) * t; by += (ay - by) * t; bz += (az - bz) * t;
        }
        double[] pa = project(ax, ay, az), pb = project(bx, by, bz);
        if (pa == null || pb == null) return;
        line2d(ctx, pa[0], pa[1], pb[0], pb[1], color);
    }

    /** Wireframe-boks (12 kanter) i verden. */
    public void wireBox(DrawContext ctx, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        for (int i = 0; i < 8; i++) {
            double cx = (i & 1) == 0 ? x1 : x2, cy = (i & 2) == 0 ? y1 : y2, cz = (i & 4) == 0 ? z1 : z2;
            if ((i & 1) == 0) lineWorld(ctx, cx, cy, cz, x2, cy, cz, color);
            if ((i & 2) == 0) lineWorld(ctx, cx, cy, cz, cx, y2, cz, color);
            if ((i & 4) == 0) lineWorld(ctx, cx, cy, cz, cx, cy, z2, color);
        }
    }

    private void line2d(DrawContext ctx, double x1, double y1, double x2, double y2, int color) {
        // Liang-Barsky klipning mod skærmen
        double dx = x2 - x1, dy = y2 - y1, t0 = 0, t1 = 1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x1, sw - x1, y1, sh - y1};
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return;
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) {
                    if (r > t1) return;
                    if (r > t0) t0 = r;
                } else {
                    if (r < t0) return;
                    if (r < t1) t1 = r;
                }
            }
        }
        int ax = (int) Math.round(x1 + t0 * dx), ay = (int) Math.round(y1 + t0 * dy);
        int bx = (int) Math.round(x1 + t1 * dx), by = (int) Math.round(y1 + t1 * dy);

        int adx = Math.abs(bx - ax), ady = Math.abs(by - ay);
        if (adx >= ady) {
            if (ax > bx) { int t = ax; ax = bx; bx = t; t = ay; ay = by; by = t; }
            if (ady == 0) { ctx.fill(ax, ay, bx + 1, ay + 1, color); return; }
            int sy = by > ay ? 1 : -1;
            for (int k = 0; k <= ady; k++) {
                int xs = (int) Math.max(ax, Math.round(ax + adx * (k - 0.5) / ady));
                int xe = (int) Math.min(bx, Math.round(ax + adx * (k + 0.5) / ady));
                int yy = ay + k * sy;
                ctx.fill(xs, yy, xe + 1, yy + 1, color);
            }
        } else {
            if (ay > by) { int t = ax; ax = bx; bx = t; t = ay; ay = by; by = t; }
            int sx = bx > ax ? 1 : -1;
            for (int k = 0; k <= adx; k++) {
                int ys = (int) Math.max(ay, Math.round(ay + ady * (k - 0.5) / Math.max(1, adx)));
                int ye = (int) Math.min(by, Math.round(ay + ady * (k + 0.5) / Math.max(1, adx)));
                int xx = ax + k * sx;
                ctx.fill(xx, ys, xx + 1, ye + 1, color);
            }
        }
    }

    // ---------- bokse ----------

    /** Skærmrektangel {minX, minY, maxX, maxY} om en boks, eller null hvis den ikke er synlig. */
    public int[] bounds(double x1, double y1, double z1, double x2, double y2, double z2) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double[] s = project((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2);
            if (s == null) return null;
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }
        if (maxX < 0 || maxY < 0 || minX > sw || minY > sh) return null;
        return new int[]{
                (int) Math.max(-50, minX), (int) Math.max(-50, minY),
                (int) Math.min(sw + 50, maxX), (int) Math.min(sh + 50, maxY)
        };
    }

    public static void box(DrawContext ctx, int[] b, int color, boolean fill) {
        int x1 = b[0], y1 = b[1], x2 = b[2], y2 = b[3];
        if (fill) ctx.fill(x1, y1, x2, y2, (color & 0x00FFFFFF) | 0x33000000);
        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);
    }

    /**
     * Tegner en boks som 3D-wireframe (wire=true) eller 2D-ramme.
     * Returnerer skærmrektanglet til labels, eller null hvis den ikke er synlig.
     */
    public int[] drawBox(DrawContext ctx, double x1, double y1, double z1, double x2, double y2, double z2,
                         int color, boolean wire, boolean fill) {
        if (!wire) {
            int[] b = bounds(x1, y1, z1, x2, y2, z2);
            if (b == null) return null;
            box(ctx, b, color, fill);
            return b;
        }

        wireBox(ctx, x1, y1, z1, x2, y2, z2, color);

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        boolean any = false;
        for (int i = 0; i < 8; i++) {
            double[] s = project((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, (i & 4) == 0 ? z1 : z2);
            if (s == null) continue;
            any = true;
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }
        if (!any || maxX < 0 || maxY < 0 || minX > sw || minY > sh) return null;
        return new int[]{(int) minX, (int) minY, (int) maxX, (int) maxY};
    }
}
