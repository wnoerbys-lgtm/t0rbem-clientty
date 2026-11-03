package dk.client67;

/** Farver og tema til GUI'en. */
public class Theme {
    public static final String[] NAMES = {"Emerald", "Ocean", "Sunset", "Violet", "Rainbow"};
    private static final int[][] COLORS = {
            {0xFF2BD99B, 0xFF1D8F6A},
            {0xFF3AA0FF, 0xFF2060D0},
            {0xFFFF8A3D, 0xFFE0407A},
            {0xFFA06BFF, 0xFF6B3FD6}
    };

    public static int index = 0;
    public static boolean particles = true;

    public static boolean rainbow() { return index == NAMES.length - 1; }

    public static void cycle() { index = (index + 1) % NAMES.length; }

    public static int accent() { return at(0f); }

    /** Farve langs en gradient (pos 0..1). Regnbue-temaet animerer. */
    public static int at(float pos) {
        if (rainbow()) {
            float h = (((System.currentTimeMillis() / 25) % 360) / 360f + pos * 0.4f) % 1f;
            return hsv(h, 0.55f, 1f);
        }
        int[] c = COLORS[Math.max(0, Math.min(index, COLORS.length - 1))];
        return mix(c[0], c[1], pos);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    public static int hsv(float h, float s, float v) {
        float r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
