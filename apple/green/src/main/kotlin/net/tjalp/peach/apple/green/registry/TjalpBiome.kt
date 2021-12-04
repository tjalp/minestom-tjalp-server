package net.tjalp.peach.apple.green.registry

import net.minestom.server.MinecraftServer
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.biomes.Biome
import net.minestom.server.world.biomes.BiomeEffects

private val DEFAULT_EFFECTS = BiomeEffects.builder()
    .fogColor(0xC0D8FF)
    .skyColor(0x78A7FF)
    .waterColor(0x3F76E4)
    .waterFogColor(0x50533)
    .build()

// Biomes start here
val PLAINS: Biome = Biome.builder()
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
    .build()

fun registerBiomes() {
    val man = MinecraftServer.getBiomeManager()
    man.addBiome(PLAINS)
}