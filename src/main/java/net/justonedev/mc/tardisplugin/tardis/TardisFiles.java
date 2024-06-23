package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class TardisFiles {
    private static String TARDIS_FOLDER = "%s/tardises/";

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
        String filename = TARDIS_FOLDER + String.format("t%d-%s.yml", tardis.getNumericID(), tardis.getFullUUIDString());
        File file = new File(filename);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("owner", tardis.getOwnerUUIDString());
        cfg.set("uuid", tardis.getTardisUUIDString());
        cfg.set("outer-shell", tardis.getOuterShellDesignIndex());
        final UUID modelUUID = tardis.getCurrentModelTardisUUID();
        cfg.set("presence", modelUUID == null ? "" : modelUUID.toString());

        try {
            cfg.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error saving file " + filename + ": " + e.getMessage());
        }
    }

    public static void loadTardises() {
        File folderFile = new File(TARDIS_FOLDER);
        if (!folderFile.exists()) if (!folderFile.mkdirs()) {
            Bukkit.getLogger().severe("Failed to create folder " + TARDIS_FOLDER + ". Aborting tardis load.");
            return;
        }
        for (File f : Objects.requireNonNull(folderFile.listFiles())) {
            if (!f.getName().endsWith(".yml") || !f.getName().startsWith("t")) continue;
            loadTardis(f);
        }
    }

    private static void loadTardis(File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String tempString = cfg.getString("owner");
        if (tempString == null) tempString = "";
        UUID ownerUUID = UUID.fromString(tempString);

        tempString = cfg.getString("uuid");
        if (tempString == null) tempString = "";
        UUID tardisUUID = UUID.fromString(tempString);

        int outerShellIndex = cfg.getInt("outer-shell");
        int plotID = cfg.getInt("plotID");

        // Insert in map is done in constructor
        Tardis tardis = new Tardis(ownerUUID, tardisUUID, outerShellIndex, plotID);
        String modelUUID = cfg.getString("presence");
        if (modelUUID != null && !modelUUID.isBlank()) {
            tardis.bindCurrentModelTardis(UUID.fromString(modelUUID), true);
        }
    }
}
