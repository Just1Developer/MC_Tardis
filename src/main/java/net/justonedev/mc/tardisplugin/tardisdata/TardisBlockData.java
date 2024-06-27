package net.justonedev.mc.tardisplugin.tardisdata;

import net.justonedev.mc.tardisplugin.BlockUtils;
import net.justonedev.mc.tardisplugin.tardis.Tardis;
import net.justonedev.mc.tardisplugin.tardis.TardisFiles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TardisBlockData {

    private static final String BLOCKDATA_FILE = "%d.bdata";
    public static final int BLOCKDATA_VALUES = 4;
    private static final int USING_BYTES = 5;
    
    public static void initializeTardisAsync(Tardis tardis) {
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
    
    public static void saveBlockDataToFiles(Tardis tardis) {
        var executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), BLOCKDATA_VALUES));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < BLOCKDATA_VALUES; ++i) {
            final int data = i;
            futures.add(CompletableFuture.runAsync(() -> saveBlockdata(tardis, data), executor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static void applyBlockdata(Tardis tardis, File file, int value) {
        Location minLoc = tardis.getInteriorPlot().getMinLoc();
        assert file.exists();
        Set<Vector> vectorSet = new HashSet<>();
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
                        Vector vector = new Vector(x, y, z);
                        BlockUtils.setTardisBlockOwnership(minLoc.clone().add(x, y, z).getBlock(), value);
                        // Todo set in tardis object too
                        vectorSet.add(vector);

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
        tardis.setBlocksOwnedBy(value, vectorSet);
    }

    private static void saveBlockdata(Tardis tardis, int ownerID) {
        Set<Vector> blocks = tardis.getBlocksOwnedBy(ownerID);
        System.out.println("DEBUG >> Saving block set of size " + (blocks == null ? "null" : blocks.size()) + " with owner ID " + ownerID);
        if (blocks == null || blocks.isEmpty()) return;
        
        File file = TardisFiles.getFileForTardis(tardis, String.format(BLOCKDATA_FILE, ownerID));
        if (!file.getParentFile().exists()) if (!file.getParentFile().mkdirs()) {
            Bukkit.getLogger().warning("Could not create folder " + file.getParent());
            return;
        }
        
        try {
            if (!file.createNewFile()) {
                Bukkit.getLogger().warning("Could not create file " + file.getName());
                return;
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("An exception occurred while creating file " + file.getName() + ":");
            e.printStackTrace();
            return;
        }

        System.out.println("Yip yup + " + file.getName());
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            
            for (var loc : blocks) {
                // Write the vector
                outputStream.write((byte) ((loc.getBlockX() >> 6) & 0xFF));
                outputStream.write((byte) ((loc.getBlockX() & 0x3F << 2) | (loc.getBlockY() & 0x03)));
                outputStream.write((byte) (loc.getBlockY() & 0xFF));
                outputStream.write((byte) ((loc.getBlockZ() >> 8) & 0xFF));
                outputStream.write((byte) (loc.getBlockZ() & 0xFF));
            }
            outputStream.flush();
        } catch (IOException e) {
            Bukkit.getLogger().warning("IOException occurred when reading file " + file.getName());
        }
    }

}
