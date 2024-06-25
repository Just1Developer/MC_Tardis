package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SchematicMaker implements CommandExecutor, Listener {

    private static final Map<UUID, Location[]> locations = new HashMap<>();

    private static final String SCHMAKER_ITEM_PERMISSION_GET = "tardis.schematic.maker.get";
    private static final String SCHMAKER_ITEM_PERMISSION_USE = "tardis.schematic.maker.use";

    private static final Material SCHMAKER_ITEM_MATERIAL = Material.NETHERITE_AXE;
    private static final int SCHMAKER_ITEM_ITEMDATA = 404;
    private static final String SCHMAKER_ITEM_NAME = "§4§lSchmaker";
    private static final String SCHMAKER_ITEM_LORE = "§7Schematic Maker, like worldedit axe";
    private static ItemStack SCHMAKER_ITEM = null;

    public SchematicMaker() {
        if (SCHMAKER_ITEM == null) {
            SCHMAKER_ITEM = new ItemStack(SCHMAKER_ITEM_MATERIAL);
            ItemMeta meta  = SCHMAKER_ITEM.getItemMeta();
            if (meta == null) return;
            meta.setDisplayName(SCHMAKER_ITEM_NAME);
            meta.setCustomModelData(SCHMAKER_ITEM_ITEMDATA);
            meta.setLore(List.of(SCHMAKER_ITEM_LORE));
            SCHMAKER_ITEM.setItemMeta(meta);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission(SCHMAKER_ITEM_PERMISSION_GET) && !commandSender.isOp()) {
            commandSender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("§cSorry, but you must be a player to use this command!");
            return true;
        }

        ((Player) commandSender).getInventory().addItem(SCHMAKER_ITEM);
        commandSender.sendMessage("§eYou have received: " + SCHMAKER_ITEM_NAME);

        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!isSchmaker(event.getItem())) return;
        if (!p.hasPermission(SCHMAKER_ITEM_PERMISSION_USE) && !p.isOp()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        assert event.getClickedBlock() != null;
        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation();

        int posNum = event.getAction() == Action.LEFT_CLICK_BLOCK ? 0 : 1;
        setPos(p, loc, posNum);
        p.sendMessage(String.format("§dSet position %d to %d %d %d.", posNum, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        if (!isSchmaker(itemStack)) return;
        if (!p.hasPermission(SCHMAKER_ITEM_PERMISSION_USE) && !p.isOp()) return;
        event.setCancelled(true);
        Location loc = event.getBlock().getLocation();

        setPos(p, loc, 0);
        p.sendMessage(String.format("§dSet position %d to %d %d %d.", 0, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    private static void setPos(Player player, Location location, int number) {
        if (player == null) return;
        if (number < 0 || number > 1) return;

        UUID uuid = player.getUniqueId();
        Location[] locs;
        if (!locations.containsKey(uuid)) {
            locs = new Location[] {null, null};
        } else {
            locs = locations.get(uuid);
        }
        locs[number] = location.clone();
        locations.put(uuid, locs);
    }

    public static Location getPos(Player player, int number) {
        if (player == null) return null;
        if (number < 0 || number > 1) return null;
        return locations.getOrDefault(player.getUniqueId(), new Location[] {null, null})[number];
    }

    private static boolean isSchmaker(ItemStack item) {
        if (item == null) return false;
        if (SCHMAKER_ITEM == null) return false;
        if (item.getType() != SCHMAKER_ITEM_MATERIAL) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.hasDisplayName()) return false;
        if (!meta.getDisplayName().equals(SCHMAKER_ITEM_NAME)) return false;
        if (!meta.hasLore()) return false;
        assert meta.getLore() != null;
        if (meta.getLore().size() != 1) return false;
        if (!meta.getLore().get(0).equals(SCHMAKER_ITEM_LORE)) return false;
        if (!meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == SCHMAKER_ITEM_ITEMDATA;
    }
}
