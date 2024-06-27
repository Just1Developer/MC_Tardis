package net.justonedev.mc.tardisplugin;

import net.justonedev.mc.tardisplugin.schematics.BlockData;
import net.justonedev.mc.tardisplugin.schematics.BlockMetaDataInjection;
import net.justonedev.mc.tardisplugin.schematics.Schematic;
import net.justonedev.mc.tardisplugin.schematics.SchematicFactory;
import net.justonedev.mc.tardisplugin.schematics.SchematicMaker;
import net.justonedev.mc.tardisplugin.tardis.Tardis;
import net.justonedev.mc.tardisplugin.tardis.TardisEvents;
import net.justonedev.mc.tardisplugin.tardis.TardisFiles;
import net.justonedev.mc.tardisplugin.tardis.TardisModelType;
import net.justonedev.mc.tardisplugin.tardis.TardisWorldGen;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TardisPlugin extends JavaPlugin implements Listener {

    public static TardisPlugin singleton;

    /**
     * Tardises uses ascending ID's to save tardises, but can't ensure correct order when loading.
     * To ensure IDs stay the same regardless, we're using a TreeMap with O(log n) access instead
     * and hope it's fast enough.
     */
    public Map<Integer, Tardis> tardises;
    public Map<UUID, Tardis> tardisesByEntityUUID;

    @Override
    public void onEnable() {
        // Plugin startup logic
        singleton = this;

        SchematicMaker schmaker = new SchematicMaker();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new TardisEvents(), this);
        pm.registerEvents(schmaker, this);

        getCommand("makeschematic").setExecutor(this);
        getCommand("buildschematic").setExecutor(this);
        getCommand("breakdownschematic").setExecutor(this);
        getCommand("schmaker").setExecutor(schmaker);

        if (this.getDescription().getVersion().toLowerCase().contains("dev")) {
            Bukkit.getLogger().info("This is a developer build. Registering additional commands spawnmodel, tptardisworld, home and test");
            getCommand("spawnmodel").setExecutor(this);
            getCommand("tptardisworld").setExecutor(this);
            getCommand("home").setExecutor(this);
            getCommand("test").setExecutor(this);
        }
        
        BlockData.init();

        tardises = new HashMap<>();
        tardisesByEntityUUID = new HashMap<>();
        TardisWorldGen.initialize();
        TardisFiles.initialize();
        
        System.out.println("ID: " + TardisModelType.TARDIS_OUTER_STATIC.customModelData);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        TardisFiles.saveAll();
    }
    
    public static ArmorStand spawnModel(Location _loc, TardisModelType modelType) {
        return spawnModel(_loc, modelType, true);
    }
    public static ArmorStand spawnModel(Location _loc, TardisModelType modelType, boolean fixLocation) {
        assert _loc.getWorld() != null;
        Location loc = fixLocation ? new Location(_loc.getWorld(), _loc.getBlockX() + 0.5, _loc.getBlockY(), _loc.getBlockZ() + 0.5, 0, 0) : _loc;
        assert loc.getWorld() != null;
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        // Since the tardis is hiding it completely and we need it visible for interaction, leave it visible
        armorStand.setInvisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setHeadPose(new EulerAngle(0, 0, 0));
        armorStand.setCustomName(String.format("tardis-%s-%s", modelType.modelName, armorStand.getUniqueId()));
        armorStand.setCustomNameVisible(false);
        armorStand.setGlowing(modelType.shouldGlow);
        armorStand.setSmall(modelType.isBaby);
        armorStand.setCollidable(true);

        if (modelType.baseMaterial != Material.AIR) {
            ItemStack itemModel = new ItemStack(modelType.baseMaterial);
            ItemMeta woolHeadMeta = itemModel.getItemMeta();
            assert woolHeadMeta != null;
            woolHeadMeta.setCustomModelData(modelType.customModelData);
            itemModel.setItemMeta(woolHeadMeta);

            assert armorStand.getEquipment() != null;
            armorStand.getEquipment().setHelmet(itemModel);
        }
        armorStand.setCanPickupItems(false);
        armorStand.setPersistent(true); // on by default anyway
        armorStand.setRemoveWhenFarAway(false);

        return armorStand;
    }

    /**
     * Returns a tardis by the UUID of its exterior door armor stand.
     * If the entity is not a door, will return null.
     * @param uuid The uuid of the armor stand.
     * @return The tardis. null if the armor stand is not a door.
     */
    public static Tardis getTardisByEntityUUID(UUID uuid) {
        return singleton.tardisesByEntityUUID.getOrDefault(uuid, null);
    }

    /**
     * Returns a tardis by any location located inside the interior of the tardis' plot.
     * If it's not valid, will return null.
     * @param loc The location.
     * @return The tardis. null if the location is not inside any tardis.
     */
    public static Tardis getTardisByAnyPlotLocation(Location loc) {
        Optional<Integer> tardisIDbyLoc = TardisWorldGen.calculateTardisIDbyLoc(loc.getBlockX(), loc.getBlockZ());
		return tardisIDbyLoc.map(integer -> singleton.tardises.getOrDefault(integer, null)).orElse(null);
	}
    // Todo the above function is broken, but only after restarting
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (command.getName().equals("spawnmodel")) {
            Tardis t = Tardis.createNewTardis(p.getUniqueId());
            t.spawnTardis(p.getLocation().clone().add(1, 0, 0));
        } else if (command.getName().equals("tptardisworld")) {
            p.teleport(TardisWorldGen.getInteriorWorld().getSpawnLocation());
        } else if (command.getName().equals("home")) {
            Location loc = p.getRespawnLocation();
            if (loc == null) loc = Bukkit.getWorlds().get(0).getSpawnLocation();
            p.teleport(loc);
        } else if (command.getName().equals("makeschematic")) {
            String name = args[0];
            Location firstLoc, secondLoc;

            if (args.length > 1) {
                firstLoc = new Location(p.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } else {
                firstLoc = SchematicMaker.getPos(p, 0);
            }
            if (args.length > 4) {
                secondLoc = new Location(p.getWorld(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
            } else {
                secondLoc = SchematicMaker.getPos(p, 1);
            }

            if (firstLoc == null || secondLoc == null || firstLoc.getWorld() == null) {
                p.sendMessage("§cError: At least one location specified was null.");
                return false;
            }

            if (!firstLoc.getWorld().equals(secondLoc.getWorld())) {
                p.sendMessage("§cError: The two locations must be in the same world.");
                return false;
            }

            boolean captureAir = false;
            if (args.length > 7) captureAir = Boolean.parseBoolean(args[7]);
            
            SchematicFactory.createSchematicAsync(name, firstLoc, secondLoc, captureAir);
        } else if (command.getName().equals("buildschematic")) {
            File file = new File(getDataFolder() + "/schematics/", args[0] + Schematic.FILE_ENDING);
            if (!file.exists()) {
                p.sendMessage("Couldn't find file " + file.getAbsolutePath());
                return true;
            }
            Schematic schematic = new Schematic(file);
            (args[0].startsWith("tardis") ? schematic
                    .with(new BlockMetaDataInjection(Material.DIAMOND_BLOCK).addMetadataTag(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG, Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)
                            .addRunFunction(Tardis.getSetOwnershipFunction(Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)))
                    .with(new BlockMetaDataInjection(Material.NETHERITE_BLOCK).addMetadataTag(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG, Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)
                            .addRunFunction(Tardis.getSetOwnershipFunction(Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)))
                    .with(new BlockMetaDataInjection(Material.GLASS).addMetadataTag(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG, Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)
                            .addRunFunction(Tardis.getSetOwnershipFunction(Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE)))
                    .with(new BlockMetaDataInjection(Material.AIR).addMetadataTag(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG, Tardis.SHELL_TARDIS_GENERATED_METADATA_VALUE)
                            .addRunFunction(Tardis.getSetOwnershipFunction(Tardis.SHELL_TARDIS_GENERATED_METADATA_VALUE)))
                    : schematic)
                    .placeInWorldAsync(p.getLocation().clone().add(p.getLocation().getDirection()).add(1, 0, 1));
        } else if (command.getName().equals("breakdownschematic")) {
            File file = new File(getDataFolder() + "/schematics/", args[0] + Schematic.FILE_ENDING);
            if (!file.exists()) {
                p.sendMessage("Couldn't find file " + file.getAbsolutePath());
                return true;
            }
            Schematic schematic = new Schematic(file);
            schematic.placeBreakdown(p.getLocation().clone().add(p.getLocation().getDirection()).add(1, 0, 1));
        } else if (command.getName().equals("test")) {
            Block b = p.getLocation().clone().add(1, 0, 0).getBlock();
            b.setType(Material.OAK_STAIRS);
            var wdata = (Waterlogged) b.getBlockData();
            wdata.setWaterlogged(true);
            b.setBlockData(wdata);
            var rdata = (Directional) b.getBlockData();
            rdata.setFacing(BlockFace.NORTH);
            b.setBlockData(rdata);
            
            Block b2 = p.getLocation().clone().add(4, 0, 0).getBlock();
            b2.setType(Material.OAK_STAIRS);
            var wdata2 = (Waterlogged) b2.getBlockData();
            wdata2.setWaterlogged(true);
            b2.setBlockData(wdata2);
            var rdata2 = (Directional) b2.getBlockData();
            rdata2.setFacing(BlockFace.WEST);
            b2.setBlockData(rdata2);
            
            Block b3 = p.getLocation().clone().add(2, 1, 0).getBlock();
            b3.setType(Material.OAK_STAIRS);
            var sdata3 = (Stairs) b3.getBlockData();
            sdata3.setWaterlogged(true);
            sdata3.setFacing(BlockFace.WEST);
            b3.setBlockData(sdata3);
        }
        return true;
    }
}
