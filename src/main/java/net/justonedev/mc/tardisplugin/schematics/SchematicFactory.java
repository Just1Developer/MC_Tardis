package net.justonedev.mc.tardisplugin.schematics;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SchematicFactory {

	private static final int NANO_TO_MILLI_TIME = 1000000;
	boolean USE_LOOPED_SEARCH = true;
	boolean LOOPED_SYNC_DEBUG_MODE = true;

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
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData;
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners;
		ConcurrentHashMap<Material, Set<Quader>> allQuaders;
		
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

		/*
		// Todo remove
		Bukkit.getScheduler().scheduleSyncDelayedTask(TardisPlugin.singleton, () -> {
			Bukkit.getServer().shutdown();
		}, 100);//*/
		
		if (async) {
			Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scanning environment...");
			oldtime = System.nanoTime();
			blockData = scanEnvironmentAsync2(minLocation, bounds);
			time = System.nanoTime(); Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] Scan completed in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Finding corners..."); oldtime = time;

			int runs = 0;
			allQuaders = new ConcurrentHashMap<>();
			do {
				if (LOOPED_SYNC_DEBUG_MODE) {
					structureCorners = findCornersNonAsync2(blockData);
				} else {
					structureCorners = findCornersAsync(blockData);
				}
				time = System.nanoTime();
				Bukkit.getLogger().info("[SchematicCreator 4 " + schematicName + "] [Run " + (++runs) + "] Found all corners in " + ((time - oldtime) / NANO_TO_MILLI_TIME) + " ms. Creating quaders...");
				oldtime = time;
				if (USE_LOOPED_SEARCH) {
					if (LOOPED_SYNC_DEBUG_MODE && false) {
						findAllQuadersNonAsync2(allQuaders, blockData, structureCorners);
					} else {
						findAllQuadersAsync2(allQuaders, blockData, structureCorners);
					}
				} else {
					allQuaders = findAllQuadersAsync(blockData, structureCorners);
				}
			} while (!blockData.isEmpty() && USE_LOOPED_SEARCH);
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

	public static void createSchematicAsync(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir) {
		createSchematicAsync(schematicName, _startPoint, _endPoint, captureAir, null);
	}
	/**
	 * Recommended for larger structures
	 * @param schematicName
	 * @param _startPoint
	 * @param _endPoint
	 * @param captureAir
	 */
	public static void createSchematicAsync(String schematicName, Location _startPoint, Location _endPoint, boolean captureAir, Player callback) {
		new Thread(() -> {
			long time = System.nanoTime();
			Bukkit.getLogger().info("Starting new Thread for creation of schematic " + schematicName);
			SchematicFactory schem = new SchematicFactory(schematicName, _startPoint, _endPoint, captureAir);
			schem.writeToFile(callback);
			Bukkit.getLogger().info("Total time: " + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms");
		}).start();
	}
	
	public void writeToFile() {
		writeToFile(true, null);
	}
	public void writeToFile(Player callback) {
		writeToFile(true, callback);
	}
	public void writeToFile(boolean overrideFile) {
		writeToFile(overrideFile, null);
	}
	public void writeToFile(boolean overrideFile, Player callback) {
		File schemFile = new File(TardisPlugin.singleton.getDataFolder() + "/schematics/", schematicName + Schematic.FILE_ENDING);
		if (schemFile.exists()) {
			if (!overrideFile) {
				String result = String.format("Could not write to file %s.schem: File already exists.", schematicName);
				Bukkit.getLogger().severe(result);
				if (callback != null) callback.sendMessage(result);
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
			String result1 = "[SchematicCreator 4 " + schematicName + "] Complete (" + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms).";
			String result2 = "Created schematic " + schemFile.getName() + " with " + byteCount + " bytes";
			Bukkit.getLogger().info(result1);
			Bukkit.getLogger().info(result2);
			if (callback != null) {
				callback.sendMessage("§2" + result1);
				callback.sendMessage("§e" + result2);
			}
		} catch (IOException e) {
			Bukkit.getLogger().severe(String.format("An error occured while trying to create schematic file %s.schem", schematicName));
			e.printStackTrace();
			return;
		}
	}
	
	private ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> scanEnvironment(Location minLocation, Vector bounds) {
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData = new ConcurrentHashMap<>();
		Location writeHead = minLocation.clone();
		for (int x = 0; x < bounds.getBlockX(); ++x, writeHead.add(1, 0, 0)) {
			for (int y = 0; y < bounds.getBlockY(); ++y, writeHead.add(0, 1, 0)) {
				for (int z = 0; z < bounds.getBlockZ(); ++z, writeHead.add(0, 0, 1)) {
					Block b = writeHead.getBlock();
					if (b.getType() == Material.AIR && !captureAir) continue;
					
					if (blockData.containsKey(b.getType())) {
						blockData.get(b.getType()).add(new BlockData(b, minLocation));
					} else {
						ConcurrentLinkedDeque<BlockData> list = new ConcurrentLinkedDeque<>();
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
	
	private ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> scanEnvironmentAsync2(Location minLocation, Vector bounds) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		// Concurrent HashMap to handle concurrent modifications
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> concurrentBlockData = new ConcurrentHashMap<>();
		
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
						
						concurrentBlockData.computeIfAbsent(b.getType(), k -> new ConcurrentLinkedDeque<>())
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
		
		return new ConcurrentHashMap<>(concurrentBlockData);
	}
	
	private static final Vector UP = new Vector(0, 1, 0), DOWN = new Vector(0, -1, 0), NORTH = new Vector(1, 0, 0), EAST = new Vector(0, 0, 1), SOUTH = new Vector(-1, 0, 0), WEST = new Vector(0, 0, -1);
	
	private static boolean doesEqual(Location loc, Vector vector) {
		return loc.getBlockX() == vector.getBlockX() && loc.getBlockY() == vector.getBlockY()  && loc.getBlockZ() == vector.getBlockZ();
	}
	
	private static final boolean ALLOW_EDGES_AS_CORNERS = false;
	private ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> findCorners(ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData) {
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners = new ConcurrentHashMap<>();
		
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
				
				if (xAxis < 2 && yAxis < 2 && zAxis < 2 || (ALLOW_EDGES_AS_CORNERS && (xAxis == 2 && yAxis < 2 && zAxis < 2 || xAxis < 2 && yAxis == 2 && zAxis < 2 || xAxis < 2 && yAxis < 2 && zAxis == 2))) {
					StructureCorner corner = new StructureCorner(data, n, s, e, w, u, d);
					if (structureCorners.containsKey(data.material)) {
						structureCorners.get(data.material).add(corner);
					} else {
						ConcurrentLinkedDeque<StructureCorner> list = new ConcurrentLinkedDeque<>();
						list.add(corner);
						structureCorners.put(data.material, list);
					}
				}
			}
		}
		return structureCorners;
	}
	
	long l = 0;
	// Force concurrent data types everywhere
	private ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> findCornersAsync(ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// A map to store future results
		Map<Material, Future<ConcurrentLinkedDeque<StructureCorner>>> futureResults = new ConcurrentHashMap<>();
		
		// Submit tasks for each material to the executor service
		for (Material mat : blockData.keySet()) {
			futureResults.put(mat, executor.submit(() -> {
				final long cur = l; l++;
				//System.out.println(cur + " >> 0");
				//long time = System.nanoTime();
				ConcurrentLinkedDeque<StructureCorner> corners = new ConcurrentLinkedDeque<>();
				ConcurrentHashMap<Integer, Set<Vector>> seenLocationsMap = new ConcurrentHashMap<>();
				//System.out.println(cur + " >> 1");
				for (var data : blockData.get(mat)) {
					int hash = data.hashCode2();
					if (seenLocationsMap.containsKey(hash)) {
						seenLocationsMap.get(hash).add(data.location);
					} else {
						Set<Vector> set = new HashSet<>();
						set.add(data.location);
						seenLocationsMap.put(hash, set);
					}
				}
				//System.out.println(cur + " >> 2");
				for (var data : blockData.get(mat)) {
					//System.out.println(cur + " >> 3");
					// set probably has better access time than our List
					var seenLocations = seenLocationsMap.getOrDefault(data.hashCode2(), null);
					if (seenLocations == null) continue;
					//System.out.println(cur + " >> 4");
					
					/*
					for (var _data : blockData.get(mat)) {
						if (data.isDataSame(_data)) seenLocations.add(_data.location);
					}*/
					
					// We have a block. To be a corner, the block needs to have max. 1 neighbor on each axis.
					// In total, we are looking for six neighbors.
					boolean n = seenLocations.contains(data.location.clone().add(NORTH));
					boolean s = seenLocations.contains(data.location.clone().add(SOUTH));
					if (n && s) continue;
					boolean e = seenLocations.contains(data.location.clone().add(EAST));
					boolean w = seenLocations.contains(data.location.clone().add(WEST));
					if (e && w) continue;
					boolean u = seenLocations.contains(data.location.clone().add(UP));
					boolean d = seenLocations.contains(data.location.clone().add(DOWN));
					if (u && d) continue;
					//System.out.println(cur + " >> 5");
					
					corners.add(new StructureCorner(data, n, s, e, w, u, d));
					//System.out.println(cur + " >> 6");
					
					
					/*
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
					}* /
					if (xAxis < 2 && yAxis < 2 && zAxis < 2 || (ALLOW_EDGES_AS_CORNERS && (xAxis == 2 && yAxis < 2 && zAxis < 2 || xAxis < 2 && yAxis == 2 && zAxis < 2 || xAxis < 2 && yAxis < 2 && zAxis == 2))) {
						corners.add(new StructureCorner(data, n, s, e, w, u, d));
					}
					*/
				}
				//Bukkit.broadcastMessage("Found " + corners.size() + " Corners for Material " + mat + " in " + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms");
				return corners;
			}));
		}
		
		// Shutdown executor and await termination of all tasks
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("An error occurred while waiting for corners to terminate:");
			e.printStackTrace();
		}
		
		// Collect results from futures
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners = new ConcurrentHashMap<>();
		for (Map.Entry<Material, Future<ConcurrentLinkedDeque<StructureCorner>>> entry : futureResults.entrySet()) {
			try {
				structureCorners.put(entry.getKey(), entry.getValue().get());
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				System.err.println("An error occurred while collecting corner futures:");
				e.printStackTrace();
			}
		}
		
		return structureCorners;
	}
	
	
	
	// Force concurrent data types everywhere
	private ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> findCornersNonAsync2(ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData) {
		ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners = new ConcurrentHashMap<>();
		
		// Submit tasks for each material to the executor service
		for (Material mat : blockData.keySet()) {
				ConcurrentLinkedDeque<StructureCorner> corners = new ConcurrentLinkedDeque<>();
				ConcurrentHashMap<Integer, Set<Vector>> seenLocationsMap = new ConcurrentHashMap<>();
				//System.out.println(cur + " >> 1");
				for (var data : blockData.get(mat)) {
					int hash = data.hashCode2();
					if (seenLocationsMap.containsKey(hash)) {
						seenLocationsMap.get(hash).add(data.location);
					} else {
						Set<Vector> set = new HashSet<>();
						set.add(data.location);
						seenLocationsMap.put(hash, set);
					}
				}
				for (var data : blockData.get(mat)) {
					// set probably has better access time than our List
					var seenLocations = seenLocationsMap.getOrDefault(data.hashCode2(), null);
					if (seenLocations == null) continue;
					
					// We have a block. To be a corner, the block needs to have max. 1 neighbor on each axis.
					// In total, we are looking for six neighbors.
					boolean n = seenLocations.contains(data.location.clone().add(NORTH));
					boolean s = seenLocations.contains(data.location.clone().add(SOUTH));
					if (n && s) continue;
					boolean e = seenLocations.contains(data.location.clone().add(EAST));
					boolean w = seenLocations.contains(data.location.clone().add(WEST));
					if (e && w) continue;
					boolean u = seenLocations.contains(data.location.clone().add(UP));
					boolean d = seenLocations.contains(data.location.clone().add(DOWN));
					if (u && d) continue;
					
					corners.add(new StructureCorner(data, n, s, e, w, u, d));
				}
				//Bukkit.broadcastMessage("Found " + corners.size() + " Corners for Material " + mat + " in " + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms");
			structureCorners.put(mat, corners);
		}
		
		return structureCorners;
	}
	
	private ConcurrentHashMap<Material, Set<Quader>> findAllQuaders(ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData, ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners) {
		ConcurrentHashMap<Material, Set<Quader>> allQuaders = new ConcurrentHashMap<>();
		// From every corner, just walk.
		// Go through all the blocks.
		for (Material mat : blockData.keySet()) {
			
			var corners = structureCorners.get(mat);
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
	
	private ConcurrentHashMap<Material, Set<Quader>> findAllQuadersAsync(ConcurrentHashMap<Material, ConcurrentLinkedDeque<BlockData>> blockData, ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Map<Material, Future<Set<Quader>>> futures = new HashMap<>();
		
		for (Material mat : blockData.keySet()) {
			final Material material = mat;
			// Submit a task for each material to process in parallel
			futures.put(material, executor.submit(() -> {
				Set<Quader> quaders = new HashSet<>();
				var corners = structureCorners.get(material);
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
		ConcurrentHashMap<Material, Set<Quader>> allQuaders = new ConcurrentHashMap<>();
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

	/**
	 * Careful, modifies the blockdata and allQuaders maps.
	 * @param blockData
	 * @param structureCorners
	 * @return
	 */
	private void findAllQuadersAsync2(Map<Material, Set<Quader>> allQuaders, Map<Material, ConcurrentLinkedDeque<BlockData>> blockData, ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners) {
		// Create a thread pool based on the number of available processors
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Map<Material, Future<Pair<Set<Vector>, Set<Quader>>>> futures = new ConcurrentHashMap<>();

		for (Material mat : blockData.keySet()) {
			final Material material = mat;
			// Submit a task for each material to process in parallel
			futures.put(material, executor.submit(() -> {
				try {
					Set<Quader> quaders = new HashSet<>();
					ConcurrentLinkedDeque<StructureCorner> corners = structureCorners.get(material);
					Set<Vector> processedBlocks = new HashSet<>();
					
					// Todo Limit to maybe 50 corners per task
					
					if (corners == null) {
						Bukkit.getLogger().warning("List for material " + material + " was null, not copying.");
						return new Pair<>(processedBlocks, quaders);
					}
					long time = System.nanoTime();
					
					Map<Integer, Set<Vector>> sameDataCorners = new ConcurrentHashMap<>();
					for (var data : blockData.get(material)) {
						int hash = data.hashCode2();
						var set = sameDataCorners.getOrDefault(hash, new HashSet<>());
						set.add(data.location);
						sameDataCorners.put(hash, set);
					}
					
					for (var corner : corners) {
						var blockLocations = sameDataCorners.getOrDefault(corner.blockData.hashCode2(), new HashSet<>());
						processedBlocks.addAll(processCorner2(corner, blockLocations, quaders));
					}
					//Bukkit.broadcastMessage("Found " + quaders.size() + " Quaders for Material " + mat + " in " + ((System.nanoTime() - time) / NANO_TO_MILLI_TIME) + " ms");
					return new Pair<>(processedBlocks, quaders);
				} catch (Exception e) {
					System.out.println(">>>>>>>>> ERROR: ");
					e.printStackTrace();
					return new Pair<>(new HashSet<>(), new HashSet<>());
				}
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
		futures.forEach((material, future) -> {
			try {
				var pair = future.get();
				var list = allQuaders.getOrDefault(material, new HashSet<>());
				list.addAll(pair.value2);
				allQuaders.put(material, list);
				if (blockData.containsKey(material)) {
					// Remove all does not work unless we override BlockData.hashCode() to match only location
					blockData.get(material).removeIf(blockDataItem -> pair.value1.contains(blockDataItem.location));
					if (blockData.get(material).isEmpty()) blockData.remove(material);
				}
			} catch (InterruptedException | ExecutionException e) {
				Bukkit.getLogger().warning("Failed to compute quaders for " + material);
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
	}

	/**
	 * Careful, modifies the blockdata and allQuaders maps.
	 * @param blockData
	 * @param structureCorners
	 * @return
	 */
	private void findAllQuadersNonAsync2(Map<Material, Set<Quader>> allQuaders, Map<Material, ConcurrentLinkedDeque<BlockData>> blockData, ConcurrentHashMap<Material, ConcurrentLinkedDeque<StructureCorner>> structureCorners) {
		// Create a thread pool based on the number of available processors

		for (Material mat : blockData.keySet()) {
			final Material material = mat;
			// Submit a task for each material to process in parallel
			Set<Quader> quaders = new HashSet<>();
			ConcurrentLinkedDeque<StructureCorner> corners = structureCorners.get(material);
			Set<Vector> processedBlocks = new HashSet<>();
			
			// Todo Limit to maybe 50 corners per task
			
			if (corners == null) {
				Bukkit.getLogger().warning("List for material " + material + " was null, not copying.");
				continue;
			}
			
			Map<Integer, Set<Vector>> sameDataCorners = new ConcurrentHashMap<>();
			for (var data : blockData.get(material)) {
				int hash = data.hashCode2();
				var set = sameDataCorners.getOrDefault(hash, new HashSet<>());
				set.add(data.location);
				sameDataCorners.put(hash, set);
			}
			
			for (var corner : corners) {
				var blockLocations = sameDataCorners.getOrDefault(corner.blockData.hashCode2(), new HashSet<>());
				processedBlocks.addAll(processCorner2(corner, blockLocations, quaders));
			}
			var pair = new Pair<>(processedBlocks, quaders);
			var list = allQuaders.getOrDefault(material, new HashSet<>());
			list.addAll(pair.value2);
			allQuaders.put(material, list);
			if (blockData.containsKey(material)) {
				// Remove all does not work unless we override BlockData.hashCode() to match only location
				blockData.get(material).removeIf(blockDataItem -> pair.value1.contains(blockDataItem.location));
				if (blockData.get(material).isEmpty()) blockData.remove(material);
			}
		}
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

	private Set<Vector> processCorner2(StructureCorner corner, Set<Vector> blockLocations, Set<Quader> quaders) {
		Set<Vector> processedBlocks = new HashSet<>();
		if (corner.explorerPaths.isEmpty()) {
			// Single block, just build one (1) quader
			Quader quader = new Quader(corner.blockData, corner.blockData.location, corner.blockData.location);
			quaders.add(quader);
			processedBlocks.add(corner.blockData.location.clone());
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

						processedBlocks.addAll(blockRow);

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
		return processedBlocks;
	}
	
	//endregion
	
}
