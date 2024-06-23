package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

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
	
	static Material[] breakdownMaterials = {
			// Adding stained glass
			Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS,
			Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS,
			Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS,
			Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
			Material.LIGHT_GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS,
			Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
			Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS,
			Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS,
			// Adding different wools
			Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
			Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
			Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
			Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
			Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL,
			Material.BLACK_WOOL,
			// Other materials
			Material.STONE, Material.DIRT,
			Material.COBBLESTONE, Material.OAK_PLANKS, Material.SPRUCE_PLANKS,
			Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS,
			Material.DARK_OAK_PLANKS, Material.OAK_LOG, Material.SPRUCE_LOG,
			Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG,
			Material.DARK_OAK_LOG, Material.SAND, Material.RED_SAND,
			Material.GRAVEL, Material.GOLD_ORE, Material.IRON_ORE,
			Material.COAL_ORE, Material.NETHER_GOLD_ORE, Material.OAK_LEAVES,
			Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
			Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.SPONGE,
			Material.GLASS, Material.LAPIS_ORE, Material.LAPIS_BLOCK,
			Material.DISPENSER, Material.SANDSTONE, Material.NOTE_BLOCK,
	};
	
	public void placeBreakdown(Location where) {
		int beginIndex = 0;
		for (var cluster : clusters) {
			beginIndex = cluster.placeBreakdown(where, beginIndex);
		}
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
