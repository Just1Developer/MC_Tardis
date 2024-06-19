package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Location;

import java.util.UUID;

import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.CONSOLE_CENTER_HEIGHT;
import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.PLOT_CENTER;

public class Tardis {

    private final UUID owner;
    private final UUID tardisUUID;
    private final int outerShellDesignIndex;
    private final TardisInteriorPlot interiorPlot;

    private Tardis(UUID owner, UUID tardisUUID, int outerShellDesignIndex, TardisInteriorPlot interiorPlot) {
        this.owner = owner;
        this.tardisUUID = tardisUUID;
        this.outerShellDesignIndex = outerShellDesignIndex;
        this.interiorPlot = interiorPlot;
    }

    public Location getAbsoluteConsoleLocation() {
        return new Location(TardisWorldGen.getInteriorWorld(),
                interiorPlot.getBeginX() + PLOT_CENTER,
                CONSOLE_CENTER_HEIGHT,
                interiorPlot.getBeginZ() + PLOT_CENTER);
    }

    public String getOwnerUUIDString() {
        return owner.toString();
    }

    public String getTardisUUIDString() {
        return tardisUUID.toString();
    }

    public String getFullUUIDString() {
        return String.format("%s.%s", getOwnerUUIDString(), getTardisUUIDString());
    }

    public TardisInteriorPlot getInteriorPlot() {
        return interiorPlot;
    }

    public int getOuterShellDesignIndex() {
        return outerShellDesignIndex;
    }

}
