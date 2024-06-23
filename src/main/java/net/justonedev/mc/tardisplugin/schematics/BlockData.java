package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
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
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    
    private static HashMap<Integer, Class<? extends org.bukkit.block.data.BlockData>> BlockDataInterfaceMap;
    private static HashMap<Integer, String> BlockDataSetterMethods;
    
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
        
        BlockDataInterfaceMap = new HashMap<>();
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_WATERLOGGED, Waterlogged.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_DIRECTIONAL, Directional.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_AGE, Ageable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ATTACHED, Attachable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_FACE_ATTACHABLE, FaceAttachable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_HANGING, Hangable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_LEVEL, Levelled.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_POWERED, Powerable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_RAIL, Rail.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_LIT, Lightable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ORIENTATION, Orientable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ROTATION, Rotatable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_BISECTED_HALF, Bisected.class);
        
        BlockDataSetterMethods = new HashMap<>();
        BlockDataSetterMethods.put(ATTRIBUTE_ID_WATERLOGGED, "setWaterlogged");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_DIRECTIONAL, "setFacing");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_AGE, "setAge");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_ATTACHED, "setAttached");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_FACE_ATTACHABLE, "setAttachedFace");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_HANGING, "setHanging");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_LEVEL, "setLevel");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_POWERED, "setPowered");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_RAIL, "setShape");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_LIT, "setLit");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_ORIENTATION, "setAxis");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_ROTATION, "setRotation");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_BISECTED_HALF, "setHalf");
    }
    
    final Vector location;
    final Material material;
    HashMap<Integer, Integer> Attributes;
    
    public BlockData(Block block) {
        this.location = new Vector(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
        this.material = block.getType();
        loadAttributesFrom(block);
    }
    
    public BlockData(Material material, Location location, Map<Integer, Integer> attributes) {
        this.material = material;
        this.location = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData(Material material, Vector location, Map<Integer, Integer> attributes) {
        this.material = material;
        this.location = location.clone();
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData(Material material, Map<Integer, Integer> attributes) {
        this.material = material;
        this.location = new Vector(0, 0, 0);
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData(Material material, int x, int y, int z, Map<Integer, Integer> attributes) {
        this.material = material;
        this.location = new Vector(x, y, z);
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData copy(Vector location) {
        return new BlockData(material, location.clone(), Attributes);
    }
    
    HashMap<Integer, Method> cachedDataSetters = null;
    void cacheBlockDataSetters(Block exampleBlock) {
        cachedDataSetters = new HashMap<>();
        for (var attribute : Attributes.entrySet()) {
            try {
                var data = castBlockData(BlockDataInterfaceMap.get(attribute.getKey()), exampleBlock.getBlockData());
                var attributeValue = getRealAttributeValue(attribute.getKey(), attribute.getValue());
                if (attributeValue == null) throw new NoSuchMethodException();  // Go to error block
                
                Method setter = data.getClass().getMethod(BlockDataSetterMethods.get(attribute.getKey()), attributeValue.getClass());
                cachedDataSetters.put(attribute.getKey(), setter);
            } catch (NoSuchMethodException | IllegalArgumentException e) {
                Bukkit.getLogger().warning(String.format("[Quader setter caching: %s @ (%s, %d, %d, %d)] Error getting attribute setter: %s",
                        material.name(),
                        exampleBlock.getLocation().getWorld() == null ? "null" : exampleBlock.getLocation().getWorld().getName(),
                        exampleBlock.getLocation().getBlockX(),
                        exampleBlock.getLocation().getBlockY(),
                        exampleBlock.getLocation().getBlockZ(),
                        e.getMessage()));
            }
        }
    }
    
    void applyAllAttributesTo(Block block) {
        if (cachedDataSetters == null) {
            Bukkit.getLogger().severe(String.format("Will not be setting attributes for block @ (%s, %d, %d, %d) because setter methods aren't cached. Tell your developer to invoke BlockData.cacheBlockDataSetters() for the quader first.",
                    block.getLocation().getWorld() == null ? "null" : block.getLocation().getWorld().getName(),
                    block.getLocation().getBlockX(),
                    block.getLocation().getBlockY(),
                    block.getLocation().getBlockZ()));
            return;
        }
        
        for (var attribute : Attributes.entrySet()) {
            try {
                var setter = cachedDataSetters.getOrDefault(attribute.getKey(), null);
                if (setter == null) throw new NoSuchMethodException();
                
                var data = castBlockData(BlockDataInterfaceMap.get(attribute.getKey()), block.getBlockData());
                var attributeValue = getRealAttributeValue(attribute.getKey(), attribute.getValue());
                setter.invoke(data, attributeValue);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                Bukkit.getLogger().warning(String.format("[Quader application: %s @ (%s, %d, %d, %d)] Error applying attribute: %s",
                        material.name(),
                        block.getLocation().getWorld() == null ? "null" : block.getLocation().getWorld().getName(),
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ(),
                        e.getMessage()));
            }
        }
    }
    
    private Object getRealAttributeValue(int key, int value) {
        switch (key) {
            case ATTRIBUTE_ID_DIRECTIONAL:
            case ATTRIBUTE_ID_ROTATION:
                return IMPORT_BLOCKFACES.get(value);
            case ATTRIBUTE_ID_AGE:
            case ATTRIBUTE_ID_LEVEL:
                return value - 1;
            case ATTRIBUTE_ID_ATTACHED:
            case ATTRIBUTE_ID_HANGING:
            case ATTRIBUTE_ID_POWERED:
            case ATTRIBUTE_ID_WATERLOGGED:
            case ATTRIBUTE_ID_LIT:
                return boolValue(value);
            case ATTRIBUTE_ID_BISECTED_HALF:
                return IMPORT_HALVES.get(value);
            case ATTRIBUTE_ID_FACE_ATTACHABLE:
                return IMPORT_ATTACHED_FACE.get(value);
            case ATTRIBUTE_ID_RAIL:
                return IMPORT_RAIL_DIRECTION.get(value);
            case ATTRIBUTE_ID_ORIENTATION:
                return IMPORT_AXIS.get(value);
            default:
                return null;
        }
    }
    
    private <T extends org.bukkit.block.data.BlockData> T castBlockData(Class<T> clazz, org.bukkit.block.data.BlockData blockData) throws IllegalArgumentException {
        if (clazz.isInstance(blockData)) {
            return clazz.cast(blockData);
        }
        throw new IllegalArgumentException("Provided BlockData instance does not match the expected class.");
    }
    
    private void loadAttributesFrom(Block block) {
        Attributes = new HashMap<>();
        org.bukkit.block.data.BlockData data = block.getBlockData();
        if(block.getBlockData() instanceof Directional) {
            Attributes.put(ATTRIBUTE_ID_DIRECTIONAL, EXPORT_BLOCKFACES.get(((Directional)data).getFacing()));
        }
        if(block.getBlockData() instanceof Ageable) {
            Attributes.put(ATTRIBUTE_ID_AGE, ((Ageable)data).getAge() + 1);    // age 0 is illegal because it's indistinguishable from no value at all
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockData blockData = (BlockData) o;
        return Objects.equals(location, blockData.location) && material == blockData.material && Objects.equals(Attributes, blockData.Attributes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(location, material, Attributes);
    }
}
