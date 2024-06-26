package net.justonedev.mc.tardisplugin.tardisdata;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import net.justonedev.mc.tardisplugin.tardis.Tardis;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TardisBlockData {

    private static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    public static final List<CompletableFuture<Void>> futures = new ArrayList<>();

    public static void initializeTardisAsync(Tardis tardis, File folder) {
        /*CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // Asynchronous task running in another thread
            try {
                Thread.sleep(1000);  // Simulate long-running task
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, scheduler.runTaskAsynchronously(TardisPlugin.singleton, () -> {}).getTaskId());

        // Add future to the list
        futures.add(future);
        */
    }

}
