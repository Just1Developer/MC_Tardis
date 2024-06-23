package net.justonedev.mc.tardisplugin.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Schematic {
	
	List<Cluster> clusters;
	
	// Dev note: {Quader: bounds: {Val1: 2, Val2: 2, Val3: 2, Orientation: 1}, location: {x: -18, y: -71, z: -99}, data: {material: SEA_LANTERN, ...}}
	
	public Schematic(File saveFile) {
		clusters = new ArrayList<>();
		if (!saveFile.exists()) {
			//Bukkit.getLogger().warning("Couldn't find file " + saveFile.getAbsolutePath());
			return;
		}
		readFile(saveFile);
	}
	
	private void readFile(File file) {
		List<Byte> readResults = new ArrayList<>();
		try (FileInputStream reader = new FileInputStream(file)) {
			int currentByte;
			boolean lastWasFF = false;  // Flag to check if the last byte was 0xFF
			
			// Read bytes until EOF
			while ((currentByte = reader.read()) != -1) {
				byte b = (byte) currentByte;  // Cast to byte
				readResults.add(b);
				
				// Check if current and last byte are 0xFF
				if (b == (byte) 0xFF) {
					if (lastWasFF) {
						clusters.add(Cluster.readFromBytes(readResults));
						readResults.clear();
						lastWasFF = false;
						continue;
					}
					lastWasFF = true;
				} else {
					lastWasFF = false;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
