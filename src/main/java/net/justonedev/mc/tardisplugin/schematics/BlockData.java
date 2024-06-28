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
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockData {

    // Save attributes whose values can be saved in a single byte.
    
    // Up to 31 Attributes
    private static final byte ATTRIBUTE_ID_WATERLOGGED = 1;  // Boolean
    private static final byte ATTRIBUTE_ID_DIRECTIONAL = 2;  // Direction (Blockface)
    private static final byte ATTRIBUTE_ID_AGE = 3;          // Int
    private static final byte ATTRIBUTE_ID_ATTACHED = 4;     // Boolean
    private static final byte ATTRIBUTE_ID_FACE_ATTACHABLE = 5;  // Enum with 3 values
    private static final byte ATTRIBUTE_ID_HANGING = 6;      // Boolean
    private static final byte ATTRIBUTE_ID_LEVEL = 7;        // Integer
    private static final byte ATTRIBUTE_ID_POWERED = 8;      // Boolean
    private static final byte ATTRIBUTE_ID_RAIL = 9;         // Enum with 10 values
    private static final byte ATTRIBUTE_ID_LIT = 10;         // Boolean
    private static final byte ATTRIBUTE_ID_ORIENTATION = 11;  // Axis enum: X,Y,Z
    private static final byte ATTRIBUTE_ID_ROTATION = 12;    // Blockface enum
    private static final byte ATTRIBUTE_ID_BISECTED_HALF = 13;     // Enum with 2 values
    private static final byte ATTRIBUTE_ID_STAIRS = 14;     // Enum with 2 values
    private static final byte ATTRIBUTE_ID_SLABS = 15;     // Enum with 2 values
    // => 4 bit required to attribute declaration
    
    private static final Map<Byte, BlockFace> IMPORT_BLOCKFACES = new HashMap<>();
    private static final Map<BlockFace, Byte> EXPORT_BLOCKFACES = new HashMap<>();
    private static final Map<Byte, FaceAttachable.AttachedFace> IMPORT_ATTACHED_FACE = new HashMap<>();
    private static final Map<FaceAttachable.AttachedFace, Byte> EXPORT_ATTACHED_FACE = new HashMap<>();
    private static final Map<Byte, Rail.Shape> IMPORT_RAIL_DIRECTION = new HashMap<>();
    private static final Map<Rail.Shape, Byte> EXPORT_RAIL_DIRECTION = new HashMap<>();
    private static final Map<Byte, Axis> IMPORT_AXIS = new HashMap<>();
    private static final Map<Axis, Byte> EXPORT_AXIS = new HashMap<>();
    private static final Map<Byte, Bisected.Half> IMPORT_HALVES = new HashMap<>();
    private static final Map<Bisected.Half, Byte> EXPORT_HALVES = new HashMap<>();
    private static final Map<Byte, Stairs.Shape> IMPORT_STAIRS = new HashMap<>();
    private static final Map<Stairs.Shape, Byte> EXPORT_STAIRS = new HashMap<>();
    private static final Map<Byte, Slab.Type> IMPORT_SLAB = new HashMap<>();
    private static final Map<Slab.Type, Byte> EXPORT_SLAB = new HashMap<>();
    
    private static HashMap<Byte, Class<? extends org.bukkit.block.data.BlockData>> BlockDataInterfaceMap;
    private static HashMap<Byte, String> BlockDataSetterMethods;
    
    public static void init() {
        // Explicit because what if the order changes in values()? then everything is mapped wrong for old models
        IMPORT_BLOCKFACES.put((byte) 1, BlockFace.SELF);
        IMPORT_BLOCKFACES.put((byte) 2, BlockFace.DOWN);
        IMPORT_BLOCKFACES.put((byte) 3, BlockFace.UP);
        IMPORT_BLOCKFACES.put((byte) 4, BlockFace.NORTH);
        IMPORT_BLOCKFACES.put((byte) 5, BlockFace.NORTH_NORTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 6, BlockFace.NORTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 7, BlockFace.EAST_NORTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 8, BlockFace.EAST);
        IMPORT_BLOCKFACES.put((byte) 9, BlockFace.EAST_SOUTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 10, BlockFace.SOUTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 11, BlockFace.SOUTH_SOUTH_EAST);
        IMPORT_BLOCKFACES.put((byte) 12, BlockFace.SOUTH);
        IMPORT_BLOCKFACES.put((byte) 13, BlockFace.SOUTH_SOUTH_WEST);
        IMPORT_BLOCKFACES.put((byte) 14, BlockFace.SOUTH_WEST);
        IMPORT_BLOCKFACES.put((byte) 15, BlockFace.WEST_SOUTH_WEST);
        IMPORT_BLOCKFACES.put((byte) 16, BlockFace.WEST);
        IMPORT_BLOCKFACES.put((byte) 17, BlockFace.WEST_NORTH_WEST);
        IMPORT_BLOCKFACES.put((byte) 18, BlockFace.NORTH_WEST);
        IMPORT_BLOCKFACES.put((byte) 19, BlockFace.NORTH_NORTH_WEST);
        
        IMPORT_ATTACHED_FACE.put((byte) 1, FaceAttachable.AttachedFace.WALL);
        IMPORT_ATTACHED_FACE.put((byte) 2, FaceAttachable.AttachedFace.CEILING);
        IMPORT_ATTACHED_FACE.put((byte) 3, FaceAttachable.AttachedFace.FLOOR);
        
        IMPORT_RAIL_DIRECTION.put((byte) 1, Rail.Shape.NORTH_WEST);
        IMPORT_RAIL_DIRECTION.put((byte) 2, Rail.Shape.NORTH_EAST);
        IMPORT_RAIL_DIRECTION.put((byte) 3, Rail.Shape.EAST_WEST);
        IMPORT_RAIL_DIRECTION.put((byte) 4, Rail.Shape.SOUTH_EAST);
        IMPORT_RAIL_DIRECTION.put((byte) 5, Rail.Shape.SOUTH_WEST);
        IMPORT_RAIL_DIRECTION.put((byte) 6, Rail.Shape.ASCENDING_NORTH);
        IMPORT_RAIL_DIRECTION.put((byte) 7, Rail.Shape.ASCENDING_EAST);
        IMPORT_RAIL_DIRECTION.put((byte) 8, Rail.Shape.ASCENDING_SOUTH);
        IMPORT_RAIL_DIRECTION.put((byte) 9, Rail.Shape.ASCENDING_WEST);
        
        IMPORT_AXIS.put((byte) 1, Axis.X);
        IMPORT_AXIS.put((byte) 2, Axis.Y);
        IMPORT_AXIS.put((byte) 3, Axis.Z);
        
        IMPORT_HALVES.put((byte) 1, Bisected.Half.BOTTOM);
        IMPORT_HALVES.put((byte) 2, Bisected.Half.TOP);

        IMPORT_STAIRS.put((byte) 1, Stairs.Shape.INNER_LEFT);
        IMPORT_STAIRS.put((byte) 2, Stairs.Shape.INNER_RIGHT);
        IMPORT_STAIRS.put((byte) 3, Stairs.Shape.OUTER_LEFT);
        IMPORT_STAIRS.put((byte) 4, Stairs.Shape.OUTER_RIGHT);

        IMPORT_SLAB.put((byte) 1, Slab.Type.BOTTOM);
        IMPORT_SLAB.put((byte) 2, Slab.Type.TOP);
        IMPORT_SLAB.put((byte) 3, Slab.Type.DOUBLE);
        
        // We know the mapping 1-to-1 and onto
        for (byte key : IMPORT_BLOCKFACES.keySet()) {
            EXPORT_BLOCKFACES.put(IMPORT_BLOCKFACES.get(key), key);
        }
        for (byte key : IMPORT_ATTACHED_FACE.keySet()) {
            EXPORT_ATTACHED_FACE.put(IMPORT_ATTACHED_FACE.get(key), key);
        }
        for (byte key : IMPORT_RAIL_DIRECTION.keySet()) {
            EXPORT_RAIL_DIRECTION.put(IMPORT_RAIL_DIRECTION.get(key), key);
        }
        for (byte key : IMPORT_AXIS.keySet()) {
            EXPORT_AXIS.put(IMPORT_AXIS.get(key), key);
        }
        for (byte key : IMPORT_HALVES.keySet()) {
            EXPORT_HALVES.put(IMPORT_HALVES.get(key), key);
        }
        for (byte key : IMPORT_STAIRS.keySet()) {
            EXPORT_STAIRS.put(IMPORT_STAIRS.get(key), key);
        }
        for (byte key : IMPORT_SLAB.keySet()) {
            EXPORT_SLAB.put(IMPORT_SLAB.get(key), key);
        }
        
        BlockDataInterfaceMap = new HashMap<>();
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_WATERLOGGED, org.bukkit.block.data.Waterlogged.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_DIRECTIONAL, org.bukkit.block.data.Directional.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_AGE, org.bukkit.block.data.Ageable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ATTACHED, org.bukkit.block.data.Attachable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_FACE_ATTACHABLE, org.bukkit.block.data.FaceAttachable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_HANGING, org.bukkit.block.data.Hangable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_LEVEL, org.bukkit.block.data.Levelled.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_POWERED, org.bukkit.block.data.Powerable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_RAIL, org.bukkit.block.data.Rail.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_LIT, org.bukkit.block.data.Lightable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ORIENTATION, org.bukkit.block.data.Orientable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_ROTATION, org.bukkit.block.data.Rotatable.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_BISECTED_HALF, org.bukkit.block.data.Bisected.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_STAIRS, org.bukkit.block.data.type.Stairs.class);
        BlockDataInterfaceMap.put(ATTRIBUTE_ID_SLABS, org.bukkit.block.data.type.Slab.class);

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
        BlockDataSetterMethods.put(ATTRIBUTE_ID_STAIRS, "setShape");
        BlockDataSetterMethods.put(ATTRIBUTE_ID_SLABS, "setType");
    }
    
    final Vector location;
    final Material material;
    HashMap<Byte, Byte> Attributes;
    
    public BlockData(Block block, Location offsetLocation) {
        this.location = new Vector(block.getLocation().getBlockX() - offsetLocation.getBlockX(), block.getLocation().getBlockY() - offsetLocation.getBlockY(), block.getLocation().getBlockZ() - offsetLocation.getBlockZ());
        this.material = block.getType();
        loadAttributesFrom(block);
    }
    
    public BlockData(Material material, Vector location, Map<Byte, Byte> attributes) {
        this.material = material;
        this.location = location.clone();
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData(Material material, int x, int y, int z, Map<Byte, Byte> attributes) {
        this.material = material;
        this.location = new Vector(x, y, z);
        Attributes = new HashMap<>();
        Attributes.putAll(attributes);
    }
    
    public BlockData copy(Vector location) {
        return new BlockData(material, location.clone(), Attributes);
    }
    
    HashMap<Byte, Method> cachedDataSetters = null;
    void cacheBlockDataSetters(Block exampleBlock) {
        cachedDataSetters = new HashMap<>();
        for (var attribute : Attributes.entrySet()) {
            try {
                var data = exampleBlock.getBlockData();
                var attributeValue = getRealAttributeValue(attribute.getKey(), attribute.getValue());
                if (attributeValue == null) throw new NoSuchMethodException();  // Go to error block
                
                Method setter = data.getClass().getMethod(BlockDataSetterMethods.get(attribute.getKey()), classOfType(attributeValue));
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
    
    private static Class<?> classOfType(Object o) {
        var _class = o.getClass();
        if (_class.equals(Integer.class)) {
            return int.class;
        } else if (_class.equals(Boolean.class)) {
            return boolean.class;
        } else if (_class.equals(Byte.class)) {
            return byte.class;
        } else if (_class.equals(Short.class)) {
            return short.class;
        } else if (_class.equals(Long.class)) {
            return long.class;
        } else {
            return _class;
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
                block.setBlockData(data);
                block.getState().update(false);
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
    
    private Object getRealAttributeValue(byte key, byte value) {
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
            case ATTRIBUTE_ID_SLABS:
                return IMPORT_SLAB.get(value);
            case ATTRIBUTE_ID_STAIRS:
                return IMPORT_STAIRS.get(value);
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
        final var msg = "Provided BlockData instance does not match the expected class.";
        Bukkit.getLogger().warning(msg);
        throw new IllegalArgumentException(msg);
    }
    
    private void loadAttributesFrom(Block block) {
        Attributes = new HashMap<>();
        org.bukkit.block.data.BlockData data = block.getBlockData();
        if(data instanceof Directional) {
            Attributes.put(ATTRIBUTE_ID_DIRECTIONAL, EXPORT_BLOCKFACES.get(((Directional)data).getFacing()));
        }
        if(data instanceof Ageable) {
            Attributes.put(ATTRIBUTE_ID_AGE, (byte) ((byte) (((Ageable)data).getAge() + 1) & 0xFF));    // age 0 is illegal because it's indistinguishable from no value at all
        }
        if(data instanceof Attachable) {
            Attributes.put(ATTRIBUTE_ID_ATTACHED, boolValue(((Attachable)data).isAttached()));
        }
        if(data instanceof FaceAttachable) {
            Attributes.put(ATTRIBUTE_ID_FACE_ATTACHABLE, EXPORT_ATTACHED_FACE.get(((FaceAttachable)data).getAttachedFace()));
        }
        if(data instanceof Hangable) {
            Attributes.put(ATTRIBUTE_ID_HANGING, boolValue(((Hangable)data).isHanging()));
        }
        if(data instanceof Levelled) {
            Attributes.put(ATTRIBUTE_ID_LEVEL, (byte) ((byte) ((Levelled)data).getLevel() + 1));    // level 0 is illegal because it's indistinguishable from no value at all
        }
        if(data instanceof Powerable) {
            Attributes.put(ATTRIBUTE_ID_POWERED, boolValue(((Powerable)data).isPowered()));
        }
        if(data instanceof Rail) {
            Attributes.put(ATTRIBUTE_ID_RAIL, EXPORT_RAIL_DIRECTION.get(((Rail)data).getShape()));
        }
        if(data instanceof Waterlogged) {
            Attributes.put(ATTRIBUTE_ID_WATERLOGGED, boolValue(((Waterlogged)data).isWaterlogged()));
        }
        if(data instanceof Lightable) {
            Attributes.put(ATTRIBUTE_ID_LIT, boolValue(((Lightable)data).isLit()));
        }
        if(data instanceof Orientable) {
            Attributes.put(ATTRIBUTE_ID_ORIENTATION, EXPORT_AXIS.get(((Orientable)data).getAxis()));
        }
        if(data instanceof Rotatable) {
            Attributes.put(ATTRIBUTE_ID_ROTATION, EXPORT_BLOCKFACES.get(((Rotatable)data).getRotation()));
        }
        if(data instanceof Bisected) {
            Attributes.put(ATTRIBUTE_ID_BISECTED_HALF, EXPORT_HALVES.get(((Bisected)data).getHalf()));
        }
        if(data instanceof Stairs) {
            Attributes.put(ATTRIBUTE_ID_STAIRS, EXPORT_STAIRS.get(((Stairs)data).getShape()));
        }
        if(data instanceof Slab) {
            Attributes.put(ATTRIBUTE_ID_SLABS, EXPORT_SLAB.get(((Slab)data).getType()));
        }
    }
    
    /**
     * 2 = True
     * 1 = False
     * @param bool the boolean
     * @return 1 or 2
     */
    private byte boolValue(boolean bool) {
        return bool ? (byte) 2 : (byte) 1;
    }
    private boolean boolValue(byte i) {
        return i == 2;
    }
    
    @Override
    public boolean equals(Object o) {
        System.out.println("0: " + getClass() + " other: " + o.getClass());
        if (this == o) return true;
        if (o == null) return false;
        System.out.println("1: " + getClass() + " other: " + o.getClass());
        if (getClass() != o.getClass()) {
            System.out.println("2");
            if (o instanceof Vector) {
                System.out.println("Comparing if vector: " + location + " equals " + o + " : " + Objects.equals(this.location, (Vector) o));
                return Objects.equals(this.location, (Vector) o);
            }
            return false;
        }
        BlockData blockData = (BlockData) o;
        if (Attributes.size() != ((BlockData) o).Attributes.size()) return false;
        for (var entry : Attributes.entrySet()) {
            if (blockData.Attributes.containsKey(entry.getKey())) return false;
            if (!Objects.equals(blockData.Attributes.get(entry.getKey()), entry.getValue())) return false;
        }
        return Objects.equals(location, blockData.location) && material == blockData.material;
    }
    
    public boolean isDataSame(BlockData blockData) {
        if (this == blockData) return true;
        if (blockData == null || getClass() != blockData.getClass()) return false;
        if (Attributes.size() != blockData.Attributes.size()) return false;
        for (var entry : Attributes.entrySet()) {
            if (!blockData.Attributes.containsKey(entry.getKey())) return false;
            if (!Objects.equals(blockData.Attributes.get(entry.getKey()), entry.getValue())) return false;
        }
        return material == blockData.material;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(location, material, Attributes);
    }
}
