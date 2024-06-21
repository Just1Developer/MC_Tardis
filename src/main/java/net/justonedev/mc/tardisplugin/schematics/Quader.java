package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.util.Vector;

public class Quader {

    private boolean locked;
    final BlockData quaderData;
    QuaderDimensions quaderDimensions;

    public Quader(BlockData blockData, QuaderDimensions dimensions, byte quaderOrientation) {
        this.quaderData = blockData; // I think reference is fine since it's read-only everywhere
        this.quaderDimensions = dimensions.copy(quaderOrientation);
        this.locked = true;
    }
    
    public Quader(BlockData blockData, Vector startPos, Vector endpos) {
        // This is just for expanding later
        final Vector minVect = minimalLocation(startPos, endpos);
        this.quaderData = blockData.copy(minVect);
        this.quaderDimensions = new QuaderDimensions(maximalLocation(startPos, endpos).subtract(minVect));
        this.locked = false;
    }
    
    private static Vector minimalLocation(Vector v1, Vector v2) {
        return new Vector(
                Math.min(v1.getBlockX(), v2.getBlockX()),
                Math.min(v1.getBlockY(), v2.getBlockY()),
                Math.min(v1.getBlockZ(), v2.getBlockZ())
        );
    }
    
    private static Vector maximalLocation(Vector v1, Vector v2) {
        return new Vector(
                Math.max(v1.getBlockX(), v2.getBlockX()),
                Math.max(v1.getBlockY(), v2.getBlockY()),
                Math.max(v1.getBlockZ(), v2.getBlockZ())
        );
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void expand(int x, int y, int z) {
        if (locked) return;
        
    }
    
    public void finalize() {
        this.locked = true;
    }

}
