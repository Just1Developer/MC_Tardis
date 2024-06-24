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
    private int chargeScheduler;

    TardisCharger(Tardis tardisRef) {
        this.tardisRef = tardisRef;
    }

    void startCheckingForCharge() {
        chargeScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TardisPlugin.singleton, () -> {
            Bukkit.broadcastMessage("I did a thing");
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

            Beacon beacon = (Beacon) block.getState();
            Bukkit.broadcastMessage("-------- ");
            Bukkit.broadcastMessage("§eInfo:: isBlockPowered: " + block.isBlockPowered());
            Bukkit.broadcastMessage("§eInfo:: isBlockIndirectlyPowered: " + block.isBlockIndirectlyPowered());
            Bukkit.broadcastMessage("§bInfo:: (Beacon).getLock: " + beacon.getLock());
            Bukkit.broadcastMessage("§bInfo:: (Beacon).getCustomName: " + beacon.getCustomName());
            Bukkit.broadcastMessage("§aInfo:: (Beacon).getCustomName: " + beacon.getPrimaryEffect());
            Bukkit.broadcastMessage("§aInfo:: (Beacon).getCustomName: " + beacon.getSecondaryEffect());
            Bukkit.broadcastMessage("§dInfo:: (Beacon).getCustomName: " + beacon.getTier());

        }, 20, 20 * IS_CHARGING_CONTROL_TIMER_SECONDS);
    }

    void stopCheckingForCharge() {
        if (Bukkit.getScheduler().isCurrentlyRunning(chargeScheduler)) {
            Bukkit.getScheduler().cancelTask(chargeScheduler);
        }
    }

}
