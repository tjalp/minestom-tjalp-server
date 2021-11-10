package net.tjalp.peach.apple.green.registry

import net.minestom.server.MinecraftServer
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.biomes.Biome
import net.minestom.server.world.biomes.BiomeEffects
import net.minestom.server.world.biomes.BiomeParticles
import net.minestom.server.world.biomes.BiomeParticles.DustParticle

private val DEFAULT_EFFECTS = BiomeEffects.builder()
    .fogColor(0xC0D8FF)
    .skyColor(0x78A7FF)
    .waterColor(0x3F76E4)
    .waterFogColor(0x50533)
    .build()

// Biomes start here
val PLAINS = Biome.builder()
    .category(Biome.Category.NONE)
    .name(NamespaceID.from("tjalp:plains"))
    .temperature(0.8f)
    .downfall(0.4f)
    .depth(0.125f)
    .scale(0.05f)
    .effects(DEFAULT_EFFECTS)
    .build()

val TJALP = Biome.builder()
    .name(NamespaceID.from("tjalp", "tjalp"))
    .category(Biome.Category.NONE)
    .depth(0.125f)
    .downfall(1f)
    .effects(
        BiomeEffects.builder()
            .biomeParticles(BiomeParticles(100f, DustParticle(0f, 0f, 0f, 1f)))
            .skyColor(0x00FF00)
            .fogColor(0x00FF00)
            .foliageColor(0x00FF00)
            .grassColor(0x00FF00)
            .waterColor(0x00FF00)
            .waterFogColor(0x00FF00)
            .build()
    )
    .build()

fun registerBiomes() {
    val man = MinecraftServer.getBiomeManager()
    man.addBiome(PLAINS)
}