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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Optional;

public class TardisEvents implements Listener {
    
    private static final double DOOR_ANGLE_OK_THRESHOLD = 70;
    
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
    public void debugMove(PlayerMoveEvent e) {
        
        Tardis t = TardisPlugin.singleton.tardises.getOrDefault(0, null);
        if (t == null) return;
        Vector v = t.getCurrentModelDirectionVector();
        if (dotProduct(v, v) == 0) {
            Bukkit.broadcastMessage("v is nullvector");
            return;
        }
        
        // Vector (0, 0, -1) should return around 180° or -180°
        // Vector (0, 0, 1) should return around 0°
        // Vector (1, 0, 0) should return around -90°
        // Vector (-1, 0, 0) should return around 90°
        //Bukkit.broadcastMessage("§eYaw: " + e.getPlayer().getLocation().getYaw() + " Y: " + (Math.toDegrees((Math.cos(v.getX()) + Math.sin(v.getZ()))) - 0) + " Y: " + (2 * Math.PI * (Math.cos(v.getX()) + Math.sin(v.getZ())) - 180));
        //Bukkit.broadcastMessage("Dot product: " + dotProduct(v, e.getPlayer().getLocation().getDirection()));
        //Bukkit.broadcastMessage("Angle: " + calculateAngle(v, e.getPlayer().getLocation().getDirection()));
        
        // From polar coordinates, we know r=1
        // x = cos(alpha)
        // z = sin(alpha)
        // => Alpha = arccos(x) = arcsin(z)
        
        // We assume v is normalized such that |v| = 1
        //for (Vector v2 : new Vector[] { new Vector(0, 0, -1), new Vector(0, 0, 1), new Vector(1, 0, 0), new Vector(-1, 0, 0) }) {
        //    Bukkit.broadcastMessage("vect: " + v2 + " angle: " + calculateAngle(v, v2) + " arccos: " + (2 * Math.PI * Math.acos(v2.getX()) - 180) + " arcsin: " + (2 * Math.PI * Math.asin(v2.getZ()) - 180));
        //}
        
        Vector pv = e.getPlayer().getLocation().getDirection();
        if (isWithinDoorAngleTolerance(pv, v)) Bukkit.broadcastMessage("§aYES");
        else Bukkit.broadcastMessage("§cNO");
        
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if (!isTardisComponent(entity)) return;
        e.setCancelled(true);
    }
    
    //region Angle-Helpers
    
    /**
     * Builds the dot product between two 2D-Vectors (considers only x and z).
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return The dot product of the two vectors.
     */
    private double dotProduct(Vector v1, Vector v2) {
        return v1.getX() * v2.getX() + v1.getZ() * v2.getZ();
    }
    
    /**
     * Calculates the determinant of the cross product of two 2D vectors, which is equivalent to the z-component of the cross product in 3D.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return the z-component of the cross product.
     */
    private double crossProductZ(Vector v1, Vector v2) {
        return v1.getX() * v2.getZ() - v1.getZ() * v2.getX();
    }
    
    /**
     * Calculates the oriented angle between two vectors.<br/>
     * <br/>
     * The method first computes the angle using the arccosine of the dot product
     * of the vectors divided by the product of their magnitudes, which provides
     * the smallest angle in radians. It then adjusts the sign of the angle based
     * on the z-component of the cross product of the two vectors. A negative
     * z-component indicates that the angle should be negative, reflecting that
     * the second vector is clockwise relative to the first vector.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the oriented angle in degrees, ranging from -180° to 180°.
     */
    private double calculateAngle(Vector v1, Vector v2) {
        double angle = (Math.acos(dotProduct(v1, v2) / (v1.length() * v2.length())));
        if (crossProductZ(v1, v2) < 0) {
            angle = -angle; // adjust the angle to reflect direction
        }
        return Math.toDegrees(angle);
    }
    
    /**
     * Checks if the angle between a player's facing direction and a door's facing direction
     * is within a specified tolerance.<br/>
     * <br/>
     * This method utilizes {@link #calculateAngle(Vector, Vector)} to determine the oriented angle
     * between the player's vector and the door's vector. It then checks if the absolute value of this
     * angle is less than or equal to the defined threshold, `DOOR_ANGLE_OK_THRESHOLD`. This threshold
     * defines the maximum angle deviation allowed for the player to be considered facing the door
     * directly enough to trigger an interaction or action.
     *
     * @param playerVector the vector representing the player's facing direction
     * @param doorVector the vector representing the door's facing direction
     * @return true if the absolute angle is within the threshold, false otherwise.
     */
    private boolean isWithinDoorAngleTolerance(Vector playerVector, Vector doorVector) {
        double angle = calculateAngle(playerVector, doorVector);
        return Math.abs(angle) <= DOOR_ANGLE_OK_THRESHOLD;
    }
    
    //endregion
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isTardisComponent(Entity entity) {
        if (entity.getType() != EntityType.ARMOR_STAND) return false;
        if (entity.getCustomName() == null) return false;
        return entity.getCustomName().startsWith("tardis");
    }

}
