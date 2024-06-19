package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TardisEvents implements Listener {

    public TardisEvents() {
        System.out.println("Hello World from Tardis Event Class!");
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Bukkit.broadcastMessage("Interact 1");
        if (e.getType() != EntityType.ARMOR_STAND) return;
        Bukkit.broadcastMessage("Interact 2");
        // Check if tardis door:
        if (e.getCustomName() == null) return;
        Bukkit.broadcastMessage("Interact 3");
        if (!e.getCustomName().startsWith("tardis")) return;
        Bukkit.broadcastMessage("Interact 4");
        Tardis tardis = TardisPlugin.getTardisByEntityUUID(e.getUniqueId());
        if (tardis == null) return;
        Bukkit.broadcastMessage("Interact 5");

        tardis.enter(event.getPlayer());
        Bukkit.broadcastMessage("Interact 6 (entered)");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Bukkit.broadcastMessage("Interact 0");
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Entity e = event.getPlayer();   // Todo just testing here, this is not a bug
        Bukkit.broadcastMessage("Interact 1");
        if (e.getType() != EntityType.ARMOR_STAND) return;
        Bukkit.broadcastMessage("Interact 2");
        // Check if tardis door:
        if (e.getCustomName() == null) return;
        Bukkit.broadcastMessage("Interact 3");
        if (!e.getCustomName().startsWith("tardis")) return;
        Bukkit.broadcastMessage("Interact 4");
        Tardis tardis = TardisPlugin.getTardisByEntityUUID(e.getUniqueId());
        if (tardis == null) return;
        Bukkit.broadcastMessage("Interact 5");

        tardis.enter(event.getPlayer());
        Bukkit.broadcastMessage("Interact 6 (entered)");
    }

}
