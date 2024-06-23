package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Schematic {
	
	public static String FILE_ENDING = ".tschem";
	List<Cluster> clusters;
	
	public Schematic(File saveFile) {
		clusters = new ArrayList<>();
		if (!saveFile.exists()) {
			Bukkit.getLogger().warning("Couldn't find file " + saveFile.getAbsolutePath());
			return;
		}
		readFile(saveFile);
	}
	
	public void placeInWorld(Location where) {
		for (var cluster : clusters) cluster.placeInWorld(where);
		Bukkit.getLogger().info(String.format("Built Schematic at Location (%s, %d, %d, %d)",
				where.getWorld() == null ? "unknown" : where.getWorld().getName(),
				where.getBlockX(),
				where.getBlockY(),
				where.getBlockZ()));
	}
	
	private void readFile(File file) {
		List<Byte> readResults = new ArrayList<>();
		try (FileInputStream reader = new FileInputStream(file)) {
			int currentByte;
			boolean lastWasFF = false;  // Flag to check if the last byte was 0xFF
			
			// Read bytes until EOF
			while ((currentByte = reader.read()) != -1) {	// -1 = EOF = File end
				byte b = (byte) currentByte;  // Cast to byte
				readResults.add(b);
				
				// Check if current and last byte are 0xFF
				if (b == (byte) 0xFF) {
					if (lastWasFF) {
						StringBuilder resultB = new StringBuilder();
						for (Byte result : readResults) {
							resultB.append(result).append("  ");
						}
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
			StringBuilder resultB = new StringBuilder();
			for (Byte result : readResults) {
				resultB.append(result).append("  ");
			}
		} catch (IOException e) {
			Bukkit.getLogger().severe("An error occured when reading schematic file " + file.getName());
		}
	}
	
}
