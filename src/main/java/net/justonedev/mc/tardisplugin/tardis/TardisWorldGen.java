package net.justonedev.mc.tardisplugin.tardis;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class TardisWorldGen {

    /**
     * The total plot size of x*x square for the tardis, this is the edge length of the square.
     */
    static final int PLOT_SIZE = 4999;   // Border is minimal smaller than 30 million, meaning for space we scrap 1 plot
    static final int PLOT_CENTER = 2500;
    // Plots: 12002, or around 12000 plots in each direction
    // Total plots = 12000 * 12000 = 144000000 = 144 Million plots
    // Takes up 59988000 blocks each direction. Space for border <-> plot:
    //      59999968 - 59988000 = 19968
    //      Per side: 19968 => 9984
    private static final int PLOT_START_XZ = 9984;
    private static final int TARDISES_PER_ROW = 12000;
    private static final int TARDISES_LIMIT = TARDISES_PER_ROW * TARDISES_PER_ROW;
    // Console will be at center point of 4999 plot => 2500 x/y + offset of plot

    /**
     * The amount of edge on each plot that is not usable because it's supposed to be a buffer
     * so interior of different tardises don't touch
     */
    private static final int EDGE_WIDTH = 25;

    private static final int WORLD_EDGE_COORD = 29999984;    // Maximum: 29999984, leave some edge too
    private static final int ZERO_COORDINATE_OFFSET = WORLD_EDGE_COORD - PLOT_START_XZ;    // Maximum: 29999984, leave some edge too
    private static final int WORLD_BORDER_MAX = 59999968;
    private static final int PLOT_END_XZ = WORLD_BORDER_MAX - PLOT_START_XZ;

    private static final String INTERIOR_WORLD_NAME = "tardis_interior";

    private static World world;


    static final int CONSOLE_CENTER_HEIGHT = 50;

    /**
     * Initializes the world gen. If the world for tardises does not exist, creates one.
     */
    public static void initialize() {
        Bukkit.getLogger().info("Initializing World Gen");
        world = Bukkit.getWorld(INTERIOR_WORLD_NAME);
        if (world == null) {
            if (!generateInteriorWorld()) {
                Bukkit.getLogger().severe("Tardis world failed to initialize");
                return;
            }
        }
        // World now exists
        Bukkit.getLogger().info("World Gen initialized successfully");
    }

    public static World getInteriorWorld() {
        return world;
    }

    /**
     * Calculates the interior plot the tardis with the given ID would have.
     * @param tardisID The tardis ID.
     * @return An optional of the interior plot. Empty if ID is invalid.
     */
    public static Optional<TardisInteriorPlot> calculateInteriorPlotByID(int tardisID) {
        if (tardisID < 0 || tardisID >= TARDISES_LIMIT) return Optional.empty();
        int row = tardisID / TARDISES_PER_ROW;
        int col = tardisID % TARDISES_PER_ROW;
        int x = row * PLOT_SIZE - ZERO_COORDINATE_OFFSET;
        int z = col * PLOT_SIZE - ZERO_COORDINATE_OFFSET;
        return Optional.of(new TardisInteriorPlot(x, z));
    }

    /**
     * Calculates the ID of the tardis given the X and Z coordinates within the interior plot.
     * @param x the x coordinate from any location inside the plot.
     * @param z the z coordinate from any location inside the plot.
     * @return An optional of the tardis ID. Empty if x or z are outside the bounds of plots.
     */
    public static Optional<Integer> calculateTardisIDbyLoc(int x, int z) {
        if (x < -ZERO_COORDINATE_OFFSET || z < -ZERO_COORDINATE_OFFSET
                || x >= PLOT_END_XZ || z >= PLOT_END_XZ) return Optional.empty();
        x += ZERO_COORDINATE_OFFSET;
        z += ZERO_COORDINATE_OFFSET;
        int row = (x / PLOT_SIZE);
        int col = (z / PLOT_SIZE);
        int tardisID = row * TARDISES_PER_ROW + col;
        return Optional.of(tardisID);
    }

    private static boolean generateInteriorWorld() {
        WorldCreator worldCreator = new WorldCreator(INTERIOR_WORLD_NAME);
        // Create completely empty world here. Flatland, no structures, no blocks, completely empty.
        worldCreator.generator(new EmptyChunkGenerator());
        world = worldCreator.createWorld();
        if (world == null) return false;
        world.getWorldBorder().setSize(WORLD_BORDER_MAX);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(12000);   // High noon
        world.setGameRule(GameRule.FIRE_DAMAGE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setClearWeatherDuration(Integer.MAX_VALUE);
        return true;
    }

    private static class EmptyChunkGenerator extends ChunkGenerator {
        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }

        @Override
        public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
            // Intentionally left empty to avoid generating any blocks
        }

        @Override
        @NotNull
        public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
            return Collections.emptyList();  // No block populators
        }
    }
}
