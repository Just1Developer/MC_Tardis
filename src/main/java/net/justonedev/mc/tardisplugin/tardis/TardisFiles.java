package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class TardisFiles {
    private static String TARDIS_FOLDER = "%s/tardises/";

    public static void initialize() {
        Bukkit.getLogger().info("Initializing File System");
        TARDIS_FOLDER = String.format(TARDIS_FOLDER, TardisPlugin.singleton.getDataFolder());
        Bukkit.getLogger().info("File System initialized successfully");
    }

    public static void saveToFile(Tardis tardis) {
        String filename = TARDIS_FOLDER + tardis.getFullUUIDString() + ".yml";
        File file = new File(filename);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("owner", tardis.getOwnerUUIDString());
        cfg.set("uuid", tardis.getTardisUUIDString());
        cfg.set("outer-shell", tardis.getOuterShellDesignIndex());

        try {
            cfg.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error saving file " + filename + ": " + e.getMessage());
        }
    }

    public static void loadTardises() {

    }
}
