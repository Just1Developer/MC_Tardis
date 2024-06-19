package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class TardisEvents implements Listener {
    
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!isTardisComponent(entity)) return;
        
        Tardis tardis = TardisPlugin.getTardisByEntityUUID(entity.getUniqueId());
        if (tardis == null) return;
        e.setCancelled(true);   // Event would probably be change equipment

        tardis.enter(e.getPlayer());
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if (!isTardisComponent(entity)) return;
        e.setCancelled(true);
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isTardisComponent(Entity entity) {
        if (entity.getType() != EntityType.ARMOR_STAND) return false;
        if (entity.getCustomName() == null) return false;
        return entity.getCustomName().startsWith("tardis");
    }

}
