package net.justonedev.mc.tardisplugin.schematics.rotation;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public interface Rotation {
	final Rotation None = new None();
	final Rotation East = new East();
	final Rotation NorthSouth = new NorthSouth();
	final Rotation EastWest = new EastWest();
	final Rotation West = new West();
	
	void transformLocation(Location loc);
	void transformVector(Vector loc);
	
	class None implements Rotation {
		@Override
		public void transformLocation(Location loc) {	}
		public void transformVector(Vector loc) {	}
		@Override
		public String toString() {
			return "{ Rotation: None }";
		}
	}
	
	class East implements Rotation {
		@Override
		public void transformLocation(Location loc) {
			// +X => +Z
			double z = loc.getZ();
			loc.setZ(loc.getX());
			loc.setX(z);
		}
		@Override
		public void transformVector(Vector loc) {
			// +X => +Z
			double z = loc.getZ();
			loc.setZ(loc.getX());
			loc.setX(z);
		}
		@Override
		public String toString() {
			return "{ Rotation: East }";
		}
	}
	
	class West implements Rotation {
		@Override
		public void transformLocation(Location loc) {
			// +X => -Z
			double z = -loc.getZ();
			loc.setZ(-loc.getX());
			loc.setX(z);
		}
		@Override
		public void transformVector(Vector loc) {
			// +X => -Z
			double z = -loc.getZ();
			loc.setZ(-loc.getX());
			loc.setX(z);
		}
		@Override
		public String toString() {
			return "{ Rotation: West }";
		}
	}
	
	class NorthSouth implements Rotation {
		@Override
		public void transformLocation(Location loc) {
			// +X => -X
			loc.setX(-loc.getX());
		}
		@Override
		public void transformVector(Vector loc) {
			// +X => -X
			loc.setX(-loc.getX());
		}
		@Override
		public String toString() {
			return "{ Rotation: NorthSouth }";
		}
	}
	
	class EastWest implements Rotation {
		@Override
		public void transformLocation(Location loc) {
			// +X => -X
			loc.setX(-loc.getX());
		}
		@Override
		public void transformVector(Vector loc) {
			// +X => -X
			loc.setX(-loc.getX());
		}
		@Override
		public String toString() {
			return "{ Rotation: EastWest }";
		}
	}
}
