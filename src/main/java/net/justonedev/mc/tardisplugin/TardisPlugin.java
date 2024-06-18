package net.justonedev.mc.tardisplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public final class TardisPlugin extends JavaPlugin implements Listener {
    
    public static TardisPlugin singleton;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        singleton = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        getCommand("spawnmodel").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    private static void spawnModel(Location loc, TardisModelType modelType) {
        assert loc.getWorld() != null;
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setHeadPose(new EulerAngle(0, 0, 0));
        armorStand.setCustomName("tardis-model-" + armorStand.getUniqueId());
        armorStand.setCustomNameVisible(false);
        armorStand.setGlowing(modelType.shouldGlow);
        armorStand.setSmall(modelType.isBaby);
        armorStand.setCollidable(true);
        
        // Set a random wool color on the armor stand's head
        ItemStack woolHead = new ItemStack(modelType.baseMaterial);
        ItemMeta woolHeadMeta = woolHead.getItemMeta();
		assert woolHeadMeta != null;
		woolHeadMeta.setCustomModelData(modelType.customModelData);
        woolHead.setItemMeta(woolHeadMeta);
        
        assert armorStand.getEquipment() != null;
        armorStand.getEquipment().setHelmet(woolHead);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        spawnModel(p.getLocation().clone().add(1, 0, 0), TardisModelType.TARDIS_OUTER_STATIC);
        return true;
    }
}


/* Old way of many armor stands:

    
    private static final double blockOffset = 0.625;// worked for a bit, but is bugged rn: 0.4378;
    private static final double initialOffsetXZ = 2.0/30.0; // => 0.067
    private static final double initialOffsetY = -1.50;
    private static final int tardisHeight = 4;
    
    public void spawnWoolHeadArmorStands(Location loc, boolean sneaking) {
        spawnWoolHeadArmorStands(loc, sneaking, initialOffsetY);
    }
    public void spawnWoolHeadArmorStands(Location _loc, boolean sneaking, double initialOffsetY) {
        Bukkit.broadcastMessage("initialOffsetY = " + initialOffsetY);
        Location loc = new Location(_loc.getWorld(), _loc.getBlockX(), _loc.getBlockY(), _loc.getBlockZ());
        loc.getBlock().setType(Material.AIR);
        
        Material shellMaterial = Material.BLUE_WOOL;
        Material baseMaterial = Material.OAK_SLAB;
        Material lampMaterial = Material.SEA_LANTERN;
        Material lampTopperMaterial = Material.BRICK_SLAB;
        
        // Basic outer shell
        Location currentLoc = loc.clone().add(initialOffsetXZ, initialOffsetY, initialOffsetXZ);
        final int blockXZspan = 3;//, blockYspan = 2;
        for (int y = 0; y < tardisHeight; ++y) {
            currentLoc.setX(loc.getX() + initialOffsetXZ);
            for (int x = 0; x < blockXZspan; ++x) {
                currentLoc.setZ(loc.getZ() + initialOffsetXZ);
                for (int z = 0; z < blockXZspan; ++z) {
                    spawnArmorStand(currentLoc, shellMaterial, false, sneaking);
                    //Bukkit.broadcastMessage(String.format("[x: %d, y: %d, z: %d] Spawning armorstand at %s", x, y, z, currentLoc));
                    currentLoc.add(0, 0, blockOffset);
                }
                currentLoc.add(blockOffset, 0, 0);
            }
            currentLoc.add(0, blockOffset, 0);
        }
        
        
        // The base plate
        currentLoc.setY(loc.getBlockY() + initialOffsetY - 0.005);  // Very minor change to counter face glitching
        currentLoc.setX(loc.getX() + initialOffsetXZ - blockOffset/2);
        for (int x = 0; x < blockXZspan + 1; ++x) {
            currentLoc.setZ(loc.getZ() + initialOffsetXZ - blockOffset/2);
            for (int z = 0; z < blockXZspan + 1; ++z) {
                spawnArmorStand(currentLoc, baseMaterial, false, sneaking);
                //Bukkit.broadcastMessage(String.format("[x: %d, y: %d, z: %d] Spawning armorstand at %s", x, y, z, currentLoc));
                currentLoc.add(0, 0, blockOffset);
            }
            currentLoc.add(blockOffset, 0, 0);
        }
        
        
        // The lamp
        currentLoc.setY(loc.getBlockY() + initialOffsetY + (tardisHeight*1.27) * blockOffset);
        currentLoc.setX(loc.getX() + initialOffsetXZ + blockOffset);
        currentLoc.setZ(loc.getZ() + initialOffsetXZ + blockOffset);
        spawnArmorStand(currentLoc, lampMaterial, true, sneaking);
        currentLoc.setY(loc.getBlockY() + initialOffsetY + (tardisHeight + 0.6) * blockOffset);
        spawnArmorStand(currentLoc, lampTopperMaterial, false, sneaking);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {
            spawnWoolHeadArmorStands(p.getLocation().clone().add(2, 0, 0), p.isSneaking());
        } else {
            try {
                double d = Double.parseDouble(args[0]);
                spawnWoolHeadArmorStands(p.getLocation().clone().add(2, 0, 0), p.isSneaking());
            } catch (NumberFormatException e) {
                spawnWoolHeadArmorStands(p.getLocation().clone().add(2, 0, 0), p.isSneaking());
            }
        }
        return true;
    }

*
*
*
*
* */