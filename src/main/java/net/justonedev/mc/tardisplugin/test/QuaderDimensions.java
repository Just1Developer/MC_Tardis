package net.justonedev.mc.tardisplugin.test;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

public class QuaderDimensions {

	private static final byte ORIENT_XYZ = 1;	// Order: X, Y, Z
	private static final byte ORIENT_XZY = 2;	// Order: X, Z, Y
	private static final byte ORIENT_YXZ = 3;	// Order: Y, X, Z
	private static final byte ORIENT_YZX = 4;	// Order: Y, Z, X
	private static final byte ORIENT_ZXY = 5;	// Order: Z, X, Y
	private static final byte ORIENT_ZYX = 6;	// Order: Z, Y, X
	
	public final int VALUE1;
	public final int VALUE2;
	public final int VALUE3;
	public final byte ORIENTATION_KEY;
	
	public QuaderDimensions(Location location) {
		this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	public QuaderDimensions(Vector size) {
		this(size.getBlockX(), size.getBlockY(), size.getBlockZ());
	}
	public QuaderDimensions(int x, int y, int z) {
		// 3 Swaps maximum
		byte orient = ORIENT_XYZ;
		if (x > y) {
			int temp = y;
			y = x;
			x = temp;
			orient = getOrientationKey(orient, Axis.X, Axis.Y);
		}
		// X < Y definitely
		// X < Z?
		if (x > z) {
			int temp = z;
			z = x;
			x = temp;
			orient = getOrientationKey(orient, Axis.X, Axis.Z);
		}
		// X < Y definitely
		if (y > z) {
			int temp = z;
			z = y;
			y = temp;
			orient = getOrientationKey(orient, Axis.Y, Axis.Z);
		}
		
		VALUE1 = x;
		VALUE2 = y;
		VALUE3 = z;
		ORIENTATION_KEY = orient;
	}
	
	QuaderDimensions(int val1, int val2, int val3, byte orientation) {
		this.VALUE1 = val1;
		this.VALUE2 = val2;
		this.VALUE3 = val3;
		this.ORIENTATION_KEY = orientation;
	}
	
	static QuaderDimensions ofSorted(int val1, int val2, int val3) {
		return new QuaderDimensions(val1, val2, val3, ORIENT_XYZ);
	}
	
	private byte getOrientationKey(byte key, Axis swapped1, Axis swapped2) {
		if (swapped1 == swapped2) return key;
		
		// Smaller goes in front
		if (swapped2 == Axis.X) {
			swapped2 = swapped1;
			swapped1 = Axis.X;
		} else if (swapped2 == Axis.Y && swapped1 == Axis.Z) {
			swapped1 = Axis.Y;
			swapped2 = Axis.Z;
		}
		
		if (swapped1 == Axis.X) {
			if (swapped2 == Axis.Y) {
				switch (ORIENTATION_KEY) {
					case ORIENT_XYZ: return ORIENT_YXZ;
					case ORIENT_XZY: return ORIENT_YZX;
					case ORIENT_YXZ: return ORIENT_XYZ;
					case ORIENT_YZX: return ORIENT_XZY;
					case ORIENT_ZXY: return ORIENT_ZYX;
					case ORIENT_ZYX: return ORIENT_ZXY;
				}
			} else if (swapped2 == Axis.Z) {
				switch (ORIENTATION_KEY) {
					case ORIENT_XYZ: return ORIENT_ZYX;
					case ORIENT_XZY: return ORIENT_ZXY;
					case ORIENT_YXZ: return ORIENT_YZX;
					case ORIENT_YZX: return ORIENT_YXZ;
					case ORIENT_ZXY: return ORIENT_XZY;
					case ORIENT_ZYX: return ORIENT_XYZ;
				}
			}
		} else if(swapped1 == Axis.Y) {
			// Swapped2 = Z
			switch (ORIENTATION_KEY) {
				case ORIENT_XYZ: return ORIENT_XZY;
				case ORIENT_XZY: return ORIENT_XYZ;
				case ORIENT_YXZ: return ORIENT_ZXY;
				case ORIENT_YZX: return ORIENT_ZYX;
				case ORIENT_ZXY: return ORIENT_YXZ;
				case ORIENT_ZYX: return ORIENT_YZX;
			}
		}
		return key;
	}
	
	/**
	 * Converts the Dimensions to a vector with X, Y and Z in the right order.
	 * @return Dimensions as (X,Y,Z) vector
	 */
	Vector toVectorDimension() {
		switch (ORIENTATION_KEY) {
			case ORIENT_XYZ: return new Vector(VALUE1, VALUE2, VALUE3);
			case ORIENT_XZY: return new Vector(VALUE1, VALUE3, VALUE2);
			case ORIENT_YXZ: return new Vector(VALUE2, VALUE1, VALUE3);
			case ORIENT_YZX: return new Vector(VALUE2, VALUE3, VALUE1);
			case ORIENT_ZXY: return new Vector(VALUE3, VALUE1, VALUE2);
			case ORIENT_ZYX: return new Vector(VALUE3, VALUE2, VALUE1);
			default: return new Vector(0, 0, 0);
		}
	}
	
	int getValueHash() {
		return Objects.hash(VALUE1, VALUE2, VALUE3);
	}
	
	QuaderDimensions copy() {
		return new QuaderDimensions(VALUE1, VALUE2, VALUE3, ORIENTATION_KEY);
	}
	
	QuaderDimensions copy(byte newOrientation) {
		return new QuaderDimensions(VALUE1, VALUE2, VALUE3, newOrientation);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		QuaderDimensions that = (QuaderDimensions) o;
		return VALUE1 == that.VALUE1 && VALUE2 == that.VALUE2 && VALUE3 == that.VALUE3 && ORIENTATION_KEY == that.ORIENTATION_KEY;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(VALUE1, VALUE2, VALUE3, ORIENTATION_KEY);
	}
}
