package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Material;

/**
 * A collection of a lot of constants that we might want to configure in the future, all in the same place.
 */
class TardisConstants {

    static final Material TARDIS_STATIC_SHELL_MATERIAL = Material.BEDROCK;
    static final Material TARDIS_ANIMATED_SHELL_MATERIAL = Material.BLUE_STAINED_GLASS;
    static final Material TIME_ROTOR_MATERIAL = Material.REDSTONE_BLOCK;

    static final Material CHARGER_BLOCK_MATERIAL = Material.BEACON;

    static final int DATA_TIME_ROTOR = 2000;
    static final int DATA_TARDIS_SHELL_ORIGINAL = 2000;
    static final int DATA_TARDIS_SHELL_OFFSET = 100;

    /**
     * The number of frames. When looping through, go from n = 1 to (including) this value
     * and add it to DATA_TARDIS_SHELL_i for the nth frame of the ith shell design.
     */
    static final int DATA_TARDIS_ANIMATION_FRAMES = 2000;
    /**
     * How many tardis shell design (including the original) there are.
     */
    static final int TARDIS_SHELLS = 1;
}
