package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class TardisProtection implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!TardisWorldGen.isInteriorWorld(e.getLocation().getWorld())) return;
        handleExplosion(e.blockList());
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        if (!TardisWorldGen.isInteriorWorld(e.getBlock().getWorld())) return;
        handleExplosion(e.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
        for (int i = 0; i < blocks.size(); ++i) {
            int ownership = BlockUtils.getTardisBlockOwnership(blocks.get(i));
            if (ownership == -1 || ownership > 2) continue;
            blocks.remove(i);
            --i;
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (TardisEvents.isTardisComponent(e.getEntity())) e.setCancelled(true);
    }

}
