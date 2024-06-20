package net.justonedev.mc.tardisplugin.tardis;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.CONSOLE_CENTER_HEIGHT;
import static net.justonedev.mc.tardisplugin.tardis.TardisWorldGen.PLOT_CENTER;

public class Tardis {

    private static final String SHELL_GENERATED_BY_WHO_METADATA_TAG = "shell-interior-owner";
    private static final int SHELL_TARDIS_GENERATED_IMMORTAL_METADATA_VALUE = 0;
    private static final int SHELL_TARDIS_GENERATED_METADATA_VALUE = 1;
    private static final int SHELL_PLAYER_GENERATED_METADATA_VALUE = 2;

    private final int numericID;
    private final UUID owner;
    private final UUID tardisUUID;
    private final int outerShellDesignIndex;
    private final TardisInteriorPlot interiorPlot;
    private final Location spawnLocation;

    private ArmorStand currentModelTardis;
    private ArmorStand currentModelDoor;

    private TardisConsole tardisConsole;

    Tardis(UUID owner, UUID tardisUUID, int outerShellDesignIndex, int interiorPlotID) {
        this.numericID = interiorPlotID;
        this.owner = owner;
        this.tardisUUID = tardisUUID;
        this.outerShellDesignIndex = outerShellDesignIndex;
        // Todo do we need this?
        Optional<TardisInteriorPlot> plot = TardisWorldGen.calculateInteriorPlotByID(interiorPlotID);
        this.interiorPlot = plot.orElseGet(() -> new TardisInteriorPlot((int) (-3 * 1e7), (int) (-3 * 1e7)));
        new TardisConsole(this);

        Location loc = getAbsoluteConsoleLocation();
        spawnLocation = new Location(loc.getWorld(), loc.getBlockX() - 10.5, loc.getY(), loc.getBlockZ() + 0.5, 0, 0);

        TardisPlugin.singleton.tardises.put(interiorPlotID, this);
    }
    int i = 0;

    public static Tardis createNewTardis(UUID owner) {
        UUID uuid = UUID.randomUUID();
        Tardis tardis = new Tardis(owner, uuid, 1, TardisPlugin.singleton.tardises.size());
        tardis.build();
        return tardis;
    }

    public void spawnTardis(Location where) {
        currentModelTardis = TardisPlugin.spawnModel(where, TardisModelType.TARDIS_OUTER_STATIC);
        bindCurrentModelTardis(currentModelTardis.getUniqueId(), false);
    }

    public void setShellModelData(int modelData) {
        Optional<ArmorStand> stand = getCurrentModelTardis();
        if (stand.isEmpty()) return;
        if (stand.get().getEquipment() == null) return;
        ItemStack item = stand.get().getEquipment().getHelmet();
        if (item == null || true) item = new ItemStack(Material.BEDROCK); // todo temp change to glass
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        stand.get().getEquipment().setHelmet(item);
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
        //TardisPlugin.singleton.tardisesByEntityUUID.remove(currentModelDoor.getUniqueId());
        TardisPlugin.singleton.tardisesByEntityUUID.remove(currentModelTardis.getUniqueId());
        currentModelTardis.remove();
        //currentModelDoor.remove();
    }
    
    UUID getCurrentModelTardisUUID() {
        return currentModelTardis == null ? null : currentModelTardis.getUniqueId();
    }
    
    /**
     * Gets a direction vector from the ArmorStand model's viewing direction. Returned vector is a copy.
     * Returns a vector (0,0,0) when anything is null.
     * @return A copy of a direction vector of the tardis shell.
     */
    Vector getCurrentModelDirectionVector() {
        if (currentModelTardis == null) return new Vector(0, 0, 0);
        return currentModelTardis.getLocation().getDirection().clone();
    }
    
    /**
     * Gets the tardis entity or an empty optional if the tardis is not present in the 'real' world.
     * @return The tardis entity or empty optional.
     */
    Optional<ArmorStand> getCurrentModelTardis() {
        return currentModelTardis == null ? Optional.empty() : Optional.of(currentModelTardis);
    }
    
    void bindCurrentModelTardis(UUID armorStandUUID) {
        bindCurrentModelTardis(armorStandUUID, true);
    }
    void bindCurrentModelTardis(UUID armorStandUUID, boolean overrideModel) {
        if (armorStandUUID == null) return;
        if (overrideModel) this.currentModelTardis = (ArmorStand) Bukkit.getEntity(armorStandUUID);
        TardisPlugin.singleton.tardisesByEntityUUID.put(armorStandUUID, this);
        // Todo if the model data suggests disappearing animation, remove and do not bind
        // Todo if the model data suggests appearing animation, just set the tardis there.
        
        
        int[] nums = { 2500, 2501, 2502, 2503, 2504 };
        i = 0;
        Bukkit.broadcastMessage("§5Current model: " + currentModelTardis);
        System.out.println("§5Current model: " + currentModelTardis);
        if (this.currentModelTardis != null) {
            Bukkit.broadcastMessage("§cStarting scheduler");
            System.out.println("§cStarting scheduler");
            Bukkit.getScheduler().scheduleSyncRepeatingTask(TardisPlugin.singleton, () -> {
                Bukkit.broadcastMessage("Setting data to " + nums[i] + " for tardis at " + currentModelTardis.getLocation());
                System.out.println("Setting data to " + nums[i] + " for tardis at " + currentModelTardis.getLocation());
                setShellModelData(nums[i]);
                Bukkit.broadcastMessage("Model data now: " + getShellModelData());
                System.out.println("Model data now: " + getShellModelData());
                i++;
                if (i >= nums.length) i = 0;
            }, 20, 20);
        }
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

}
