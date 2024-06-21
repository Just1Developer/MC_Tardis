package net.justonedev.mc.tardisplugin.schematics;

public class Quader {

    final BlockData quaderData;
    QuaderDimensions quaderDimensions;

    public Quader(BlockData blockData) {
        quaderData = blockData; // I think reference is fine since it's read-only everywhere
    }

}
