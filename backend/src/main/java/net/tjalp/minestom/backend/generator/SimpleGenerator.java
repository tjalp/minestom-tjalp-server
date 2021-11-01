package net.tjalp.minestom.backend.generator;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.interpolation.InterpolationType;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;
import net.tjalp.minestom.backend.registry.TjalpBiome;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimpleGenerator implements ChunkGenerator {

    private final Random random = new Random();
    private final JNoise jNoise = JNoise.newBuilder().perlin().setInterpolation(InterpolationType.LINEAR).setSeed(random.nextInt()).setFrequency(0.4).build();

    public int getHeight(int x, int z) {
        double preHeight = jNoise.getNoise(x / 16.0, z / 16.0);
        return (int) ((preHeight > 0 ? preHeight * 18 : preHeight * 12) + 64);
    }

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                final int height = getHeight(x + chunkX * 16, z + chunkZ * 16);
                for (int y = 0; y < height; y++) {
                    if (y == 0) {
                        batch.setBlock(x, y, z, Block.BEDROCK);
                    } else if (y == height - 1) {
                        batch.setBlock(x, y, z, Block.GRASS_BLOCK);
                    } else if (y > height - 7) {
                        batch.setBlock(x, y, z, Block.DIRT);
                    } else {
                        batch.setBlock(x, y, z, Block.STONE);
                    }
                }
                if (height < 61) {
                    batch.setBlock(x, height - 1, z, Block.DIRT);
                    for (int y = 0; y < 61 - height; y++) {
                        batch.setBlock(x, y + height, z, Block.WATER);
                    }
                }
            }
        }
    }

    @Override
    public void fillBiomes(@NotNull Biome[] biomes, int chunkX, int chunkZ) {
        Arrays.fill(biomes, TjalpBiome.PLAINS);
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        return new ArrayList<>();
    }
}
