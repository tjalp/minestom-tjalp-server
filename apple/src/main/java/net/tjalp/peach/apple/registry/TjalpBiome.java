package net.tjalp.peach.apple.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.Biome.*;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeManager;
import net.minestom.server.world.biomes.BiomeParticles;

public class TjalpBiome {

    private static final BiomeEffects DEFAULT_EFFECTS = BiomeEffects.builder()
            .fogColor(0xC0D8FF)
            .skyColor(0x78A7FF)
            .waterColor(0x3F76E4)
            .waterFogColor(0x50533)
            .build();

    // Biomes start here
    public static final Biome PLAINS = Biome.builder()
            .category(Category.NONE)
            .name(NamespaceID.from("tjalp:plains"))
            .temperature(0.8F)
            .downfall(0.4F)
            .depth(0.125F)
            .scale(0.05F)
            .effects(DEFAULT_EFFECTS)
            .build();

    public static final Biome TJALP = Biome.builder()
            .name(NamespaceID.from("tjalp", "tjalp"))
            .category(Category.NONE)
            .depth(0.125F)
            .downfall(1F)
            .effects(BiomeEffects.builder()
                    .biomeParticles(new BiomeParticles(100F, new BiomeParticles.DustParticle(0f, 0f, 0f, 1F)))
                    .skyColor(0x00FF00)
                    .fogColor(0x00FF00)
                    .foliageColor(0x00FF00)
                    .grassColor(0x00FF00)
                    .waterColor(0x00FF00)
                    .waterFogColor(0x00FF00)
                    .build())
            .build();

    public static void registerBiomes() {
        BiomeManager man = MinecraftServer.getBiomeManager();

        man.addBiome(TjalpBiome.PLAINS);
    }
}
