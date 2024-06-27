package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;

import java.util.Optional;

import static net.justonedev.mc.tardisplugin.tardis.TardisConstants.CHARGER_BLOCK_MATERIAL;

public class TardisCharger {
    
    private static final int IS_CHARGING_CONTROL_TIMER_SECONDS = 5;
    private static boolean running;
    private static int chargeScheduler;
    
    public static void startChargingScheduler() {
        if (running) return;
        running = true;
        chargeScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TardisPlugin.singleton, () -> {
            for (Tardis tardisRef : TardisPlugin.singleton.tardises.values()) {
                Optional<Location> beaconLoc = tardisRef.getOuterShellLocation();
                if (beaconLoc.isEmpty()) {
                    continue;
                }
                Block block = beaconLoc.get().getBlock();
                if (block.getType() != CHARGER_BLOCK_MATERIAL) {
                    continue;
                }
                
                //--------------------------------------------------//
                //                                                  //
                //  Observation: The getTier() changes from 0 to 1  //
                //  when activated.                                 //
                //                                                  //
                //--------------------------------------------------//
                
                Beacon beacon = (Beacon) block.getState();
                boolean active = beacon.getTier() > 0;
                boolean isTardisCharging = tardisRef.isCharging();
                if (active != isTardisCharging) {
                    tardisRef.setCharging(active);
                    if (active) {
                        Bukkit.broadcastMessage("§bStarted charging tardis " + tardisRef.getTardisUUIDString() + ".");
                    } else {
                        Bukkit.broadcastMessage("§cStopped charging tardis" + tardisRef.getTardisUUIDString() + ".");
                    }
                }
            }
            
        }, 20, 20 * IS_CHARGING_CONTROL_TIMER_SECONDS);
    }
    
    public static void stopChargingScheduler() {
        if (Bukkit.getScheduler().isCurrentlyRunning(chargeScheduler)) {
            Bukkit.getScheduler().cancelTask(chargeScheduler);
        }
    }
}
