package dk.t0rbem.modules;

import java.util.List;

public class MemoryModule extends TextHudModule {
    public MemoryModule() { super("Memory", "Viser brugt RAM.", false, 4, 158); }

    @Override
    protected List<String> lines() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        return List.of("Mem: " + used + "/" + max + " MB");
    }
}
