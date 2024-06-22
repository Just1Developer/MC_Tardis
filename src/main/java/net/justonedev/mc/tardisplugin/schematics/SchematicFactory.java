package net.justonedev.mc.tardisplugin.schematics;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchematicFactory {

	final String schematicName;
	List<Cluster> clusters;
	final boolean captureAir;
	
	public SchematicFactory(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir) {
		Map<Material, List<BlockData>> blockData;
		Map<Material, List<StructureCorner>> structureCorners;
		Map<Material, Set<Quader>> allQuaders;
		
		this.schematicName = schematicName;
		this.captureAir = captureAir;
		if ((_startPoint.getWorld() != null && !_startPoint.getWorld().equals(_endPoint.getWorld())) || (_endPoint.getWorld() != null && !_endPoint.getWorld().equals(_startPoint.getWorld()))) {
			Bukkit.getLogger().severe("Error while trying to create schematic: Start and Endpoint are not in the same world. Will not be creating a schematic");
			return;
		}
		Location minLocation = new Location(
				_startPoint.getWorld(),
				Math.min(_startPoint.getBlockX(), _endPoint.getBlockX()),
				Math.min(_startPoint.getBlockY(), _endPoint.getBlockY()),
				Math.min(_startPoint.getBlockZ(), _endPoint.getBlockZ())
		);
		Vector bounds = new Vector(
				Math.max(_startPoint.getBlockX(), _endPoint.getBlockX()) - minLocation.getBlockX() + 1,
				Math.max(_startPoint.getBlockY(), _endPoint.getBlockY()) - minLocation.getBlockY() + 1,
				Math.max(_startPoint.getBlockZ(), _endPoint.getBlockZ()) - minLocation.getBlockZ() + 1
		);
		Bukkit.broadcastMessage(String.format("Min Location: (%d, %d, %d)", minLocation.getBlockX(), minLocation.getBlockY(), minLocation.getBlockZ()));
		Bukkit.broadcastMessage(String.format("Bounds: %d, %d, %d", bounds.getBlockX(), bounds.getBlockY(), bounds.getBlockZ()));
		int amt = 0;
		blockData = scanEnvironment(minLocation, bounds);
		for (var list : blockData.values()) amt += list.size();
		Bukkit.broadcastMessage("Distinct materials: " + blockData.size());
		Bukkit.broadcastMessage("Blocks: " + amt);
		
		structureCorners = findCorners(blockData);
		amt = 0;
		for (var list : structureCorners.values()) amt += list.size();
		Bukkit.broadcastMessage("Corners: " + amt);
		
		allQuaders = findAllQuaders(blockData, structureCorners);
		amt = 0;
		for (var list : allQuaders.values()) amt += list.size();
		Bukkit.broadcastMessage("All Quaders: " + amt);
		
		for (var list : blockData.values()) amt += list.size();
		// Make all same quaders into clusters
		clusters = new ArrayList<>();
		for (var quaders : allQuaders.values()) {
			clusters.add(new Cluster(quaders));
		}
		
		Bukkit.broadcastMessage("Clusters: " + clusters.size());
	}
	
	public void writeToFile() {
		File schemFile = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/", schematicName + ".schem");
		if (schemFile.exists()) {
			Bukkit.getLogger().severe(String.format("Could not write to file %s.schem: File already exists.", schematicName));
			return;
		}
		File dir = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/");
		if (!dir.exists()) if (!dir.mkdirs()) {
			Bukkit.getLogger().severe("Could not create schematics folder.");
			return;
		}
		try {
			schemFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(schemFile);
			for (Cluster cluster : clusters) {
				var bytes = cluster.encode();
				Bukkit.broadcastMessage("bytes: " + bytes.length + ": " + bytes);
				fos.write(bytes);
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			Bukkit.getLogger().severe(String.format("An error occured while trying to create schematic file %s.schem", schematicName));
			e.printStackTrace();
			return;
		}
	}
	
	private Map<Material, List<BlockData>> scanEnvironment(Location minLocation, Vector bounds) {
		Map<Material, List<BlockData>> blockData = new HashMap<>();
		Location writeHead = minLocation.clone();
		for (int x = 0; x < bounds.getBlockX(); ++x, writeHead.add(1, 0, 0)) {
			for (int y = 0; y < bounds.getBlockY(); ++y, writeHead.add(0, 1, 0)) {
				for (int z = 0; z < bounds.getBlockZ(); ++z, writeHead.add(0, 0, 1)) {
					Block b = writeHead.getBlock();
					Bukkit.broadcastMessage(String.format("§e[%d, %d, %d] Reading block @ (%d, %d, %d), type: %s", x, y, z, b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ(), b.getType()));
					if (b.getType() == Material.AIR && !captureAir) continue;
					
					if (blockData.containsKey(b.getType())) {
						blockData.get(b.getType()).add(new BlockData(b));
					} else {
						List<BlockData> list = new ArrayList<>();
						list.add(new BlockData(b));
						blockData.put(b.getType(), list);
					}
				}
				writeHead.setZ(minLocation.getBlockZ());
			}
			writeHead.setY(minLocation.getBlockY());
		}
		return blockData;
	}
	
	private static final Vector UP = new Vector(0, 1, 0), DOWN = new Vector(0, -1, 0), NORTH = new Vector(1, 0, 0), EAST = new Vector(0, 0, 1), SOUTH = new Vector(-1, 0, 0), WEST = new Vector(0, 0, -1);
	
	private static boolean doesEqual(Location loc, Vector vector) {
		return loc.getBlockX() == vector.getBlockX() && loc.getBlockY() == vector.getBlockY()  && loc.getBlockZ() == vector.getBlockZ();
	}
	
	private Map<Material, List<StructureCorner>> findCorners(Map<Material, List<BlockData>> blockData) {
		Map<Material, List<StructureCorner>> structureCorners = new HashMap<>();
		
		// Go through all the blocks.
		for (Material mat : blockData.keySet()) {
			// set probably has better access time than our List
			Set<Vector> seenLocations = new HashSet<>();
			for (var data : blockData.get(mat)) {
				seenLocations.add(data.location);
			}
			for (var data : blockData.get(mat)) {
				// We have a block. To be a corner, the block needs to have max. 1 neighbor on each axis.
				// In total, we are looking for six neighbors.
				boolean n = seenLocations.contains(data.location.clone().add(NORTH)),
						e = seenLocations.contains(data.location.clone().add(EAST)),
						s = seenLocations.contains(data.location.clone().add(SOUTH)),
						w = seenLocations.contains(data.location.clone().add(WEST)),
						u = seenLocations.contains(data.location.clone().add(UP)),
						d = seenLocations.contains(data.location.clone().add(DOWN));
				int xAxis = (n ? 1 : 0) + (s ? 1 : 0);
				int yAxis = (u ? 1 : 0) + (d ? 1 : 0);
				int zAxis = (e ? 1 : 0) + (w ? 1 : 0);
				
				if (xAxis < 2 && yAxis < 2 && zAxis < 2) {
					List<Vector> axis = new ArrayList<>();
					if (n) axis.add(NORTH.clone());
					else if (s) axis.add(SOUTH.clone());
					if (e) axis.add(EAST.clone());
					else if (w) axis.add(WEST.clone());
					if (u) axis.add(UP.clone());
					else if (d) axis.add(DOWN.clone());
					if (structureCorners.containsKey(data.material)) {
						structureCorners.get(data.material).add(new StructureCorner(data, axis));
					} else {
						List<StructureCorner> list = new ArrayList<>();
						list.add(new StructureCorner(data, axis));
						structureCorners.put(data.material, list);
					}
				}
			}
		}
		return structureCorners;
	}
	
	private Map<Material, Set<Quader>> findAllQuaders(Map<Material, List<BlockData>> blockData, Map<Material, List<StructureCorner>> structureCorners) {
		Map<Material, Set<Quader>> allQuaders = new HashMap<>();
		// From every corner, just walk.
		// Go through all the blocks.
		for (Material mat : blockData.keySet()) {
			// set probably has better access time than our List
			Set<Vector> blockLocations = new HashSet<>();
			for (var data : blockData.get(mat)) {
				blockLocations.add(data.location);
			}
			
			
			/*
			Set<Vector> cornerLocations = new HashSet<>();
			for (var data : structureCorners.get(mat)) {
				cornerLocations.add(data.blockData.location);
			}*/
			
			
			for (var corner : structureCorners.get(mat)) {
				while (!corner.startingAxis.isEmpty()) {
					//Quader quader = new Quader();
					
					Vector start = corner.blockData.location.clone(), end = corner.blockData.location.clone();
					
					Vector currentAxis = corner.startingAxis.get(0);
					corner.startingAxis.remove(currentAxis);
					Set<Vector> expanded = new HashSet<>();
					
					while (currentAxis != null) {
						final boolean negativeExpansion = isNegativeExpansion(currentAxis);
						// Todo If we encounter a corner, perhaps make sure to remove the OPPOSITE direction as starting point
						while (true) {
							// Todo check for errors
							var blockRow = getNextBlockRow(start, end, currentAxis);
							boolean _break = false;
							for (Vector block : blockRow) {
								if (!blockLocations.contains(block)) {
									// Exploration finished, can't explore further
									_break = true;
									break;
								}
							}
							if (_break) break;
							
							if (negativeExpansion) start.add(currentAxis);
							else end.add(currentAxis);
						}
						
						// Now choose next axis
						expanded.add(currentAxis);
						currentAxis = null;
						for (Vector visitable : corner.explorableAxis) {
							if (expanded.contains(visitable)) continue;
							currentAxis = visitable;
						}
					}
					// Now we've expanded all we can
					
					// Some print debug code:
					Bukkit.broadcastMessage(String.format("§cWill be constructing quader from: start: (%d, %d, %d) to end: (%d, %d, %d)", start.getBlockX(), start.getBlockY(), start.getBlockZ(), end.getBlockX(), end.getBlockY(), end.getBlockZ()));
					if (start.getBlockX() == -336 || start.getBlockZ() == -1101) {
						// This is a failure in the example im testing. The quader isn't fully expanding like it's supposed to
						Bukkit.broadcastMessage("§eExplorable Axis:");
						for (Vector visitable : corner.explorableAxis) {
							Bukkit.broadcastMessage(String.format("Axis: (%d, %d, %d)", visitable.getBlockX(), visitable.getBlockY(), visitable.getBlockZ()));
						}
						Bukkit.broadcastMessage("§eExplored Axis:");
						for (Vector visitable : expanded) {
							Bukkit.broadcastMessage(String.format("Axis: (%d, %d, %d)", visitable.getBlockX(), visitable.getBlockY(), visitable.getBlockZ()));
						}
					}
					
					// Let's build a quader
					Quader quader = new Quader(corner.blockData, start, end);
					if (allQuaders.containsKey(corner.blockData.material)) {
						allQuaders.get(corner.blockData.material).add(quader);
					} else {
						Set<Quader> set = new HashSet<>();
						set.add(quader);
						allQuaders.put(corner.blockData.material, set);
					}
				}
			}
		}
		return allQuaders;
	}
	
	private static boolean isNegativeExpansion(Vector vector) {
		return vector.getBlockX() < 0 || vector.getBlockY() < 0 || vector.getBlockZ() < 0;
	}
	
	// Todo check for errors
	private static Set<Vector> getNextBlockRow(Vector start, Vector end, Vector expansionDirection) {
		// Get direction. It is guaranteed that only one coordinate is != 0 at a time.
		int fromX, toX, fromY, toY, fromZ, toZ;
		if (expansionDirection.getBlockX() != 0) {
			// X direction
			// Which front?
			if (expansionDirection.getBlockX() < 0) {
				// Negative. Get X of startPos
				fromX = start.getBlockX() + expansionDirection.getBlockX();
				toX = fromX;
			} else {
				fromX = end.getBlockX() + expansionDirection.getBlockX();
				toX = fromX;
			}
			fromY = Math.min(start.getBlockY(), end.getBlockY());
			toY = Math.max(start.getBlockY(), end.getBlockY());
			fromZ = Math.min(start.getBlockZ(), end.getBlockZ());
			toZ = Math.max(start.getBlockZ(), end.getBlockZ());
		} else if (expansionDirection.getBlockY() != 0) {
			// X direction
			// Which front?
			if (expansionDirection.getBlockY() < 0) {
				// Negative. Get X of startPos
				fromY = start.getBlockY() + expansionDirection.getBlockY();
				toY = fromY;
			} else {
				fromY = end.getBlockY() + expansionDirection.getBlockY();
				toY = fromY;
			}
			fromX = Math.min(start.getBlockX(), end.getBlockX());
			toX = Math.max(start.getBlockX(), end.getBlockX());
			fromZ = Math.min(start.getBlockZ(), end.getBlockZ());
			toZ = Math.max(start.getBlockZ(), end.getBlockZ());
		} else {
			// X direction
			// Which front?
			if (expansionDirection.getBlockZ() < 0) {
				// Negative. Get X of startPos
				fromZ = start.getBlockZ() + expansionDirection.getBlockZ();
				toZ = fromZ;
			} else {
				fromZ = end.getBlockZ() + expansionDirection.getBlockZ();
				toZ = fromZ;
			}
			fromX = Math.min(start.getBlockX(), end.getBlockX());
			toX = Math.max(start.getBlockX(), end.getBlockX());
			fromY = Math.min(start.getBlockY(), end.getBlockY());
			toY = Math.max(start.getBlockY(), end.getBlockY());
		}
		
		Set<Vector> nextBlocks = new HashSet<>();
		for (int x = fromX; x <= toX; ++x) {
			for (int y = fromY; y <= toY; ++y) {
				for (int z = fromZ; z <= toZ; ++z) {
					nextBlocks.add(new Vector(x, y, z));
				}
			}
		}
		return nextBlocks;
	}

}
