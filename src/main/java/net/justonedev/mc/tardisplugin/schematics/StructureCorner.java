package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class StructureCorner {
	
	static final Vector UP = new Vector(0, 1, 0), DOWN = new Vector(0, -1, 0), NORTH = new Vector(1, 0, 0), EAST = new Vector(0, 0, 1), SOUTH = new Vector(-1, 0, 0), WEST = new Vector(0, 0, -1);
	
	final BlockData blockData;
	final List<Vector> startingAxis;
	final List<Vector> explorableAxis;
	final List<Vector[]> explorerPaths;
	
	StructureCorner(BlockData blockData, List<Vector> axis) {
		this.blockData = blockData;
		this.startingAxis = axis;
		this.explorableAxis = new ArrayList<>();
		this.explorableAxis.addAll(axis);
		explorerPaths = new ArrayList<>();
	}
	
	StructureCorner(BlockData blockData, boolean north, boolean south, boolean east, boolean west, boolean up, boolean down) {
		this.blockData = blockData;
		this.startingAxis = new ArrayList<>();
		this.explorableAxis = new ArrayList<>();
		explorerPaths = new ArrayList<>();
		List<Vector> explorables = new ArrayList<>();
		int dimensions = 0;
		if (north) { explorables.add(NORTH); dimensions++; }
		if (south) { explorables.add(SOUTH); dimensions++; }
		if (east) { explorables.add(EAST); dimensions++; }
		if (west) { explorables.add(WEST); dimensions++; }
		if (up) { explorables.add(UP); dimensions++; }
		if (down) { explorables.add(DOWN); dimensions++; }
		
		// Create all linear combinations of all expansion directions
		
		if (dimensions == 0) return;
		
		for (int i = 0; i < explorables.size(); i++) {
			if (dimensions > 1) {
				for (int j = 0; j < explorables.size(); j++) {
					if (i == j) continue;
					if (dimensions > 2) {
						for (int k = 0; k < explorables.size(); k++) {
							if (i == k || j == k) continue;
							explorerPaths.add(new Vector[] { explorables.get(i).clone(), explorables.get(j).clone(), explorables.get(k).clone() });
						}
					} else {
						explorerPaths.add(new Vector[] { explorables.get(i).clone(), explorables.get(j).clone() });
					}
				}
			} else {
				explorerPaths.add(new Vector[] { explorables.get(i).clone() });
			}
		}
	}
	
}
