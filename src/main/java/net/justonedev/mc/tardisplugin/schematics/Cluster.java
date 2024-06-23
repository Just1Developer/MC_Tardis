package net.justonedev.mc.tardisplugin.schematics;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of Quaders with the same material but different attributes.
 */
public class Cluster {

    static final int SIZE_UPPERBOUND_16_BIT = 0xFF00;
    static final int SIZE_UPPERBOUND_MAX_16_BIT = 0xFFFF;
    static final int SIZE_UPPERBOUND_24_BIT = 0xFF0000;
    static final int SIZE_UPPERBOUND_MAX_24_BIT = 0xFFFFFF;
    static final byte TERMINATOR_BYTE = (byte) 0xFF;
    static final byte NULL_BYTE = (byte) 0xFF;
    
    static final int[] SIZES_UPPERBOUND = {
            0xFF,
            0xFF00,
            0xFF0000,
            0xFF000000
    };
    
    static final int[] SIZES_UPPERBOUND_MAX = {
            0xFF,
            0xFFFF,
            0xFFFFFF,
            0xFFFFFFFF
    };

    Material material;
    Set<Quader> quaders;
    Set<Byte> existingAttributes;

    Cluster(Set<Quader> quaders) {
        existingAttributes = new HashSet<>();
        this.quaders = quaders;
        for (var quader : quaders) {
            existingAttributes.addAll(quader.quaderData.Attributes.keySet());
        }
        if (!quaders.isEmpty()) material = this.quaders.iterator().next().quaderData.material;
        else material = null;
    }
    
    public void placeInWorld(Location anchorLocation) {
        for (var q : quaders) q.placeInWorld(anchorLocation);
    }

    // First byte: first bit gives format: 0 => 16-bit, 1 => 24-bit
    // Next 7 bit => amount of following ascii characters

    byte[] encode() {
        // 1. Byte that list how many letters (bytes) in ASCII the type will be.
        // 2. Then, the type with 1 ASCII letter per byte.
        // 3. Then, the dimensions in ascending order.
        // 4. Then, the attributes, 2 per byte.
        List<Byte> bytes = new ArrayList<>();

        if (material == null) {
            return new byte[0];
        }

        boolean boundsNeed24Bit = false;
        int coordsByteAmount = 1;
        for (Quader quader : quaders) {
            // Check if any of the coordinates are >= SIZE_UPPERBOUND_16_BIT
            // If so, mark we need 24 bit
            // If any of the coordinates are >= SIZE_UPPERBOUND_24_BIT, return empty array and print error.
            if (!boundsNeed24Bit) {
                // Check if either the highest value exceeds the 16 bit cap or the lowest value makes the first byte all 1s, which would otherwise trigger as terminator byte
                if (quader.quaderDimensions.VALUE3 >= SIZE_UPPERBOUND_MAX_16_BIT || quader.quaderDimensions.VALUE1 >= SIZE_UPPERBOUND_16_BIT) boundsNeed24Bit = true;
            }
            // Same thing for 24 bit
            if (quader.quaderDimensions.VALUE3 >= SIZE_UPPERBOUND_MAX_24_BIT || quader.quaderDimensions.VALUE1 >= SIZE_UPPERBOUND_24_BIT) {
                Bukkit.getLogger().severe("Encoding of schematic cluster failed: Quader Dimensions are too high to be encoded in 24-bit format.");
                return new byte[0];
            }
            
            // Now check the coords
            while (quader.quaderData.location.getBlockX() >= SIZES_UPPERBOUND[Math.min(coordsByteAmount - 1, SIZES_UPPERBOUND.length)]
                    || quader.quaderData.location.getBlockZ() >= SIZES_UPPERBOUND_MAX[Math.min(coordsByteAmount - 1, SIZES_UPPERBOUND_MAX.length)]) {
                // We know Y can't
                if (coordsByteAmount == 4) {
                    Bukkit.getLogger().severe("Encoding of schematic cluster failed: Quader Dimensions are too high to be encoded in 24-bit format.");
                    return new byte[0];
                }
                ++coordsByteAmount;
            }
        }

        String materialString = material.name();
        // We know names are >= length 1, so make it 1-based
        bytes.add((byte) (materialString.length() - 1));
        for (int _c = 0; _c < materialString.length(); _c++) {
            bytes.add((byte) materialString.charAt(_c));
        }
        
        // Now, build how many attributes there are and what their IDs are. We will have this byte and then ceil(attributes / 2) bytes for their IDs
        
        // This next byte is interesting. It consists of the following:
        // Bit 1: This encodes if the quader dimensions use 16 (0) or 24 (1) bit, so 2 or 3 byte.
        // Bits 2&3: These bits encode if the coordinates use 8 (0), 16 (1), 24 (2) or 32 (3) bits, so 1-4 byte.
        // Bits 4-8: The last 5 bits encode the size of the existing attributes. Since it probably won't exceed 31, we're good on this front
        bytes.add((byte) ((existingAttributes.size() & 0x1F) | (((coordsByteAmount - 1) & 0x03) << 5) | (boundsNeed24Bit ? 0x80 : 0x00)));
        // reduce variable scope
        Bukkit.broadcastMessage("§bWill be writing " + existingAttributes.size() + " attributes that should fit into " + Math.ceil(existingAttributes.size() / 2.0));
        Bukkit.broadcastMessage("§e(_PRE)List size is " + bytes.size());
        {
            byte b = 0;
            boolean secondHalf = false;
            for (int attr : existingAttributes) {
                // Reverse order
                if (secondHalf) {
                    bytes.add((byte) (b | (((byte) attr) << 4)));
                    b = 0;
                } else {
                    b = (byte) (attr & 0x0F);
                }
                secondHalf = !secondHalf;
            }
            // Remaining first half
            if (b != 0) bytes.add(b);
        }
        Bukkit.broadcastMessage("§e(POST)List size is " + bytes.size());

        HashMap<Integer, List<Quader>> differentShapes = new HashMap<>();
        for (Quader quader : quaders) {
            int hash = quader.quaderDimensions.getValueHash();
            if (differentShapes.containsKey(hash)) {
                differentShapes.get(hash).add(quader);
            } else {
                List<Quader> l = new ArrayList<>();
                l.add(quader);
                differentShapes.put(hash, l);
            }
        }
        
        for (var list : differentShapes.values()) {
            // Shapes consist of a few thing:
            // 1. The shape
            // 2. A list of first x,y,z,O where O = quader orientation, and then the list of attributes with 1 byte per attribute
            // Todo maybe sort attributes by type and collect all booleans in one byte
            // Add X, Y and Z
            QuaderDimensions dims = list.get(0).quaderDimensions;   // guaranteed to not be empty
            addNumber(dims.VALUE1, boundsNeed24Bit ? 3 : 2, bytes);       // same for all in the list
            addNumber(dims.VALUE2, boundsNeed24Bit ? 3 : 2, bytes);       // same for all in the list
            addNumber(dims.VALUE3, boundsNeed24Bit ? 3 : 2, bytes);       // same for all in the list
            // Now, add the locations and attributes of all the quaders
            for (Quader quader : list) {
                // For here, we (will) know exactly how many bytes this is
                // First add x, y, and z coordinates. All will be >=, of course
                addNumber(quader.quaderData.location.getBlockX(), coordsByteAmount, bytes);
                addNumber(quader.quaderData.location.getBlockY(), Math.min(coordsByteAmount, 2), bytes);         // Keep this between 1 and 2 bytes. 1 byte <=> everything needs 1 byte, otherwise 2 byte
                addNumber(quader.quaderData.location.getBlockZ(), coordsByteAmount, bytes);
                // Then, we add the orientation of the quader
                bytes.add(quader.quaderDimensions.ORIENTATION_KEY);
                // Then, we lock in the attributes in the exact order
				Bukkit.broadcastMessage("§e[" + material + "] Will be writing " + existingAttributes.size() + " Attributes. List size now: " + bytes.size() + " >> " + bytes);
                for (byte attr : existingAttributes) {
                    // This is some wild casting: Integer can't be cast to byte, so we cast
                    // it to int (primitive type), which in turn can be cast to byte.
                    bytes.add(quader.quaderData.Attributes.getOrDefault(attr, NULL_BYTE));
					Bukkit.broadcastMessage("§dWriting Attribute " + attr + " with value " + quader.quaderData.Attributes.getOrDefault(attr, NULL_BYTE) + " for material " + materialString);
                }
				Bukkit.broadcastMessage("§b[" + material + "] Wrote Attributes.             List size now: " + bytes.size() + " >> " + bytes);
            }
            bytes.add(TERMINATOR_BYTE);
            Bukkit.broadcastMessage("§d[" + material + "] Finished Quader shape. List size now: " + bytes.size() + " >> " + bytes);
        }
        bytes.add(TERMINATOR_BYTE);
        Bukkit.broadcastMessage("§b[" + material + "] Finished Cluster. List size now: " + bytes.size() + " >> " + bytes);
		
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
			Byte value = bytes.get(i);
			// I can't believe i have to do this for null bytes because primitive type can handle 0x00 but Object Byte can't
			byteArray[i] = value == null ? 0 : value;
		}
        return byteArray;
    }
    
    public static Cluster readFromBytes(List<Byte> bytes) {
		Bukkit.broadcastMessage("Will be reading a cluster from " + bytes.size() + " bytes");
        if (bytes.isEmpty()) return null;
        int index = 0;
        Material material;
        // Set material:
        {
            StringBuilder materialBuilder = new StringBuilder();
            int materialLength = bytes.get(index++) + 1;
            for (; index <= materialLength; ++index) {
                materialBuilder.append((char) (int) bytes.get(index));
            }
            try {
                material = Material.getMaterial(materialBuilder.toString());
            } catch (Exception e) {
                // What exception this throws I don't know right now, so catch everything (bad coding practice btw)
                Bukkit.getLogger().severe("Failed to parse material String: \"" + materialBuilder.toString() + "\"");
                return null;
            }
        }
		Bukkit.broadcastMessage("Material is " + material + ", current index is " + index + " (should be 1+length = " + (1+material.name().length()) + ")");
        
        byte numberData = bytes.get(index++);
        int attributeCount = numberData & 0x1F;
        int coordBytes = ((numberData >> 5) & 0x03) + 1;
        int boundsBytes = (numberData & 0x80) != 0 ? 3 : 2;
		
		Bukkit.broadcastMessage("§dUsing " + attributeCount + " attributes, " + coordBytes + " coordinate bytes and " + boundsBytes + " bounds bytes");
        Bukkit.broadcastMessage("§dIndex is currently " + index);
        int indexPrev_broadcast = index;
        
        List<Byte> existingAttributes = new ArrayList<>();
        {
            byte b;
            for (int count = 0; count < attributeCount; count += 2) {
                b = bytes.get(index++);
                existingAttributes.add((byte) (b & 0x0F));
                if ((b & 0xF0) != 0) existingAttributes.add((byte) ((b >> 4) & 0x0F));
            }
        }
        Bukkit.broadcastMessage("§dIndex is currently " + index + ", should be §e" + (indexPrev_broadcast + Math.ceil(attributeCount/2.0)));
        Bukkit.broadcastMessage("§dWe read " + existingAttributes.size() + " attributes that should fit into " + Math.ceil(existingAttributes.size()/2.0));
        
        Set<Quader> quaders = new HashSet<>();
        
        while (true) {
            Pair<Integer, Integer> readNumberResult;
            // Read different quader shapes here
            readNumberResult = readNumber(index, boundsBytes, bytes, true);
            index = readNumberResult.value1;
            int bounds1 = readNumberResult.value2;
            if (bounds1 == TERMINATOR_BYTE) break;
            
            readNumberResult = readNumber(index, boundsBytes, bytes, false);
            index = readNumberResult.value1;
            int bounds2 = readNumberResult.value2;
            
            readNumberResult = readNumber(index, boundsBytes, bytes, false);
            index = readNumberResult.value1;
            int bounds3 = readNumberResult.value2;
            
            QuaderDimensions dimensions = QuaderDimensions.ofSorted(bounds1, bounds2, bounds3);
            
            while (true) {
                // read single quaders with the same shape here.
                Map<Byte, Byte> attributeValues = new HashMap<>();
                // Read different quader shapes here
                readNumberResult = readNumber(index, coordBytes, bytes, true);
                index = readNumberResult.value1;
                int x = readNumberResult.value2;
                if (x == TERMINATOR_BYTE) break;
                
                readNumberResult = readNumber(index, Math.min(coordBytes, 2), bytes, false);
                index = readNumberResult.value1;
                int y = readNumberResult.value2;
                
                readNumberResult = readNumber(index, coordBytes, bytes, false);
                index = readNumberResult.value1;
                int z = readNumberResult.value2;
                
                byte boundsOR = bytes.get(index++);
                
                // Now get the attribute values
                for (int count = 0; count < attributeCount; ++count) {
					/*
					try {
						attributeValue = bytes.get(index++);
					} catch (IndexOutOfBoundsException e) {
						Bukkit.broadcastMessage("tried to load attrValue and failed. Attribute Count: " + attributeCount + ", count: " + count + ", index: " + (index-1) + ", bytes length: " + bytes.size());
						Bukkit.broadcastMessage("§bMaterial: " + material.name());
						for (var attr : existingAttributes) {
							Bukkit.broadcastMessage("§bExisting Attr: " + attr);
						}
						Bukkit.broadcastMessage("§eBytelist:");
						for (var b : bytes) {
							Bukkit.broadcastMessage("§e>> " + b.toString());
						}
						throw e;
					}
					 */
					byte attributeValue = bytes.get(index++);
					Bukkit.broadcastMessage("Read attribute " + count + " at " + (index-1) + " with value " + attributeValue);
                    if (attributeValue == 0) continue;
                    attributeValues.put(existingAttributes.get(count), attributeValue);
                }
                
                // Now we have (the bounds), the coords, the orientation and the attributes.
                // Time to create a Quader.
                quaders.add(new Quader(new BlockData(material, x, y, z, attributeValues), dimensions, boundsOR));
            }
        }
		Bukkit.broadcastMessage("§dLoaded cluster of material " + material);
        
        return new Cluster(quaders);
    }
    
    private static Pair<Integer, Integer> readNumber(int currentIndex, int bytes, List<Byte> list, boolean lookoutForTerminatorByte) {
		//if (lookoutForTerminatorByte) Bukkit.broadcastMessage("OOO I'm lookin'");
        int value = 0;
        for (int place = 0; place < bytes; ++place) {
			//Bukkit.broadcastMessage("Lookin, now getting next. index " + currentIndex + " for list of size " + list.size() + " >> " + list);
            int next = list.get(currentIndex++);
			//Bukkit.broadcastMessage("next was " + next + " ooooo");
            if (lookoutForTerminatorByte && place == 0 && next == TERMINATOR_BYTE) return new Pair<>(currentIndex, (int) TERMINATOR_BYTE);
            value |= (next) << 8 * (bytes - place - 1);
        }
        return new Pair<>(currentIndex, value);
    }
    
    private static void addNumber(int number, int bytecount, List<Byte> list) {
        if (bytecount >= 4) {
            list.add((byte) ((number & 0xFF000000) >> 24));
        }
        if (bytecount >= 3) {
            list.add((byte) ((number & 0xFF0000) >> 16));
        }
        if (bytecount >= 2) {
            list.add((byte) ((number & 0xFF00) >> 8));
        }
        list.add((byte) (number & 0xFF));
    }

    void write(String schemFileName) {
        if (!new File(TardisPlugin.singleton.getDataFolder() + "/schematics/").exists())
            if (!new File(TardisPlugin.singleton.getDataFolder() + "/schematics/").mkdirs()) return;
        File schemFile = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/" + schemFileName + ".schem");
        try {
            if (!schemFile.createNewFile()) return;
            FileOutputStream fos = new FileOutputStream(schemFile);
            fos.write(encode());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
