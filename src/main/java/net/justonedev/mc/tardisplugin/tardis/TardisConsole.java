package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class TardisConsole {

    private static final Location RelativeLocation = new Location(
            TardisWorldGen.getInteriorWorld(),
            TardisWorldGen.PLOT_CENTER,
            TardisWorldGen.CONSOLE_CENTER_HEIGHT,
            TardisWorldGen.PLOT_CENTER);

    private final Tardis refTardis;
    private final Location absoluteLocation;

    TardisConsole(Tardis refTardis) {
        this.refTardis = refTardis;
        refTardis.setConsole(this);
        absoluteLocation = refTardis.getAbsoluteConsoleLocation();
    }

    void build() {
        TardisPlugin.spawnModel(absoluteLocation, TardisModelType.TARDIS_CONSOLE);
        absoluteLocation.clone().add(0, -2, 0).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(1, -2, 0).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(0, -2, 1).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(-1, -2, 0).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(0, -2, -1).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(1, -2, 1).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(-1, -2, 1).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(-1, -2, -1).getBlock().setType(Material.ACACIA_WOOD);
        absoluteLocation.clone().add(1, -2, -1).getBlock().setType(Material.ACACIA_WOOD);
        Bukkit.broadcastMessage("§e[TardisConsole:26] spawned model @ " + absoluteLocation);
        Bukkit.broadcastMessage("§9 " + absoluteLocation.getBlockX() + "  -  " + absoluteLocation.getBlockY() + "  -  " + absoluteLocation.getBlockZ());
    }

}
