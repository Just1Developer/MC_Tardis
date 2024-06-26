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

    private final Tardis tardisRef;
    private boolean running;
    private int chargeScheduler;
    private boolean isCharging;

    TardisCharger(Tardis tardisRef) {
        this.tardisRef = tardisRef;
        running = false;
        this.isCharging = false;
    }

    void startCheckingForCharge() {
        if (running) return;
        running = true;
        chargeScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TardisPlugin.singleton, () -> {
            Optional<Location> beaconLoc = tardisRef.getOuterShellLocation();
            if (beaconLoc.isEmpty()) {
                stopCheckingForCharge();
                return;
            }
            Block block = beaconLoc.get().getBlock();
            if (block.getType() != CHARGER_BLOCK_MATERIAL) {
                stopCheckingForCharge();
                return;
            }
            
            //--------------------------------------------------//
            //                                                  //
            //  Observation: The getTier() changes from 0 to 1  //
            //  when activated.                                 //
            //                                                  //
            //--------------------------------------------------//
            
            // For some reason, this still executes twice. But, since it's a toggle and it does what it's supposed to, I don't mind.
            
            // Todo: Perhaps one scheduler for all charging? -> Good when many tardises
            
            Beacon beacon = (Beacon) block.getState();
            boolean active = beacon.getTier() > 0;
            if (active != isCharging) {
                isCharging = active;
                if (isCharging) {
                    Bukkit.broadcastMessage("§bStarted charging tardis." + System.nanoTime());
                } else {
                    Bukkit.broadcastMessage("§cStopped charging tardis.");
                }
            }

        }, 20, 20 * IS_CHARGING_CONTROL_TIMER_SECONDS);
    }

    void stopCheckingForCharge() {
        if (Bukkit.getScheduler().isCurrentlyRunning(chargeScheduler)) {
            Bukkit.getScheduler().cancelTask(chargeScheduler);
        }
    }

}
