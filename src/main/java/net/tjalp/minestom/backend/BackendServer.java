package net.tjalp.minestom.backend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
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
            player.setRespawnPoint(new Pos(0, 1, 0));
            player.setPermissionLevel(2);
        });

        // Register the commands
        registerCommands();

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
}
