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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SchematicFactory {

	private static final int NANO_TO_MILLI_TIME = 1000000;

	private final Vector bounds;
	final String schematicName;
	List<Cluster> clusters;
	final boolean captureAir;
	
	public SchematicFactory(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir) {
		this(schematicName, _startPoint, _endPoint, captureAir, true);
	}
	public SchematicFactory(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir, boolean async) {
		this.schematicName = schematicName;
		this.captureAir = captureAir;
		Map<Material, List<BlockData>> blockData;
		Map<Material, List<StructureCorner>> structureCorners;
		Map<Material, Set<Quader>> allQuaders;
		
		Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Creating location bounds...");
		if ((_startPoint.getWorld() != null && !_startPoint.getWorld().equals(_endPoint.getWorld())) || (_endPoint.getWorld() != null && !_endPoint.getWorld().equals(_startPoint.getWorld()))) {
			Bukkit.getLogger().severe("Error while trying to create schematic: Start and Endpoint are not in the same world. Will not be creating a schematic");
			bounds = new Vector();
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
		this.bounds = bounds.clone();

		long time, oldtime;
		
		if (async) {
			Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scanning environment...");
			oldtime = System.nanoTime();
			blockData = scanEnvironmentAsync2(minLocation, bounds);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scan completed in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Finding corners..."); oldtime = time;
			structureCorners = findCornersAsync(blockData);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Found all corners in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Creating quaders..."); oldtime = time;
			allQuaders = findAllQuadersAsync(blockData, structureCorners);
			int quaderAmount = 0;
			for (var val : allQuaders.values()) quaderAmount += val.size();
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] " + quaderAmount + " Quaders created in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Making clusters..."); oldtime = time;
		} else {
			Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scanning environment...");
			oldtime = System.nanoTime();
			blockData = scanEnvironment(minLocation, bounds);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scan completed in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Finding corners..."); oldtime = time;
			structureCorners = findCorners(blockData);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Found all corners in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Creating quaders..."); oldtime = time;
			allQuaders = findAllQuaders(blockData, structureCorners);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Quaders created in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Making clusters..."); oldtime = time;
		}

		
		// Make all same quaders into clusters
		clusters = new ArrayList<>();
		for (var quaders : allQuaders.values()) {
			clusters.add(new Cluster(quaders));
		}
		time = System.nanoTime();
		Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Cluster creation complete in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms.");
	}
	
	/**
	 * Recommended for larger structures
	 * @param schematicName
	 * @param _startPoint
	 * @param _endPoint
	 * @param captureAir
	 */
	public static void createSchematicAsync(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir) {
		new Thread(() -> {
			long time = System.nanoTime();
			Bukkit.getLogger().info("Starting new Thread for creation of schematic " + schematicName);
			SchematicFactory schem = new SchematicFactory(schematicName, _startPoint, _endPoint, captureAir);
			schem.writeToFile();
			Bukkit.getLogger().info("Total time: " + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms");
		}).start();
	}
	
	public void writeToFile() {
		writeToFile(true);
	}
	public void writeToFile(boolean overrideFile) {
		File schemFile = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/", schematicName + Schematic.FILE_ENDING);
		if (schemFile.exists()) {
			if (!overrideFile) {
				Bukkit.getLogger().severe(String.format("Could not write to file %s.schem: File already exists.", schematicName));
				return;
			}
			schemFile.delete();
		}
		File dir = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/");
		if (!dir.exists()) if (!dir.mkdirs()) {
			Bukkit.getLogger().severe("Could not create schematics folder.");
			return;
		}
		try {
			schemFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(schemFile);
			int byteCount = 0;
			
			Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Begin writing clusters to file...");
			long time = System.nanoTime();
			
			for (Cluster cluster : clusters) {
				var bytes = cluster.encode();
				byteCount += bytes.length;
				fos.write(bytes);
			}
			fos.flush();
			fos.close();
			Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Complete (" + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms).");
			Bukkit.getLogger().info("Created schematic " + schemFile.getName() + " with " + byteCount + " bytes");
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
					if (b.getType() == Material.AIR && !captureAir) continue;
					
					if (blockData.containsKey(b.getType())) {
						blockData.get(b.getType()).add(new BlockData(b, minLocation));
					} else {
						List<BlockData> list = new ArrayList<>();
						list.add(new BlockData(b, minLocation));
						blockData.put(b.getType(), list);
					}
				}
				writeHead.setZ(minLocation.getBlockZ());
			}
			writeHead.setY(minLocation.getBlockY());
		}
		return blockData;
	}
	
	private Map<Material, List<BlockData>> scanEnvironmentAsync(Location minLocation, Vector bounds) {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Map<Material, ConcurrentLinkedQueue<BlockData>> concurrentBlockData = new ConcurrentHashMap<>();
		
		int maxX = bounds.getBlockX();
		int maxY = bounds.getBlockY();
		int maxZ = bounds.getBlockZ();
		
		// Determine the largest axis
		int maxDimension = Math.max(maxX, Math.max(maxY, maxZ));
		String largestAxis = maxX == maxDimension ? "X" : maxY == maxDimension ? "Y" : "Z";
		
		// Create tasks based on the largest axis
		List<Callable<Void>> tasks = new ArrayList<>();
		for (int i = 0; i < maxDimension; i++) {
			int index = i;
			tasks.add(() -> {
				Location writeHead = minLocation.clone();
				if (largestAxis.equals("X")) {
					writeHead.add(index, 0, 0);
				} else if (largestAxis.equals("Y")) {
					writeHead.setY(minLocation.getBlockY() + index);
				} else {
					writeHead.setZ(minLocation.getBlockZ() + index);
				}
				
				for (int x = 0; x < maxX; x++) {
					for (int y = 0; y < maxY; y++) {
						for (int z = 0; z < maxZ; z++) {
							if (largestAxis.equals("X")) {
								if (x == 0) continue;
								writeHead.add(1, 0, 0);
							} else if (largestAxis.equals("Y")) {
								if (y == 0) continue;
								writeHead.add(0, 1, 0);
							} else {
								if (z == 0) continue;
								writeHead.add(0, 0, 1);
							}
							
							Block b = writeHead.getBlock();
							if (b.getType() == Material.AIR) continue;
							
							concurrentBlockData.computeIfAbsent(b.getType(), k -> new ConcurrentLinkedQueue<>()).add(new BlockData(b, minLocation));
						}
						if (largestAxis.equals("Z")) writeHead.setZ(minLocation.getBlockZ());
					}
					if (largestAxis.equals("Y")) writeHead.setY(minLocation.getBlockY());
				}
				if (largestAxis.equals("X")) writeHead.setX(minLocation.getBlockX());
				return null;
			});
		}
		
		// Execute all tasks
		try {
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // handle interruptions
		}
		executor.shutdown();
		
		// Convert ConcurrentLinkedQueue to List
		Map<Material, List<BlockData>> blockData = new HashMap<>();
		concurrentBlockData.forEach((key, value) -> blockData.put(key, new ArrayList<>(value)));
		
		return blockData;
	}
	
	private Map<Material, List<BlockData>> scanEnvironmentAsync2(Location minLocation, Vector bounds) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		// Concurrent HashMap to handle concurrent modifications
		ConcurrentHashMap<Material, List<BlockData>> concurrentBlockData = new ConcurrentHashMap<>();
		
		// List to keep track of futures to ensure all tasks complete
		List<Future<Void>> futures = new ArrayList<>();
		
		// Iterate through each 'x' layer in parallel
		for (int x = 0; x < bounds.getBlockX(); x++) {
			final int currentX = x; // Effective final for use in lambda
			futures.add(executor.submit(() -> {
				Location writeHead = minLocation.clone().add(currentX, 0, 0); // Move the write head to the current layer
				for (int y = 0; y < bounds.getBlockY(); y++) {
					for (int z = 0; z < bounds.getBlockZ(); z++) {
						Block b = writeHead.clone().add(0, y, z).getBlock();
						if (b.getType() == Material.AIR && !captureAir) continue;
						
						concurrentBlockData.computeIfAbsent(b.getType(), k -> Collections.synchronizedList(new ArrayList<>()))
								.add(new BlockData(b, minLocation));
					}
				}
				return null; // Future<Void> requires a return, even if it's null
			}));
		}
		
		// Shut down executor and wait for all tasks to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		// Wait for all futures to complete (all layers scanned)
		for (Future<Void> f : futures) {
			try {
				f.get(); // This ensures any exception thrown during task execution is caught here
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		// Convert to regular HashMap to return (if needed outside concurrent context)
		
		return new HashMap<>(concurrentBlockData);
	}
	
	private static final Vector UP = new Vector(0, 1, 0), DOWN = new Vector(0, -1, 0), NORTH = new Vector(1, 0, 0), EAST = new Vector(0, 0, 1), SOUTH = new Vector(-1, 0, 0), WEST = new Vector(0, 0, -1);
	
	private static boolean doesEqual(Location loc, Vector vector) {
		return loc.getBlockX() == vector.getBlockX() && loc.getBlockY() == vector.getBlockY()  && loc.getBlockZ() == vector.getBlockZ();
	}
	
	private static final boolean ALLOW_EDGES_AS_CORNERS = true;
	private Map<Material, List<StructureCorner>> findCorners(Map<Material, List<BlockData>> blockData) {
		Map<Material, List<StructureCorner>> structureCorners = new HashMap<>();
		
		// Go through all the blocks.
		for (Material mat : blockData.keySet()) {
			for (var data : blockData.get(mat)) {
				// set probably has better access time than our List
				Set<Vector> seenLocations = new HashSet<>();
				for (var _data : blockData.get(mat)) {
					if (data.isDataSame(_data)) seenLocations.add(_data.location);
				}
				
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
					StructureCorner corner = new StructureCorner(data, n, s, e, w, u, d);
					if (structureCorners.containsKey(data.material)) {
						structureCorners.get(data.material).add(corner);
					} else {
						List<StructureCorner> list = new ArrayList<>();
						list.add(corner);
						structureCorners.put(data.material, list);
					}
				}
			}
		}
		return structureCorners;
	}
	
	private Map<Material, List<StructureCorner>> findCornersAsync(Map<Material, List<BlockData>> blockData) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// A map to store future results
		Map<Material, Future<List<StructureCorner>>> futureResults = new HashMap<>();
		
		// Submit tasks for each material to the executor service
		for (Material mat : blockData.keySet()) {
			futureResults.put(mat, executor.submit(() -> {
				List<StructureCorner> corners = new ArrayList<>();
				for (var data : blockData.get(mat)) {
					// set probably has better access time than our List
					Set<Vector> seenLocations = new HashSet<>();
					for (var _data : blockData.get(mat)) {
						if (data.isDataSame(_data)) seenLocations.add(_data.location);
					}
					
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
					
					/*
					if (xAxis < 2 && yAxis < 2 && zAxis < 2 || (ALLOW_EDGES_AS_CORNERS && (xAxis == 2 && yAxis < 2 && zAxis < 2 || xAxis < 2 && yAxis == 2 && zAxis < 2 || xAxis < 2 && yAxis < 2 && zAxis == 2))) {
						List<Vector> axis = new ArrayList<>();
						if (n) axis.add(NORTH.clone());
						if (s) axis.add(SOUTH.clone());
						if (e) axis.add(EAST.clone());
						if (w) axis.add(WEST.clone());
						if (u) axis.add(UP.clone());
						if (d) axis.add(DOWN.clone());
						corners.add(new StructureCorner(data, axis));
					}*/
					if (xAxis < 2 && yAxis < 2 && zAxis < 2) {
						corners.add(new StructureCorner(data, n, s, e, w, u, d));
					}
				}
				return corners;
			}));
		}
		
		// Shutdown executor and await termination of all tasks
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		// Collect results from futures
		Map<Material, List<StructureCorner>> structureCorners = new HashMap<>();
		for (Map.Entry<Material, Future<List<StructureCorner>>> entry : futureResults.entrySet()) {
			try {
				structureCorners.put(entry.getKey(), entry.getValue().get());
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		return structureCorners;
	}
	
	private Map<Material, Set<Quader>> findAllQuaders(Map<Material, List<BlockData>> blockData, Map<Material, List<StructureCorner>> structureCorners) {
		Map<Material, Set<Quader>> allQuaders = new HashMap<>();
		// From every corner, just walk.
		// Go through all the blocks.
		for (Material mat : blockData.keySet()) {
			
			List<StructureCorner> corners = structureCorners.get(mat);
			if (corners == null) {
				Bukkit.getLogger().warning("List for material " + mat + " was null, not copying.");
				continue;
			}
			
			for (var corner : corners) {
				// set probably has better access time than our List
				Set<Vector> blockLocations = new HashSet<>();
				for (var data : blockData.get(mat)) {
					if (data.isDataSame(corner.blockData)) blockLocations.add(data.location);
				}
				
				if (corner.explorerPaths.isEmpty()) {
					// Single block, just build one (1) quader
					Quader quader = new Quader(corner.blockData, corner.blockData.location, corner.blockData.location);
					registerNewQuader(allQuaders, corner, quader);
				} else {
					Vector start = corner.blockData.location.clone(), end = corner.blockData.location.clone();
					for (var currentExplorer : corner.explorerPaths) {
						int explorerIndex = 0;
						Vector currentAxis = currentExplorer[explorerIndex];
						
						while (currentAxis != null) {
							final boolean negativeExpansion = isNegativeExpansion(currentAxis);
							// Todo If we encounter a corner, perhaps make sure to remove the OPPOSITE direction as starting point
							while (true) {
								var blockRow = getNextBlockRow(start, end, currentAxis);
								if (blockRow.isEmpty()) break;
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
							
							explorerIndex++;
							currentAxis = explorerIndex < currentExplorer.length ? currentExplorer[explorerIndex] : null;
						}
						// Now we've expanded all we can
						
						// Let's build a quader
						Quader quader = new Quader(corner.blockData, start, end);
						registerNewQuader(allQuaders, corner, quader);
					}
				}
			}
		}
		return allQuaders;
	}
	
	private void registerNewQuader(Map<Material, Set<Quader>> allQuaders, StructureCorner corner, Quader quader) {
		if (allQuaders.containsKey(corner.blockData.material)) {
			allQuaders.get(corner.blockData.material).add(quader);
		} else {
			Set<Quader> set = new HashSet<>();
			set.add(quader);
			allQuaders.put(corner.blockData.material, set);
		}
	}
	
	private static boolean isNegativeExpansion(Vector vector) {
		return vector.getBlockX() < 0 || vector.getBlockY() < 0 || vector.getBlockZ() < 0;
	}
	
	private Set<Vector> getNextBlockRow(Vector start, Vector end, Vector expansionDirection) {
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
		
		toX++; toY++; toZ++;

		fromX = Math.max(Math.min(fromX, bounds.getBlockX()), 0);
		toX = Math.max(Math.min(toX, bounds.getBlockX()), 0);
		fromY = Math.max(Math.min(fromY, bounds.getBlockY()), 0);
		toY = Math.max(Math.min(toY, bounds.getBlockY()), 0);
		fromZ = Math.max(Math.min(fromZ, bounds.getBlockZ()), 0);
		toZ = Math.max(Math.min(toZ, bounds.getBlockZ()), 0);
		
		Set<Vector> nextBlocks = new HashSet<>();
		for (int x = fromX; x < toX; ++x) {
			for (int y = fromY; y < toY; ++y) {
				for (int z = fromZ; z < toZ; ++z) {
					nextBlocks.add(new Vector(x, y, z));
				}
			}
		}
		return nextBlocks;
	}

	
	//region Async Quader Creation
	
	private Map<Material, Set<Quader>> findAllQuadersAsync(Map<Material, List<BlockData>> blockData, Map<Material, List<StructureCorner>> structureCorners) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Map<Material, Future<Set<Quader>>> futures = new HashMap<>();
		
		for (Material mat : blockData.keySet()) {
			final Material material = mat;
			// Submit a task for each material to process in parallel
			futures.put(material, executor.submit(() -> {
				Set<Quader> quaders = new HashSet<>();
				List<StructureCorner> corners = structureCorners.get(material);
				if (corners == null) {
					Bukkit.getLogger().warning("List for material " + material + " was null, not copying.");
					return quaders;
				}
				
				for (var corner : corners) {
					Set<Vector> blockLocations = new HashSet<>();
					for (var data : blockData.get(material)) {
						if (data.isDataSame(corner.blockData)) blockLocations.add(data.location);
					}
					
					processCorner(corner, blockLocations, quaders);
				}
				return quaders;
			}));
		}
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Bukkit.getLogger().severe("Building Quaders interrupted.");
			Thread.currentThread().interrupt();
		}
		
		// Collect all results
		Map<Material, Set<Quader>> allQuaders = new HashMap<>();
		futures.forEach((material, future) -> {
			try {
				allQuaders.put(material, future.get());
			} catch (InterruptedException | ExecutionException e) {
				Bukkit.getLogger().warning("Failed to compute quaders for " + material);
				Thread.currentThread().interrupt();
			}
		});
		
		return allQuaders;
	}
	
	private void processCorner(StructureCorner corner, Set<Vector> blockLocations, Set<Quader> quaders) {
		if (corner.explorerPaths.isEmpty()) {
			// Single block, just build one (1) quader
			Quader quader = new Quader(corner.blockData, corner.blockData.location, corner.blockData.location);
			quaders.add(quader);
		} else {
			Vector start = corner.blockData.location.clone(), end = corner.blockData.location.clone();
			for (var currentExplorer : corner.explorerPaths) {
				int explorerIndex = 0;
				Vector currentAxis = currentExplorer[explorerIndex];
				
				while (currentAxis != null) {
					final boolean negativeExpansion = isNegativeExpansion(currentAxis);
					// Todo If we encounter a corner, perhaps make sure to remove the OPPOSITE direction as starting point
					while (true) {
						var blockRow = getNextBlockRow(start, end, currentAxis);
						if (blockRow.isEmpty()) break;
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
					
					explorerIndex++;
					currentAxis = explorerIndex < currentExplorer.length ? currentExplorer[explorerIndex] : null;
				}
				// Now we've expanded all we can
				
				// Let's build a quader
				Quader quader = new Quader(corner.blockData, start, end);
				quaders.add(quader);
			}
		}
	}
	
	//endregion
	
}
