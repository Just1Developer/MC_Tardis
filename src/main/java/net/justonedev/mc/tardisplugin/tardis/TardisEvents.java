package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TardisEvents implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (e.getType() != EntityType.ARMOR_STAND) return;
        // Check if tardis door:
        if (e.getCustomName() == null) return;
        if (!e.getCustomName().startsWith("tardis")) return;
        Tardis tardis = TardisPlugin.getTardisByEntityUUID(e.getUniqueId());
        if (tardis == null) return;

        tardis.enter(event.getPlayer());
    }

}
