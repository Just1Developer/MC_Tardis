package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SchematicStructure {

    Location minimalLocation;
    Vector dimensions;

    SchematicStructure(Location from, Location to) {
        dimensions = new Vector(to.getBlockX() - from.getBlockX(), to.getBlockY() - from.getBlockY(), to.getBlockZ() - from.getBlockZ());
        minimalLocation = new Location(
                from.getWorld(),
                Math.min(from.getBlockX(), to.getBlockX()),
                Math.min(from.getBlockY(), to.getBlockY()),
                Math.min(from.getBlockZ(), to.getBlockZ()));
    }

}
