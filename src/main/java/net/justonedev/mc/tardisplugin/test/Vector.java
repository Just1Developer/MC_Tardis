package net.justonedev.mc.tardisplugin.test;

public class Vector {
	
	int x, y, z;
	
	Vector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	int getBlockX() {
		return x;
	}
	
	int getBlockY() {
		return y;
	}
	
	int getBlockZ() {
		return z;
	}
	
	Vector copy() {
		return new Vector(x, y, z);
	}
	
}
