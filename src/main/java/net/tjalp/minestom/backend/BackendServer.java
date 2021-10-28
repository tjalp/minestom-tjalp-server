package net.tjalp.minestom.backend;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.world.biomes.BiomeManager;
import net.tjalp.minestom.backend.biome.TjalpBiome;
import net.tjalp.minestom.backend.command.GamemodeCommand;
import net.tjalp.minestom.backend.command.StopCommand;
import net.tjalp.minestom.backend.generator.SimpleGenerator;

public class BackendServer {

    /**
     * Initialize the server
     */
    public void init() {
        // Initialize the Minecraft server
        MinecraftServer server = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Create the instance
        InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setChunkGenerator(new SimpleGenerator());

        // Enable Mojang authentication
        MojangAuth.init();

        // Specify the instance the player should be spawned in
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(PlayerLoginEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0, 100, 0));
            player.setPermissionLevel(2);
        });

        handler.addListener(ServerListPingEvent.class, event -> {
            ResponseData data = event.getResponseData();

            data.setProtocol(-1);
            data.setVersion(
                    LegacyComponentSerializer.legacySection().serialize(
                            Component.text("tjalp's ").color(NamedTextColor.DARK_AQUA)
                                    .append(Component.text("server ->").color(NamedTextColor.AQUA))
                                    .append(Component.text("                                                                    "))
                                    .append(Component.text(data.getOnline()).color(NamedTextColor.GRAY))
                                    .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                                    .append(Component.text(data.getMaxPlayer()).color(NamedTextColor.GRAY))
                    )
            );
        });

        // Register the services
        registerCommands();
        registerBiomes();

        // Start the server
        server.start("0.0.0.0", 25565);
    }

    /**
     * Register the commands the server should use
     */
    private void registerCommands() {
        CommandManager man = MinecraftServer.getCommandManager();

        man.register(new GamemodeCommand());
        man.register(new StopCommand());
    }

    /**
     * Register the biomes so they can be used
     */
    private void registerBiomes() {
        BiomeManager man = MinecraftServer.getBiomeManager();

        man.addBiome(TjalpBiome.TJALP);
    }
}
