package net.justonedev.mc.tardisplugin.test;

import net.justonedev.mc.tardisplugin.schematics.Pair;
import org.bukkit.Bukkit;

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
    static final int NULL_BYTE = 0xFF;
    
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
    
    String material;
    Set<Quader> quaders;
    Set<Integer> existingAttributes;

    Cluster(Set<Quader> quaders) {
        existingAttributes = new HashSet<>();
        this.quaders = quaders;
        for (var quader : quaders) {
            existingAttributes.addAll(quader.quaderData.Attributes.keySet());
        }
        if (!quaders.isEmpty()) material = this.quaders.iterator().next().quaderData.material;
        else material = null;
    }

    public static Cluster readFromBytes(List<Byte> bytes) {
        if (bytes.isEmpty()) return null;
        int index = 0;
        String material;
        // Set material:
        {
            StringBuilder materialBuilder = new StringBuilder();
            int materialLength = bytes.get(index) + 1;
            for (index = 1; index <= materialLength; ++index) {
                materialBuilder.append((char) (int) bytes.get(index));
            }
            try {
                material = materialBuilder.toString();//materialBuilder.toString().equals("SEA_LANTERN") ? Material.SEA_LANTERN : materialBuilder.toString().equals("BEDROCK") ? Material.BEDROCK : materialBuilder.toString().equals("BLUE_WOOL") ? Material.BLUE_WOOL : Material.SMOOTH_QUARTZ;
            } catch (Exception e) {
                // What exception this throws I don't know right now, so catch everything (bad coding practice btw)
                Bukkit.getLogger().severe("Failed to parse material String: \"" + materialBuilder.toString() + "\"");
                return null;
            }
        }
        
        byte numberData = bytes.get(index++);
        int attributeCount = numberData & 0x1F;
        int coordBytes = ((numberData >> 5) & 0x03) + 1;
        int boundsBytes = (numberData & 0x80) != 0 ? 3 : 2;
        
        List<Integer> existingAttributes = new ArrayList<>();
        {
            byte b;
            for (int count = 0; count < attributeCount; count += 2) {
                b = bytes.get(index++);
                existingAttributes.add(b & 0x0F);
                if ((b & 0xF0) != 0) existingAttributes.add((b >> 4) & 0x0F);
            }
        }
        
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
                Map<Integer, Integer> attributeValues = new HashMap<>();
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
                    int attributeValue = bytes.get(index++);
                    if (attributeValue == 0) continue;
                    attributeValues.put(existingAttributes.get(count), attributeValue);
                }
                
                // Now we have (the bounds), the coords, the orientation and the attributes.
                // Time to create a Quader.
                quaders.add(new Quader(new BlockData(material, x, y, z, attributeValues), dimensions, boundsOR));
            }
        }
        
        return new Cluster(quaders);
    }
    
    private static Pair<Integer, Integer> readNumber(int currentIndex, int bytes, List<Byte> list, boolean lookoutForTerminatorByte) {
        int value = 0;
        System.out.println("Attempting to read " + bytes + " bytes at index " + currentIndex + " for a list of size " + list.size());
        for (int place = 0; place < bytes; ++place) {
            int next = list.get(currentIndex++);
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

}
