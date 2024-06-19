package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.UUID;

public class TardisEvents implements Listener {
    
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Bukkit.broadcastMessage("Click event triggered");
        Entity entity = e.getRightClicked();
        Bukkit.broadcastMessage("Interact 1");
        if (!isTardisComponent(entity)) return;
        Bukkit.broadcastMessage("Interact 2");
        
        Tardis tardis = TardisPlugin.getTardisByEntityUUID(entity.getUniqueId());
        Bukkit.broadcastMessage("§cTardis: " + tardis);
        Bukkit.broadcastMessage("§cContains: " + TardisPlugin.singleton.tardisesByEntityUUID.containsKey(entity.getUniqueId()));
        Bukkit.broadcastMessage("§cSize: " + TardisPlugin.singleton.tardisesByEntityUUID.size());
        for (UUID uuid : TardisPlugin.singleton.tardisesByEntityUUID.keySet()) {
            Bukkit.broadcastMessage("§9UUID: " + uuid);
        }
        if (tardis == null) return;
        e.setCancelled(true);   // Event would probably be change equipment
        Bukkit.broadcastMessage("Interact 3");

        tardis.enter(e.getPlayer());
        Bukkit.broadcastMessage("Interact 4 (entered)");
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Bukkit.broadcastMessage("Damage event triggered");
        Entity entity = e.getEntity();
        if (!isTardisComponent(entity)) return;
        e.setCancelled(true);
        Bukkit.broadcastMessage("Tardis is immortal");
    }
    
    private static boolean isTardisComponent(Entity entity) {
        Bukkit.broadcastMessage("§eCheck: Type: " + entity.getType());
        if (entity.getType() != EntityType.ARMOR_STAND) return false;
        // Check if tardis door:
        Bukkit.broadcastMessage("§eCheck: hasCustomName: " + (entity.getCustomName() != null));
        if (entity.getCustomName() == null) return false;
        Bukkit.broadcastMessage("§eCheck: customName: " + entity.getCustomName() + " - condition: " + entity.getCustomName().startsWith("tardis"));
        return entity.getCustomName().startsWith("tardis");
    }

}
