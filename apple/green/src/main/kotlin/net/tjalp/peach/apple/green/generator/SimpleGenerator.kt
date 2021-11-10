package net.tjalp.peach.apple.green.generator

import de.articdive.jnoise.JNoise
import de.articdive.jnoise.interpolation.InterpolationType
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.world.biomes.Biome
import net.tjalp.peach.apple.green.registry.PLAINS
import java.util.*

class SimpleGenerator : ChunkGenerator {

    private val random = Random()
    private val jNoise =
        JNoise.newBuilder().perlin().setInterpolation(InterpolationType.LINEAR).setSeed(random.nextInt().toLong())
            .setFrequency(0.4).build()

    private fun getHeight(x: Int, z: Int): Int {
        val preHeight = jNoise.getNoise(x / 16.0, z / 16.0)
        return ((if (preHeight > 0) preHeight * 18 else preHeight * 12) + 64).toInt()
    }

    override fun generateChunkData(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {
        for (x in 0 until Chunk.CHUNK_SIZE_X) {
            for (z in 0 until Chunk.CHUNK_SIZE_Z) {
                val height = getHeight(x + chunkX * 16, z + chunkZ * 16)
                for (y in 0 until height) {
                    if (y == 0) {
                        batch.setBlock(x, y, z, Block.BEDROCK)
                    } else if (y == height - 1) {
                        batch.setBlock(x, y, z, Block.GRASS_BLOCK)
                    } else if (y > height - 7) {
                        batch.setBlock(x, y, z, Block.DIRT)
                    } else {
                        batch.setBlock(x, y, z, Block.STONE)
                    }
                }
                if (height < 61) {
                    batch.setBlock(x, height - 1, z, Block.DIRT)
                    for (y in 0 until 61 - height) {
                        batch.setBlock(x, y + height, z, Block.WATER)
                    }
                }
            }
        }
    }

    override fun fillBiomes(biomes: Array<out Biome>, chunkX: Int, chunkZ: Int) {
        Arrays.fill(biomes, PLAINS)
    }

    override fun getPopulators(): List<ChunkPopulator?> {
        return ArrayList()
    }
}