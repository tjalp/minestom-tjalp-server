package net.tjalp.peach.apple.green.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.DimensionTypeManager;

public class TjalpDimension {

    public static final DimensionType OVERWORLD = DimensionType.builder(NamespaceID.from("minecraft:overworld"))
            .ultrawarm(false)
            .natural(true)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(true)
            .raidCapable(true)
            .skylightEnabled(false)
            .ceilingEnabled(false)
            .fixedTime(null)
            .ambientLight(1.0f)
            .height(256)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build();

    public static void registerDimensions() {
        DimensionTypeManager man = MinecraftServer.getDimensionTypeManager();

        man.removeDimension(DimensionType.OVERWORLD);

        man.addDimension(OVERWORLD);
    }
}
