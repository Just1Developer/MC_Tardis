package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Material;

import java.util.Random;

public class TardisModelType {

	// Some random values
	private static final int DATA_MIN = 1000000, DATA_MAX = 9999999 - DATA_MIN, SEED = 38589201;
	private static final Random r_gen = new Random(SEED);

	private static int i = 250;
	
	public static final TardisModelType TARDIS_OUTER_STATIC = new TardisModelType(
			"outer-static", Material.SEA_LANTERN, 250, false, false);
	public static final TardisModelType TARDIS_OUTER_ENTER = new TardisModelType(
			"outer-enter", Material.BEDROCK, r_next(), false, false);
	public static final TardisModelType TARDIS_OUTER_EXIT = new TardisModelType(
			"outer-exit", Material.BEDROCK, r_next(), false, false);
	public static final TardisModelType TARDIS_OUTER_DOOR = new TardisModelType(
			"outer-door", Material.AIR, r_next(), false, false);
	public static final TardisModelType TARDIS_INNER_DOOR = new TardisModelType(
			"interior-door", Material.BEDROCK, r_next(), false, false);
	public static final TardisModelType TARDIS_CONSOLE = new TardisModelType(
			"interior-console", Material.BEDROCK, r_next(), false, false);

	private static int r_next() {
		//return r_gen.nextInt(DATA_MAX) + DATA_MIN;
		return i++;
	}

	public final String modelName;
	public final Material baseMaterial;
	public final int customModelData;
	public final boolean isBaby;
	public final boolean shouldGlow;
	
	public TardisModelType(String modelName, Material baseMaterial, int customModelData, boolean isBaby, boolean shouldGlow) {
		this.modelName = modelName;
		this.baseMaterial = baseMaterial;
		this.customModelData = customModelData;
		this.isBaby = isBaby;
		this.shouldGlow = shouldGlow;
	}

}
