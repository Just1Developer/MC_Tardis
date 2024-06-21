package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Attachable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;

import java.util.HashMap;
import java.util.Map;

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
    private static final int ATTRIBUTE_ID_ORIENTATION = 11;  // Axis enum: X,Y,Z
    private static final int ATTRIBUTE_ID_ROTATION = 12;    // Blockface enum
    private static final int ATTRIBUTE_ID_BISECTED_HALF = 13;     // Enum with 2 values
    // => 4 bit required to attribute declaration
    
    private static final Map<Integer, BlockFace> IMPORT_BLOCKFACES = new HashMap<>();
    private static final Map<BlockFace, Integer> EXPORT_BLOCKFACES = new HashMap<>();
    private static final Map<Integer, FaceAttachable.AttachedFace> IMPORT_ATTACHED_FACE = new HashMap<>();
    private static final Map<FaceAttachable.AttachedFace, Integer> EXPORT_ATTACHED_FACE = new HashMap<>();
    private static final Map<Integer, Rail.Shape> IMPORT_RAIL_DIRECTION = new HashMap<>();
    private static final Map<Rail.Shape, Integer> EXPORT_RAIL_DIRECTION = new HashMap<>();
    private static final Map<Integer, Axis> IMPORT_AXIS = new HashMap<>();
    private static final Map<Axis, Integer> EXPORT_AXIS = new HashMap<>();
    private static final Map<Integer, Bisected.Half> IMPORT_HALVES = new HashMap<>();
    private static final Map<Bisected.Half, Integer> EXPORT_HALVES = new HashMap<>();
    
    public static void init() {
        // Explicit because what if the order changes in values()? then everything is mapped wrong for old models
        IMPORT_BLOCKFACES.put(1, BlockFace.SELF);
        IMPORT_BLOCKFACES.put(2, BlockFace.DOWN);
        IMPORT_BLOCKFACES.put(3, BlockFace.UP);
        IMPORT_BLOCKFACES.put(4, BlockFace.NORTH);
        IMPORT_BLOCKFACES.put(5, BlockFace.NORTH_NORTH_EAST);
        IMPORT_BLOCKFACES.put(6, BlockFace.NORTH_EAST);
        IMPORT_BLOCKFACES.put(7, BlockFace.EAST_NORTH_EAST);
        IMPORT_BLOCKFACES.put(8, BlockFace.EAST);
        IMPORT_BLOCKFACES.put(9, BlockFace.EAST_SOUTH_EAST);
        IMPORT_BLOCKFACES.put(10, BlockFace.SOUTH_EAST);
        IMPORT_BLOCKFACES.put(11, BlockFace.SOUTH_SOUTH_EAST);
        IMPORT_BLOCKFACES.put(12, BlockFace.SOUTH);
        IMPORT_BLOCKFACES.put(13, BlockFace.SOUTH_SOUTH_WEST);
        IMPORT_BLOCKFACES.put(14, BlockFace.SOUTH_WEST);
        IMPORT_BLOCKFACES.put(15, BlockFace.WEST_SOUTH_WEST);
        IMPORT_BLOCKFACES.put(16, BlockFace.WEST);
        IMPORT_BLOCKFACES.put(17, BlockFace.WEST_NORTH_WEST);
        IMPORT_BLOCKFACES.put(18, BlockFace.NORTH_WEST);
        IMPORT_BLOCKFACES.put(19, BlockFace.NORTH_NORTH_WEST);
        
        IMPORT_ATTACHED_FACE.put(1, FaceAttachable.AttachedFace.WALL);
        IMPORT_ATTACHED_FACE.put(2, FaceAttachable.AttachedFace.CEILING);
        IMPORT_ATTACHED_FACE.put(3, FaceAttachable.AttachedFace.FLOOR);
        
        IMPORT_RAIL_DIRECTION.put(1, Rail.Shape.NORTH_WEST);
        IMPORT_RAIL_DIRECTION.put(2, Rail.Shape.NORTH_EAST);
        IMPORT_RAIL_DIRECTION.put(3, Rail.Shape.EAST_WEST);
        IMPORT_RAIL_DIRECTION.put(4, Rail.Shape.SOUTH_EAST);
        IMPORT_RAIL_DIRECTION.put(5, Rail.Shape.SOUTH_WEST);
        IMPORT_RAIL_DIRECTION.put(6, Rail.Shape.ASCENDING_NORTH);
        IMPORT_RAIL_DIRECTION.put(7, Rail.Shape.ASCENDING_EAST);
        IMPORT_RAIL_DIRECTION.put(8, Rail.Shape.ASCENDING_SOUTH);
        IMPORT_RAIL_DIRECTION.put(9, Rail.Shape.ASCENDING_WEST);
        
        IMPORT_AXIS.put(1, Axis.X);
        IMPORT_AXIS.put(2, Axis.Y);
        IMPORT_AXIS.put(3, Axis.Z);
        
        IMPORT_HALVES.put(1, Bisected.Half.BOTTOM);
        IMPORT_HALVES.put(2, Bisected.Half.TOP);
        
        // We know the mapping 1-to-1 and onto
        for (int key : IMPORT_BLOCKFACES.keySet()) {
            EXPORT_BLOCKFACES.put(IMPORT_BLOCKFACES.get(key), key);
        }
        for (int key : IMPORT_ATTACHED_FACE.keySet()) {
            EXPORT_ATTACHED_FACE.put(IMPORT_ATTACHED_FACE.get(key), key);
        }
        for (int key : IMPORT_RAIL_DIRECTION.keySet()) {
            EXPORT_RAIL_DIRECTION.put(IMPORT_RAIL_DIRECTION.get(key), key);
        }
        for (int key : IMPORT_AXIS.keySet()) {
            EXPORT_AXIS.put(IMPORT_AXIS.get(key), key);
        }
        for (int key : IMPORT_HALVES.keySet()) {
            EXPORT_HALVES.put(IMPORT_HALVES.get(key), key);
        }
    }
    
    final Location location;
    final Material material;
    HashMap<Integer, Integer> Attributes;
    
    public BlockData(Block block) {
        location = block.getLocation();
        material = block.getType();
        applyAttributes(block);
    }
    
    private void applyAttributes(Block block) {
        Attributes = new HashMap<>();
        org.bukkit.block.data.BlockData data = block.getBlockData();
        if(block.getBlockData() instanceof Directional) {
            Attributes.put(ATTRIBUTE_ID_DIRECTIONAL, EXPORT_BLOCKFACES.get(((Directional)data).getFacing()));
        }
        if(block.getBlockData() instanceof Ageable) {
            Attributes.put(ATTRIBUTE_ID_AGE, ((Ageable)data).getAge());
        }
        if(block.getBlockData() instanceof Attachable) {
            Attributes.put(ATTRIBUTE_ID_ATTACHED, boolValue(((Attachable)data).isAttached()));
        }
        if(block.getBlockData() instanceof Bisected) {
            Attributes.put(ATTRIBUTE_ID_BISECTED_HALF, EXPORT_HALVES.get(((Bisected)data).getHalf()));
        }
        if(block.getBlockData() instanceof FaceAttachable) {
            Attributes.put(ATTRIBUTE_ID_FACE_ATTACHABLE, EXPORT_ATTACHED_FACE.get(((FaceAttachable)data).getAttachedFace()));
        }
        if(block.getBlockData() instanceof Hangable) {
            Attributes.put(ATTRIBUTE_ID_HANGING, boolValue(((Hangable)data).isHanging()));
        }
        if(block.getBlockData() instanceof Levelled) {
            Attributes.put(ATTRIBUTE_ID_LEVEL, ((Levelled)data).getLevel() + 1);    // level 0 is illegal because it's indistinguishable from no value at all
        }
        if(block.getBlockData() instanceof Powerable) {
            Attributes.put(ATTRIBUTE_ID_POWERED, boolValue(((Powerable)data).isPowered()));
        }
        if(block.getBlockData() instanceof Rail) {
            Attributes.put(ATTRIBUTE_ID_RAIL, EXPORT_RAIL_DIRECTION.get(((Rail)data).getShape()));
        }
        if(block.getBlockData() instanceof Waterlogged) {
            Attributes.put(ATTRIBUTE_ID_WATERLOGGED, boolValue(((Waterlogged)data).isWaterlogged()));
        }
        if(block.getBlockData() instanceof Lightable) {
            Attributes.put(ATTRIBUTE_ID_LIT, boolValue(((Lightable)data).isLit()));
        }
        if(block.getBlockData() instanceof Orientable) {
            Attributes.put(ATTRIBUTE_ID_ORIENTATION, EXPORT_AXIS.get(((Orientable)data).getAxis()));
        }
        if(block.getBlockData() instanceof Rotatable) {
            Attributes.put(ATTRIBUTE_ID_ROTATION, EXPORT_BLOCKFACES.get(((Rotatable)data).getRotation()));
        }
    }
    
    private int boolValue(boolean bool) {
        return bool ? 1 : 0;
    }
    private boolean boolValue(int i) {
        return i != 0;
    }

}
