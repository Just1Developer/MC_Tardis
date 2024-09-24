package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
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
            if (BlockUtils.getTardisBlockOwnership(blocks.get(i)) > 1) continue;
            blocks.remove(i);
            --i;
        }
    }

}
