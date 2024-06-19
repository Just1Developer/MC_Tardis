package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.CONSOLE_CENTER_HEIGHT;
import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.PLOT_CENTER;

public class Tardis {

    private final int numericID;
    private final UUID owner;
    private final UUID tardisUUID;
    private final int outerShellDesignIndex;
    private final TardisInteriorPlot interiorPlot;

    Tardis(UUID owner, UUID tardisUUID, int outerShellDesignIndex, int interiorPlotID) {
        this.numericID = interiorPlotID;
        this.owner = owner;
        this.tardisUUID = tardisUUID;
        this.outerShellDesignIndex = outerShellDesignIndex;
        // Todo do we need this?
        Optional<TardisInteriorPlot> plot = TardisWorldGen.calculateInteriorPlotByID(interiorPlotID);
        this.interiorPlot = plot.orElseGet(() -> new TardisInteriorPlot((int) (-3 * 1e7), (int) (-3 * 1e7)));

        TardisPlugin.singleton.tardises.put(interiorPlotID, this);
    }

    public static Tardis createNewTardis(UUID owner) {
        UUID uuid = UUID.randomUUID();
        Tardis tardis = new Tardis(owner, uuid, 1, TardisPlugin.singleton.tardises.size());
        tardis.build();
        return tardis;
    }

    /**
     * Builds the console center room and everything. Typically done when creating a new tardis.
     */
    public void build() {

    }

    public Location getAbsoluteConsoleLocation() {
        return new Location(TardisWorldGen.getInteriorWorld(),
                interiorPlot.getBeginX() + PLOT_CENTER,
                CONSOLE_CENTER_HEIGHT,
                interiorPlot.getBeginZ() + PLOT_CENTER);
    }

    public int getNumericID() {
        return numericID;
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
