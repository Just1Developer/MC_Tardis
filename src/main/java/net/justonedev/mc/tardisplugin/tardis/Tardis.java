package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import net.justonedev.mc.tardisplugin.schematics.BlockRunnable;
import net.justonedev.mc.tardisplugin.tardisdata.TardisBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.justonedev.mc.tardisplugin.TardisPlugin.getTardisByAnyPlotLocation;
import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.CONSOLE_CENTER_HEIGHT;
import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.PLOT_CENTER;

public class Tardis {

    public static final String SHELL_GENERATED_BY_WHO_METADATA_TAG = "shell-interior-owner";
    public static final int SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE = 0;
    public static final int SHELL_TARDIS_GENERATED_METADATA_VALUE = 1;
    public static final int SHELL_PLAYER_GENERATED_METADATA_VALUE = 2;
    
    private static final String SOUND_NAME_TAKEOFF = "tardis.sfx.takeoff";
    private static final String SOUND_NAME_CLOISTER_BELL = "tardis.sfx.bell";
    private static final String SOUND_NAME_ARRIVE = "tardis.sfx.arrive";

    private final int numericID;
    private final UUID owner;
    private final UUID tardisUUID;
    private final int outerShellDesignIndex;
    private final int interiorPlotID;
    private final TardisInteriorPlot interiorPlot;
    private final Location spawnLocation;

    private UUID tardisOuterShellUUID;
    private Location tardisOuterShellLocation;
    private Vector tardisOuterShellDirection;
    private Location tardisOuterShellSpawnLocation;

    private TardisCharger tardisCharger;

    private TardisConsole tardisConsole;

    Tardis(UUID owner, UUID tardisUUID, int outerShellDesignIndex, int interiorPlotID) {
        this.numericID = interiorPlotID;
        this.owner = owner;
        this.tardisUUID = tardisUUID;
        this.interiorPlotID = interiorPlotID;
        this.outerShellDesignIndex = outerShellDesignIndex;
        // Todo do we need this?
        Optional<TardisInteriorPlot> plot = TardisWorldGen.calculateInteriorPlotByID(interiorPlotID);
        this.interiorPlot = plot.orElseGet(() -> new TardisInteriorPlot((int) (-3 * 1e7), (int) (-3 * 1e7)));
        new TardisConsole(this);

        Location loc = getAbsoluteConsoleLocation();
        spawnLocation = new Location(loc.getWorld(), loc.getBlockX() - 10.5, loc.getY(), loc.getBlockZ() + 0.5, 0, 0);

        ownershipTracking = new ArrayList<Set<Vector>>();

        TardisPlugin.singleton.tardises.put(interiorPlotID, this);
        tardisCharger = new TardisCharger(this);
    }

    public static Tardis createNewTardis(UUID owner) {
        UUID uuid = UUID.randomUUID();
        Tardis tardis = new Tardis(owner, uuid, 1, TardisPlugin.singleton.tardises.size());
        tardis.build();
        return tardis;
    }

    private int animationScheduler;
    private int currentFrame;
    boolean up = false;
    int FRAME_COUNT = 16;
    
    public void spawnTardis(Location where) {
        var currentModelTardis = TardisPlugin.spawnModel(where, TardisModelType.TARDIS_OUTER_STATIC);
        tardisOuterShellUUID = currentModelTardis.getUniqueId();
        tardisOuterShellLocation = currentModelTardis.getLocation();
        tardisOuterShellDirection = currentModelTardis.getLocation().getDirection();
        tardisOuterShellSpawnLocation = tardisOuterShellLocation.clone().add(tardisOuterShellDirection);
        bindCurrentModelTardis(tardisOuterShellUUID, false);
        
        /*
        if (System.nanoTime() > 0) {
            setUseTransparent(true);
            setShellModelData(2001);
            Entity e = Bukkit.getEntity(tardisOuterShellUUID);
            if (e != null) {
                ((ArmorStand) e).setInvulnerable(false);
                ((ArmorStand) e).setInvisible(false);
                ((ArmorStand) e).setGlowing(true);
            }
            return;
        }*/
        
        if (Bukkit.getScheduler().isCurrentlyRunning(animationScheduler)) return;
        
        setUseTransparent(true);
        ArmorStand a = (ArmorStand) Bukkit.getEntity(tardisOuterShellUUID);
        if (a != null) a.setInvisible(true);
        currentFrame = 0;
        if (where.getWorld() != null) where.getWorld().playSound(where, SOUND_NAME_ARRIVE, 3.0f, 1.0f);
        animationScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TardisPlugin.singleton, () -> {
            setShellModelData(TardisConstants.DATA_TARDIS_ANIMATION_FRAMES + toModelDataDelta());
            currentFrame++;
            if (currentFrame > 9 * (20 / 2)) {
                ArmorStand b = (ArmorStand) Bukkit.getEntity(tardisOuterShellUUID);
                if (b != null) b.setInvisible(false);
                setUseTransparent(false);
                setShellModelData(TardisConstants.DATA_TARDIS_SHELL_ORIGINAL);
                Bukkit.getScheduler().cancelTask(animationScheduler);
            }
        }, 0, 2);

        /*
        Bukkit.getScheduler().scheduleSyncDelayedTask(TardisPlugin.singleton, () -> {
            Entity e = Bukkit.getEntity(tardisOuterShellUUID);
            if (e != null) {
                e.setInvulnerable(false);
                e.setGlowing(true);
                e.remove();
            }
        }, 300);*/
    }
    
    int currentValue = FRAME_COUNT;
    private int toModelDataDelta() {
        int value = currentValue;
        if (up) {
            currentValue++;
            if (currentValue >= FRAME_COUNT - (currentFrame / 12)) up = false;
        } else {
            currentValue--;
            if (currentValue <= 3) up = true;
        }
        return value;
    }

    public void setShellModelData(int modelData) {
        Optional<ArmorStand> stand = getCurrentModelTardis();
        if (stand.isEmpty()) return;
        if (stand.get().getEquipment() == null) return;
        ItemStack item = stand.get().getEquipment().getHelmet();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        stand.get().getEquipment().setHelmet(item);
    }

    public void setUseTransparent(boolean transparent) {
        Optional<ArmorStand> stand = getCurrentModelTardis();
        if (stand.isEmpty()) return;
        if (stand.get().getEquipment() == null) return;
        ItemStack item = stand.get().getEquipment().getHelmet();
        Material newMat = transparent ? TardisConstants.TARDIS_ANIMATED_SHELL_MATERIAL : TardisConstants.TARDIS_STATIC_SHELL_MATERIAL;
        
        ItemStack newItem;
        int modelData = getShellModelData();
        if (modelData == -1) modelData = TardisConstants.DATA_TARDIS_SHELL_ORIGINAL;
        if (item == null || item.getType() != newMat) newItem = new ItemStack(newMat);
        else return;
        
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) return;
        meta.setCustomModelData(modelData);
        newItem.setItemMeta(meta);
        stand.get().getEquipment().setHelmet(newItem);
    }

    public int getShellModelData() {
        Optional<ArmorStand> stand = getCurrentModelTardis();
        if (stand.isEmpty()) return -1;
        if (stand.get().getEquipment() == null) return -1;
        ItemStack item = stand.get().getEquipment().getHelmet();
        if (item == null) item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;
        return meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
    }

    public void despawnTardis() {
        if (tardisOuterShellUUID == null) return;
        if (tardisOuterShellLocation == null) return;
        if (tardisOuterShellLocation.getWorld() == null) return;

        tardisOuterShellLocation.getWorld().loadChunk(tardisOuterShellLocation.getChunk());
        Entity currentModelTardis = Bukkit.getEntity(tardisOuterShellUUID);

        if (currentModelTardis == null) {
            Bukkit.getLogger().warning("Despawning of tardis @ " + tardisOuterShellLocation + " failed, could not get bound entity.");
            return;
        }

        TardisPlugin.singleton.tardisesByEntityUUID.remove(currentModelTardis.getUniqueId());
        currentModelTardis.remove();
    }
    
    Optional<UUID> getTardisOuterShellUUID() {
        return Optional.of(tardisOuterShellUUID);
    }

    int getInteriorPlotID() {
        return interiorPlotID;
    }
    
    /**
     * Gets a direction vector from the ArmorStand model's viewing direction. Returned vector is a copy.
     * Returns a vector (0,0,0) when anything is null.
     * @return A copy of a direction vector of the tardis shell.
     */
    Optional<Vector> getCurrentModelDirectionVector() {
        return Optional.of(tardisOuterShellDirection);
    }
    
    /**
     * Gets the tardis entity or an empty optional if the tardis is not present in the 'real' world.
     * @return The tardis entity or empty optional.
     */
    Optional<ArmorStand> getCurrentModelTardis() {
        return getCurrentModelTardis(tardisOuterShellLocation, tardisOuterShellUUID);
    }

    /**
     * Gets the tardis entity or an empty optional if the tardis is not present in the 'real' world.
     * @return The tardis entity or empty optional.
     */
    private Optional<ArmorStand> getCurrentModelTardis(Location tardisOuterShellLocation, UUID tardisOuterShellUUID) {
        if (tardisOuterShellUUID == null) return Optional.empty();
        if (tardisOuterShellLocation == null) return Optional.empty();
        if (tardisOuterShellLocation.getWorld() == null) return Optional.empty();

        tardisOuterShellLocation.getWorld().loadChunk(tardisOuterShellLocation.getChunk());
        Entity currentModelTardis = Bukkit.getEntity(tardisOuterShellUUID);
        if (currentModelTardis == null) return Optional.empty();
        if (currentModelTardis.getType() != EntityType.ARMOR_STAND) return Optional.empty();
        return Optional.of((ArmorStand) currentModelTardis);
    }

    /**
     * Gets the location of the tardises' outer shell or an empty optional if the tardis is not present in the 'real' world.
     * @return The tardis shell location or empty optional.
     */
    Optional<Location> getOuterShellLocation() {
        return tardisOuterShellLocation == null ? Optional.empty() : Optional.of(tardisOuterShellLocation);
    }
    
    /**
     * Only use in setup, please.
     * @param loc The location.
     */
    void setTardisOuterShellLocation(Location loc) {
        this.tardisOuterShellLocation = loc;
    }
    
    /**
     * Might be null.
     * @return Null or location.
     */
    Location getTardisOuterShellLocation() {
        return this.tardisOuterShellLocation;
    }
    
    void bindCurrentModelTardis(UUID armorStandUUID) {
        bindCurrentModelTardis(armorStandUUID, true);
    }
    void bindCurrentModelTardis(UUID armorStandUUID, boolean overrideModel) {
        
        // Todo for binding to work, the chunk where the tardis is located must be loaded
        // Todo: work with UUID instead and query entity when necessary
        
        // Todo: Or save shell location
        
        if (armorStandUUID == null) return;
        if (overrideModel) this.tardisOuterShellUUID = armorStandUUID;
        TardisPlugin.singleton.tardisesByEntityUUID.put(armorStandUUID, this);

        tardisCharger.startCheckingForCharge();

        // Todo if the model data suggests disappearing animation, remove and do not bind
        // Todo if the model data suggests appearing animation, just set the tardis there.
    }

    /**
     * Teleports a player into the tardis.
     * @param player The player.
     */
    public void enter(Player player) {
        player.teleport(spawnLocation);
    }

    /**
     * Builds the console center room and everything. Typically done when creating a new tardis.
     */
    public void build() {
        // of all blocks we build, set the metadata. Metadata can have 3 values:
        // 0: Block is part of the shell, and is vital, so is marked as indestructible.
        //    -> Console, time rotor or door, for example.
        // 1: Block is part of the shell, but placed by tardis. Do not refund when destroyed.
        // 2: Block is part of the shell, but placed by player. Refund when destroyed.
        tardisConsole.build();
    }

    void setConsole(TardisConsole console) {
        this.tardisConsole = console;
    }

    public Location getAbsoluteConsoleLocation() {
        return new Location(TardisWorldGen.getInteriorWorld(),
                interiorPlot.getBeginX() + PLOT_CENTER,
                CONSOLE_CENTER_HEIGHT,
                interiorPlot.getBeginZ() + PLOT_CENTER);
    }

    public int getNumericID() {
        return numericID;
    }

    public String getOwnerUUIDString() {
        return owner.toString();
    }

    public String getTardisUUIDString() {
        return tardisUUID.toString();
    }

    public String getFullUUIDString() {
        return String.format("%s.%s", getOwnerUUIDString(), getTardisUUIDString());
    }

    public TardisInteriorPlot getInteriorPlot() {
        return interiorPlot;
    }

    public int getOuterShellDesignIndex() {
        return outerShellDesignIndex;
    }

    //region Block ownership

    List<Set<Vector>> ownershipTracking;

    public Set<Vector> getBlocksOwnedBy(int ownerID) {
        if (ownerID < 0 || ownerID >= ownershipTracking.size()) return Set.of();
        return ownershipTracking.get(ownerID);
    }

    public void setBlocksOwnedBy(int ownerID, Set<Vector> vectors) {
        ownershipTracking.set(ownerID, new HashSet<>(vectors));
    }

    public void addBlockOwnershipTag(Vector vector, int newOwner) {
        if (newOwner < 0) return;
        if (newOwner >= TardisBlockData.BLOCKDATA_VALUES) return;
        while (ownershipTracking.size() <= newOwner) ownershipTracking.add(new HashSet<>());
        ownershipTracking.get(newOwner).add(vector);
    }

    public void changeBlockOwnershipTag(Vector vector, int newOwner) {
        for (var set : ownershipTracking) {
            if (set.remove(vector)) break;
        }
        addBlockOwnershipTag(vector, newOwner);
    }

    public void addBlockOwnershipTag(Location totalLocation, int newOwner) {
        addBlockOwnershipTag(toRelativeVector(totalLocation), newOwner);
    }

    public void changeBlockOwnershipTag(Location totalLocation, int newOwner) {
        changeBlockOwnershipTag(toRelativeVector(totalLocation), newOwner);
    }

    private Vector toRelativeVector(Location totalLocation) {
        return new Vector(totalLocation.getBlockX(), totalLocation.getBlockY(), totalLocation.getBlockZ())
                .subtract(interiorPlot.getMinLoc().toVector());
    }

    public static BlockRunnable getSetOwnershipFunction(int ownerID) {
        return (block) -> {
            Bukkit.broadcastMessage("§e1");
            Tardis tardis = getTardisByAnyPlotLocation(block.getLocation());
            Bukkit.broadcastMessage("§a2 + " + tardis);
            if (tardis == null) return;
            Bukkit.broadcastMessage("§d3");
            if (block.getLocation().getWorld() == null || !block.getLocation().getWorld().equals(TardisWorldGen.getInteriorWorld())) return;
            Bukkit.broadcastMessage("Adding ownership tag " + ownerID + " to location " + block.getLocation());
            tardis.addBlockOwnershipTag(block.getLocation(), Tardis.SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE);
        };
    }

    //endregion

}
