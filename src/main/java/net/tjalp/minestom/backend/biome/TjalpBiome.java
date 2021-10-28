package net.tjalp.minestom.backend.biome;

import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.Biome.*;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeParticles;

public class TjalpBiome {

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
}
