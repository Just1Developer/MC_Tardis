package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class StructureCorner {
	
	final BlockData blockData;
	final List<Vector> startingAxis;
	final List<Vector> explorableAxis;
	
	StructureCorner(BlockData blockData, List<Vector> axis) {
		this.blockData = blockData;
		this.startingAxis = axis;
		this.explorableAxis = new ArrayList<>();
		this.explorableAxis.addAll(axis);
	}
	
}
