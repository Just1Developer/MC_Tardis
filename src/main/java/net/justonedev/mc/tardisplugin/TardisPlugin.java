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
