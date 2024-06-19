package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Optional;

public class TardisEvents implements Listener {
    
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!isTardisComponent(entity)) return;
        e.setCancelled(true);   // Event would probably be change equipment
        String name = entity.getCustomName();
        assert name != null;
        
        Bukkit.broadcastMessage("§eName: " + entity.getCustomName());
        if (name.contains(TardisModelType.TARDIS_OUTER_STATIC.modelName)) {
            Bukkit.broadcastMessage("§cTardis: Entering Door");
            Tardis tardis = TardisPlugin.getTardisByEntityUUID(entity.getUniqueId());
            if (tardis == null) return;
            
            // This way, we can later do entry key validation and more
            tardis.enter(e.getPlayer());
        } else if (name.contains(TardisModelType.TARDIS_INNER_DOOR.modelName)) {
            Bukkit.broadcastMessage("§cTardis: Exiting Door");
            Tardis tardis = TardisPlugin.getTardisByAnyPlotLocation(entity.getLocation());
            
            Bukkit.broadcastMessage("§eID: " + TardisWorldGen.calculateTardisIDbyLoc(entity.getLocation().getBlockX(), entity.getLocation().getBlockZ()));
            Bukkit.broadcastMessage("§eTardis: " + tardis);
            for (int key : TardisPlugin.singleton.tardises.keySet()) {
                Bukkit.broadcastMessage("§bKey: " + key);
            }
            
            if (tardis == null) return;
            Optional<ArmorStand> model = tardis.getCurrentModelTardis();
            Bukkit.broadcastMessage("§cui");
            
            if (model.isEmpty()) return;
            Bukkit.broadcastMessage("§cui2");
            // Todo perhaps later modify location when we have set way to determine and set facing of the tardis
            e.getPlayer().teleport(model.get().getLocation());
        }
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
    
    @EventHandler
    public void onBeaconIgnite(Event)

}
