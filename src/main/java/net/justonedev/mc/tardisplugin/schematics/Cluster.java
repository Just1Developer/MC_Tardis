package net.justonedev.mc.tardisplugin.schematics;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of Quaders with the same material but different attributes.
 */
public class Cluster {

    static final int SIZE_UPPERBOUND_16_BIT = 0xFF00;
    static final int SIZE_UPPERBOUND_24_BIT = 0xFF0000;

    Material material;
    List<Quader> quaders;
    Set<Integer> existingAttributes;

    Cluster(List<Quader> quaders) {
        existingAttributes = new HashSet<>();
        this.quaders = quaders;
        for (var quader : quaders) {
            existingAttributes.addAll(quader.quaderData.Attributes.keySet());
        }
        if (!quaders.isEmpty()) material = quaders.get(0).quaderData.material;
        else material = null;
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

        boolean need24Bit = false;
        for (Quader quader : quaders) {
            // Check if any of the coordinates are >= SIZE_UPPERBOUND_16_BIT
            // If so, mark we need 24 bit
            // If any of the coordinates are >= SIZE_UPPERBOUND_24_BIT, return empty array and print error.
        }

        String materialString = material.name();
        bytes.add((byte) (materialString.length() & 0x7F | (need24Bit ? 0x80 : 0x00)));
        for (int _c = 0; _c < materialString.length(); _c++) {
            bytes.add((byte) materialString.charAt(_c));
        }

        HashMap<Vector, List<Quader>> differentShapes;

        byte[] byteArray = new byte[bytes.size()];
        int index = 0;
        for (byte b : bytes) {
            byteArray[index++] = b;
        }
        return byteArray;
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
