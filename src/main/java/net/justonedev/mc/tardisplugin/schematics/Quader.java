package net.justonedev.mc.tardisplugin.schematics;

public class Quader {

    final BlockData quaderData;
    QuaderDimensions quaderDimensions;

    public Quader(BlockData blockData, QuaderDimensions dimensions, byte quaderOrientation) {
        quaderData = blockData; // I think reference is fine since it's read-only everywhere
        this.quaderDimensions = dimensions.copy(quaderOrientation);
    }

}
