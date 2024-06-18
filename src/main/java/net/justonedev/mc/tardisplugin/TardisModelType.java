package net.justonedev.mc.tardisplugin;

import org.bukkit.Material;

public class TardisModelType {

	public final Material baseMaterial;
	public final int customModelData;
	public final boolean isBaby;
	public final boolean shouldGlow;
	
	public TardisModelType(Material baseMaterial, int customModelData, boolean isBaby, boolean shouldGlow) {
		this.baseMaterial = baseMaterial;
		this.customModelData = customModelData;
		this.isBaby = isBaby;
		this.shouldGlow = shouldGlow;
	}
	
	public static final TardisModelType TARDIS_OUTER_STATIC = new TardisModelType(Material.BEDROCK, 1, false, false);

}
