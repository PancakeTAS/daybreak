package de.pancake.daybreak.generators;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static de.pancake.daybreak.DaybreakPlugin.BORDER_RADIUS;

/**
 * Vanilla generator for the daybreak plugin.
 * @author Pancake
 */
public class VanillaGenerator extends ChunkGenerator {

    /**
     * Check if chunk is in worldborder.
     * @param x Chunk X
     * @param z Chunk Z
     * @return Whether chunk is in worldborder.
     */
    public boolean isInWorldborder(int x, int z) {
        var radius = BORDER_RADIUS / 16;
        return x < radius && x >= -radius && z < radius && z >= -radius;
    }

    /**
     * Generate barriers around the worldborder.
     * @param worldInfo The world info of the world this chunk will be used for
     * @param random The random generator to use
     * @param chunkX The X-coordinate of the chunk
     * @param chunkZ The Z-coordinate of the chunk
     * @param chunkData To modify
     */
    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        super.generateNoise(worldInfo, random, chunkX, chunkZ, chunkData);

        var radius = BORDER_RADIUS / 16;

        if (chunkX < radius+1 && chunkX >= -radius-1 && chunkZ < radius+1 && chunkZ >= -radius-1) {
            if (chunkX == radius)
                chunkData.setRegion(0, -64, 0, 1, 321, 16, Material.BARRIER);

            if (chunkZ == radius)
                chunkData.setRegion(0, -64, 0, 16, 321, 1, Material.BARRIER);

            if (chunkX == -radius-1)
                chunkData.setRegion(15, -64, 0, 16, 321, 16, Material.BARRIER);

            if (chunkZ == -radius-1)
                chunkData.setRegion(0, -64, 15, 16, 321, 16, Material.BARRIER);
        }
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return isInWorldborder(chunkX, chunkZ);
    }
}
