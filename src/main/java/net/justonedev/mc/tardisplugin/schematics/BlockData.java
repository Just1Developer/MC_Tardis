package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Material;

import java.util.HashMap;

public class BlockData {

    // Save attributes whose values can be saved in a single byte.

    private static final int ATTRIBUTE_ID_WATERLOGGED = 1;  // Boolean
    private static final int ATTRIBUTE_ID_DIRECTIONAL = 2;  // Direction (Blockface)
    private static final int ATTRIBUTE_ID_AGE = 3;          // Int
    private static final int ATTRIBUTE_ID_ATTACHED = 4;     // Boolean
    private static final int ATTRIBUTE_ID_FACE_ATTACHABLE = 5;  // Enum with 3 values
    private static final int ATTRIBUTE_ID_HANGING = 6;      // Boolean
    private static final int ATTRIBUTE_ID_LEVEL = 7;        // Integer
    private static final int ATTRIBUTE_ID_POWERED = 8;      // Boolean
    private static final int ATTRIBUTE_ID_RAIL = 9;         // Enum with 10 values
    private static final int ATTRIBUTE_ID_LIT = 10;         // Boolean
    private static final int ATTRIBUTE_ID_ORIENTABLE = 11;  // Axis enum: X,Y,Z
    private static final int ATTRIBUTE_ID_Rotation = 12;    // Blockface enum
    // => 4 bit required to attribute declaration

    Material material;
    HashMap<Integer, Integer> Attributes = new HashMap<>();

}
