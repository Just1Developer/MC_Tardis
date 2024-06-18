package net.justonedev.mc.tardisplugin;

import net.justonedev.mc.tardisplugin.animation.Animation;
import net.justonedev.mc.tardisplugin.animation.AnimationType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;

import java.util.List;

public final class TardisPlugin extends JavaPlugin implements Listener {
    
    public static TardisPlugin singleton;
    private NamespacedKey animatedKey;
    private NamespacedKey CUSTOM_TAG_KEY;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        singleton = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        animatedKey = new NamespacedKey(this, "animated");
        CUSTOM_TAG_KEY = new NamespacedKey(this, "customtag");
        getCommand("test").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity.getType() != EntityType.ARMOR_STAND) continue;
            if (!entity.isCustomNameVisible()) continue;
            if (entity.getCustomName() == null) continue;
            if (entity.getCustomName().equals("debug")) entity.remove();
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() == Material.BEACON) {
            TileState tileState = (TileState) block.getState();
            tileState.getPersistentDataContainer().set(new NamespacedKey(this, "tag3"), PersistentDataType.STRING, "one");
        } else if (block.getType() == Material.STONE) {
            spawnWoolHeadArmorStands(block.getLocation(), event.getPlayer().isSneaking());
        }
    }
    
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
    
    private void spawnArmorStand(Location loc, Material material, boolean baby, boolean glow) {
        // Create the armor stand
        assert loc.getWorld() != null;
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setHeadPose(new EulerAngle(0, 0, 0));
        armorStand.setCustomName("debug");   // for debug
        armorStand.setCustomNameVisible(false);
        armorStand.setGlowing(glow);
        armorStand.setSmall(baby);
        armorStand.setCollidable(true);
        
        // Set a random wool color on the armor stand's head
        ItemStack woolHead = new ItemStack(material);
        ItemMeta woolHeadMeta = woolHead.getItemMeta();
		assert woolHeadMeta != null;
		woolHeadMeta.setCustomModelData(2);
        woolHead.setItemMeta(woolHeadMeta);
        
        assert armorStand.getEquipment() != null;
        armorStand.getEquipment().setHelmet(woolHead);
    }
    
    /*
    @EventHandler
    public void onTestTriggerEvent(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() != Material.BEDROCK) return;
        if (e.getPlayer().isSneaking())
            Animation.startAnimation(e.getPlayer(), e.getBlockPlaced(), AnimationType.TARDIS_OUTER_ENTRY, 1, 4);
        else {
            //Animation.switchToAnimatedTexture(e.getPlayer(), e.getBlockPlaced());
            setBlockAnimationState(e.getBlockPlaced(), true);
            Bukkit.broadcastMessage("1");
            return;
        }
        Bukkit.broadcastMessage("2");
    }*/
    
    long lastTime = 0;
    
    boolean bool = false;
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
		assert b != null;
		if (b.getType() == Material.BEDROCK) {
            
            // Rudimentary 1s cooldown
            long time = System.currentTimeMillis();
            if (lastTime + 50 >= time) return;
            lastTime = time;
        
        
        String next = getOther(getCustomTexture(b));
        setCustomTexture(b, next);
        b.getState().update(true);
        e.getPlayer().sendBlockChange(b.getLocation(), b.getBlockData());
        
        Bukkit.broadcastMessage("3 switched to blockstate " + next);
        
        } else if (b.getType() == Material.BEACON) {
            e.setCancelled(true);
            // Persistent Data Holder
            String customTag = bool ? "tardis" : "none";
            bool = !bool;
            Bukkit.broadcastMessage("Setting tag to " + customTag);
            if (customTag != null) {
                // Custom block logic
                //BlockData data = Bukkit.createBlockData("minecraft:beacon[custom=" + customTag + "]");
                //b.setBlockData(data);
            } else {
                // Normal bedrock logic
                //BlockData data = Bukkit.createBlockData("minecraft:beacon[custom=none]");
                //b.setBlockData(data);
            }
            setCustomPersistentTexture(b, customTag);
            b.getState().update(true);
            
            // Force client-side update
            if (e.getPlayer().isSneaking()) {
                b.getWorld().getBlockAt(b.getLocation()).setType(Material.AIR);
                b.getWorld().getBlockAt(b.getLocation()).setType(Material.BEACON);
            }
        }
     
     
    }
    
    private String getOther(String s) {
        if (s.equals("stone")) return "ore";
        return "stone";
    }
    
    /*
    private void setBlockAnimationState(Block block, boolean animated) {
        BlockData blockData = block.getBlockData();
        blockData.getAsString();
        
        if (animated) {
            block.getPersistentDataContainer().set(animatedKey, PersistentDataType.INTEGER, 1);
            blockData = Material.BEDROCK.createBlockData("[animated=true]");
        } else {
            block.getPersistentDataContainer().set(animatedKey, PersistentDataType.INTEGER, 0);
            blockData = Material.BEDROCK.createBlockData("[animated=false]");
        }
        
        block.setBlockData(blockData, true);
    }*/
    
    public void setCustomTexture(Block block, String textureName) {
        // Set custom NBT tag
        block.setMetadata("customTexture", new FixedMetadataValue(this, textureName));
    }
    
    public String getCustomTexture(Block block) {
        // Get custom NBT tag
        List<MetadataValue> metadata = block.getMetadata("customTexture");
        if (!metadata.isEmpty()) {
            return metadata.get(0).asString();
        }
        return ""; // No custom texture found
    }
    
    public void setCustomPersistentTexture(Block block, String value) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            tileState.getPersistentDataContainer().set(CUSTOM_TAG_KEY, PersistentDataType.STRING, value);
            tileState.update(); // Ensure to update the tile state to persist the changes
        }
    }
    
    public String getCustomPersistentTexture(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            if (tileState.getPersistentDataContainer().has(CUSTOM_TAG_KEY, PersistentDataType.STRING)) {
                return tileState.getPersistentDataContainer().get(CUSTOM_TAG_KEY, PersistentDataType.STRING);
            }
        }
        return "none"; // No custom texture found
    }
    
    
    
    
    // Newer
    
    
    
    private String getCustomTag(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            if (tileState.getPersistentDataContainer().has(CUSTOM_TAG_KEY, PersistentDataType.STRING)) {
                return tileState.getPersistentDataContainer().get(CUSTOM_TAG_KEY, PersistentDataType.STRING);
            }
        }
        return "none"; // Default or no custom tag
    }
    
    private void setCustomTag(Block block, String value) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            TileState tileState = (TileState) state;
            tileState.getPersistentDataContainer().set(CUSTOM_TAG_KEY, PersistentDataType.STRING, value);
            tileState.update(); // Ensure to update the tile state to persist the changes
        }
    }
    
    private void updateBlockAppearance(Block block, String customTag) {
        // Update the block appearance based on the customTag
        // This can be done by forcing a block update or changing its state if possible
        // As an example, using setType to force a client-side update:
        Material originalType = block.getType();
        block.setType(Material.AIR); // Temporarily set to air
        block.setType(originalType); // Reset to original type
        
        // Optionally, you could send a custom packet to update the block appearance on the client side
    }
}
