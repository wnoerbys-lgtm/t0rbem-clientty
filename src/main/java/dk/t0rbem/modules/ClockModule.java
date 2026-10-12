package dk.t0rbem.modules;

import dk.t0rbem.settings.BoolSetting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockModule extends TextHudModule {
    private static final DateTimeFormatter H24 = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter H12 = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private final BoolSetting twentyFour = add(new BoolSetting("24-hour", true));

    public ClockModule() { super("Clock", "Viser det rigtige klokkeslæt.", false, 4, 142); }

    @Override
    protected List<String> lines() {
        return List.of(LocalTime.now().format(twentyFour.get() ? H24 : H12));
    }
}
