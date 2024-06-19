package net.justonedev.mc.tardisplugin;

import net.justonedev.mc.tardisplugin.tardis.Tardis;
import net.justonedev.mc.tardisplugin.tardis.TardisFiles;
import net.justonedev.mc.tardisplugin.tardis.TardisModelType;
import net.justonedev.mc.tardisplugin.tardis.TardisWorldGen;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class TardisPlugin extends JavaPlugin implements Listener {

    public static TardisPlugin singleton;

    /**
     * Tardises uses ascending ID's to save tardises, but can't ensure correct order when loading.
     * To ensure IDs stay the same regardless, we're using a TreeMap with O(log n) access instead
     * and hope it's fast enough.
     */
    public Map<Integer, Tardis> tardises;
    public Map<UUID, Tardis> tardisesByEntityUUID;

    @Override
    public void onEnable() {
        // Plugin startup logic
        singleton = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        getCommand("spawnmodel").setExecutor(this);
        getCommand("tptardisworld").setExecutor(this);
        getCommand("home").setExecutor(this);

        tardises = new HashMap<>();
        tardisesByEntityUUID = new HashMap<>();
        TardisWorldGen.initialize();
        TardisFiles.initialize();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        TardisFiles.saveAll();
    }
    
    public static ArmorStand spawnModel(Location _loc, TardisModelType modelType) {
        assert _loc.getWorld() != null;
        Location loc = new Location(_loc.getWorld(), _loc.getBlockX() + 0.5, _loc.getBlockY(), _loc.getBlockZ() + 0.5, 0, 0);
        assert loc.getWorld() != null;
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setHeadPose(new EulerAngle(0, 0, 0));
        armorStand.setCustomName(String.format("tardis-%s-%s", modelType.modelName, armorStand.getUniqueId()));
        armorStand.setCustomNameVisible(false);
        armorStand.setGlowing(modelType.shouldGlow);
        armorStand.setSmall(modelType.isBaby);
        armorStand.setCollidable(true);

        if (modelType.baseMaterial != Material.AIR) {
            ItemStack itemModel = new ItemStack(modelType.baseMaterial);
            ItemMeta woolHeadMeta = itemModel.getItemMeta();
            assert woolHeadMeta != null;
            woolHeadMeta.setCustomModelData(modelType.customModelData);
            itemModel.setItemMeta(woolHeadMeta);

            assert armorStand.getEquipment() != null;
            armorStand.getEquipment().setHelmet(itemModel);
        }

        return armorStand;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (command.getName().equals("spawnmodel")) {
            Tardis t = Tardis.createNewTardis(p.getUniqueId());
            t.spawnTardis(p.getLocation().clone().add(1, 0, 0));
        } else if (command.getName().equals("tptardisworld")) {
            p.teleport(TardisWorldGen.getInteriorWorld().getSpawnLocation());
        } else if (command.getName().equals("home")) {
            Location loc = p.getRespawnLocation();
            if (loc == null) loc = Bukkit.getWorlds().get(0).getSpawnLocation();
            p.teleport(loc);
        }
        return true;
    }
}
