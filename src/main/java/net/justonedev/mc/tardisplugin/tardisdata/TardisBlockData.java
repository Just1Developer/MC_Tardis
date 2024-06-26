package net.justonedev.mc.tardisplugin.tardisdata;

import net.justonedev.mc.tardisplugin.BlockUtils;
import net.justonedev.mc.tardisplugin.tardis.Tardis;
import net.justonedev.mc.tardisplugin.tardis.TardisFiles;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TardisBlockData {

    private static final String BLOCKDATA_FILE = "%d.bdata";
    private static final int BLOCKDATA_VALUES = 4;
    private static final int USING_BYTES = 5;
    public static void initializeTardisAsync(Tardis tardis, File folder) {
        var executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), BLOCKDATA_VALUES));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < BLOCKDATA_VALUES; ++i) {
            final int data = i;
            File file = TardisFiles.getFileForTardis(tardis, String.format(BLOCKDATA_FILE, data));
            if (!file.exists()) continue;   // probably no data
            futures.add(CompletableFuture.runAsync(() -> applyBlockdata(tardis, file, data), executor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static void applyBlockdata(Tardis tardis, File file, int value) {
        Location minLoc = tardis.getInteriorPlot().getOrigin();
        assert file.exists();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte bytenum = 0;
            int x = 0, y = 0, z = 0;
            for (byte b : inputStream.readAllBytes()) {
                // 0th byte
                bytenum++;
                switch (bytenum) {
                    case 1:
                        // X uses 13 bit
                        x |= (b) << 6;
                        break;
                    case 2:
                        // Y uses 10 bit, 2 of the 2nd x byte and one own byte
                        x |= (b >> 2);
                        y |= (b & 0b11) << 8;
                        break;
                    case 3:
                        y |= b;
                        break;
                    case 4:
                        z |= b << 8;
                        break;
                    case 5:
                        z |= b;
                        break;
                    default:
                        BlockUtils.setTardisBlockOwnership(minLoc.clone().add(x, y, z).getBlock(), value);
                        // Todo set in tardis object too

                        bytenum = 0;
                        x = 0;
                        y = 0;
                        z = 0;
                        break;
                }
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("IOException occurred when reading file " + file.getName());
        }
    }

}
