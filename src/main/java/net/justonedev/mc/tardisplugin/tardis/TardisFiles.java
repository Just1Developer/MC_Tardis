package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import net.justonedev.mc.tardisplugin.tardisdata.TardisBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TardisFiles {
    private static String TARDIS_FOLDER = "%s/tardises/";
    private static String SINGLE_TARDIS_FOLDER_FORMAT = TARDIS_FOLDER + "t%d-%s/%s";
    private static String MAIN_DATAHOLDER_FILE = "tardis.yml";

    public static void initialize() {
        Bukkit.getLogger().info("Initializing File System");
        TARDIS_FOLDER = String.format(TARDIS_FOLDER, TardisPlugin.singleton.getDataFolder());
        loadTardises();
        Bukkit.getLogger().info("File System initialized successfully");
    }

    public static void saveAll() {
        System.out.println("Saving All TardisFiles");
        for (Tardis tardis : TardisPlugin.singleton.tardises.values()) {
            System.out.println("Saving file " + tardis.getNumericID() + " + " + tardis.getFullUUIDString());
            saveToFile(tardis);
        }
        System.out.println("Saved All TardisFiles");
    }

    public static void saveToFile(Tardis tardis) {
        File file = getFileForTardis(tardis, MAIN_DATAHOLDER_FILE);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("owner", tardis.getOwnerUUIDString());
        cfg.set("uuid", tardis.getTardisUUIDString());
        cfg.set("exterior-shell.design", tardis.getOuterShellDesignIndex());
        saveBlockLocation(cfg, tardis.getTardisOuterShellLocation(), "exterior-shell.location");
        final var modelUUID = tardis.getTardisOuterShellUUID();
        cfg.set("presence", modelUUID.isEmpty() ? "" : modelUUID.get().toString());

        try {
            cfg.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error saving file " + file.getName() + ": " + e.getMessage());
        }
    }

    public static void loadTardises() {
        File folderFile = new File(TARDIS_FOLDER);
        if (!folderFile.exists()) if (!folderFile.mkdirs()) {
            Bukkit.getLogger().severe("Failed to create folder " + TARDIS_FOLDER + ". Aborting tardis load.");
            return;
        }

        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (File folder : Objects.requireNonNull(folderFile.listFiles())) {
            if (!folder.isDirectory()) continue;
            if (!folder.getName().startsWith("t")) continue;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> loadTardis(folder), executor);
            futures.add(future);
        }

        // Wait for all blockdata futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static void loadTardis(File folder) {
        File mainfile = new File(folder, MAIN_DATAHOLDER_FILE);
        if (!mainfile.exists()) {
            Bukkit.getLogger().warning("Could not load main tardis file in folder " + folder.getName());
            return;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(mainfile);
        String tempString = cfg.getString("owner");
        if (tempString == null) tempString = "";
        UUID ownerUUID = UUID.fromString(tempString);

        tempString = cfg.getString("uuid");
        if (tempString == null) tempString = "";
        UUID tardisUUID = UUID.fromString(tempString);

        int outerShellIndex = cfg.getInt("exterior-shell.design");
        int plotID = cfg.getInt("plotID");

        // Insert in map is done in constructor
        Tardis tardis = new Tardis(ownerUUID, tardisUUID, outerShellIndex, plotID);
        String modelUUID = cfg.getString("presence");
        Location loc = loadBlockLocation(cfg, "exterior-shell.location");
        tardis.setTardisOuterShellLocation(loc);
        if (modelUUID != null && !modelUUID.isBlank()) {
            tardis.bindCurrentModelTardis(UUID.fromString(modelUUID), true);
        }
        TardisBlockData.initializeTardisAsync(tardis, folder);
    }

    private static Location loadBlockLocation(YamlConfiguration cfg, String locName) {
        if (!cfg.isSet(locName + ".world")) {
            //Bukkit.getLogger().warning("Failed to load location " + locName + " because " + locName + ".world is not present");
            return null;
        }

        String w_name = cfg.getString(locName + ".world");
        World world = Bukkit.getWorld(w_name != null ? w_name : "");
        if (world == null) {
            Bukkit.getLogger().warning("Failed to load location " + locName + " because the world " + w_name + " is not present.");
            return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        }

        return new Location(world, cfg.getInt(locName + ".x"), cfg.getInt(locName + ".y"), cfg.getInt(locName + ".z"));
    }

    private static Location loadCenterBlockLocation(YamlConfiguration cfg, Location loc, String locName) {
        return loadBlockLocation(cfg, locName).add(0.5, 0, 0.5);

    }

    private static void saveBlockLocation(YamlConfiguration cfg, Location loc, String locName) {
        if (loc == null) {
            return;
        }
        if (loc.getWorld() == null) {
            Bukkit.getLogger().warning("Failed to save location " + locName + " because world is null");
        }
        cfg.set(locName + ".world", loc.getWorld().getName());
        cfg.set(locName + ".x", loc.getBlockX());
        cfg.set(locName + ".y", loc.getBlockY());
        cfg.set(locName + ".z", loc.getBlockZ());

    }

    private static void saveFullLocation(YamlConfiguration cfg, Location loc, String locName) {
        if (loc.getWorld() == null) {
            Bukkit.getLogger().warning("Failed to save location " + locName + " because world is null");
        }
        cfg.set(locName + ".world", loc.getWorld().getName());
        cfg.set(locName + ".x", loc.getX());
        cfg.set(locName + ".y", loc.getY());
        cfg.set(locName + ".z", loc.getZ());
        cfg.set(locName + ".yaw", loc.getYaw());
        cfg.set(locName + ".pitch", loc.getPitch());
    }

    public static File getFileForTardis(Tardis tardis, String filename) {
        return new File(String.format(SINGLE_TARDIS_FOLDER_FORMAT, TardisPlugin.singleton.getDataFolder(), tardis.getNumericID(), tardis.getFullUUIDString(), filename));
    }
}
