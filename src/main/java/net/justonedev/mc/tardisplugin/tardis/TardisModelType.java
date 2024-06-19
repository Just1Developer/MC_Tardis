package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Material;

public class TardisModelType {

	public static final int TARDIS_OUTER_STATIC_DATA = 2500;
	public static final int TARDIS_OUTER_ENTRY_DATA = 2501;
	public static final int TARDIS_OUTER_EXIT_DATA = 2502;
	public static final int TARDIS_INNER_EXIT_DATA = 2503;
	public static final int TARDIS_CONSOLE_DATA = 2504;
	public static final int TARDIS_TIME_ROTOR_DATA = 2505;
	
	// We will never "spawn" exit, we'll only change the item model data. No need for a preset
	
	public static final TardisModelType TARDIS_OUTER_STATIC = new TardisModelType(
			"outer-static", Material.BEDROCK, TARDIS_OUTER_STATIC_DATA, false, false);
	public static final TardisModelType TARDIS_OUTER_ENTER = new TardisModelType(
			"outer-enter", Material.BEDROCK, TARDIS_OUTER_ENTRY_DATA, false, false);
	public static final TardisModelType TARDIS_INNER_DOOR = new TardisModelType(
			"interior-door", Material.BEDROCK, TARDIS_CONSOLE_DATA, false, false);
	public static final TardisModelType TARDIS_CONSOLE = new TardisModelType(
			"interior-console", Material.BEDROCK, TARDIS_TIME_ROTOR_DATA, false, false);

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
