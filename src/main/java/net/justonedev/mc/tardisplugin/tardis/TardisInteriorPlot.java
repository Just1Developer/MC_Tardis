package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Location;
import org.bukkit.World;

public class TardisInteriorPlot {

    private static final int size = TardisWorldGen.PLOT_SIZE;
    private final int beginX, beginZ;

    /**
     * Creates a new interior plot.
     * @param beginX The lower x bound.
     * @param beginZ The lower z bound.
     */
    public TardisInteriorPlot(int beginX, int beginZ) {
        this.beginX = beginX;
        this.beginZ = beginZ;
    }

    /**
     * If a given location is inside this tardis plot. Must be in the tardis world
     * configured by {@link TardisWorldGen} to apply correctly.
     * @param loc The location to compare.
     * @return True if the loc is inside the plot, false if not.
     */
    public boolean isInside(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().equals(TardisWorldGen.getInteriorWorld())) return false;
        return loc.getX() >= beginX && loc.getZ() >= beginZ
                && loc.getX() <= beginX + size && loc.getZ() <= beginZ + size;
    }

    /**
     * Gets the X coordinate of the beginning (lower edge) of the plot.<br>
     * Spans from here PLOT_SIZE to positive X.
     * @return The begin x coordinate.
     */
    public int getBeginX() {
        return beginX;
    }

    /**
     * Gets the Z coordinate of the beginning (lower edge) of the plot.<br>
     * Spans from here PLOT_SIZE to positive Z.
     * @return The begin z coordinate.
     */
    public int getBeginZ() {
        return beginZ;
    }

    /**
     * Gets the origin as Location object @ (X, 0, Z)
     * @return The plot origin point.
     */
    public Location getOrigin() {
        return new Location(TardisWorldGen.getInteriorWorld(), beginX, 0, beginZ);
    }

    /**
     * Gets the minimum location as Location object @ (X, world.minHeight, Z)
     * @return The plot origin point.
     */
    public Location getMinLoc() {
        World w = TardisWorldGen.getInteriorWorld();
        assert w != null;
        return new Location(w, beginX, w.getMinHeight(), beginZ);
    }

}
