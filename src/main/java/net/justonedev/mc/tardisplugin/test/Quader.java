package net.justonedev.mc.tardisplugin.test;

import java.util.Objects;

public class Quader {

    private boolean locked;
    final BlockData quaderData;
    QuaderDimensions quaderDimensions;

    public Quader(BlockData blockData, QuaderDimensions dimensions, byte quaderOrientation) {
        this.quaderData = blockData; // I think reference is fine since it's read-only everywhere
        this.quaderDimensions = dimensions.copy(quaderOrientation);
        this.locked = true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quader quader = (Quader) o;
        return Objects.equals(quaderData, quader.quaderData) && Objects.equals(quaderDimensions, quader.quaderDimensions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quaderData, quaderDimensions);
    }
    
    @Override
    public String toString() {
        return String.format("{Quader: bounds: {Val1: %d, Val2: %d, Val3: %d, Orientation: %d}, location: {x: %d, y: %d, z: %d}, data: {material: %s, ...}}", quaderDimensions.VALUE1, quaderDimensions.VALUE2, quaderDimensions.VALUE3, quaderDimensions.ORIENTATION_KEY, quaderData.location.getBlockX(), quaderData.location.getBlockY(), quaderData.location.getBlockZ(), quaderData.material);
    }
}
