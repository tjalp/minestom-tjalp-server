package net.tjalp.minestom.backend;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.tjalp.minestom.backend.command.GamemodeCommand;
import net.tjalp.minestom.backend.command.SkinCommand;
import net.tjalp.minestom.backend.command.StopCommand;
import net.tjalp.minestom.backend.command.TeleportCommand;
import net.tjalp.minestom.backend.generator.SimpleGenerator;
import net.tjalp.minestom.backend.listener.BackendEventListener;
import net.tjalp.minestom.backend.registry.TjalpBiome;
import net.tjalp.minestom.backend.registry.TjalpDimension;

public class BackendServer {

    /** The static server instance, there should only be one */
    private static BackendServer instance;

    public static void main(String[] args) {
        BackendServer server = new BackendServer();

        // Initialize the BackendServer
        server.init();
    }

    /**
     * Get the server that is currently running
     *
     * @return the server that is currently running
     */
    public static BackendServer get() {
        return instance;
    }

    /** The main instance which is loaded at all times */
    public Instance overworld = null;

    /**
     * Initialize the server
     */
    public void init() {
        instance = this;

        // Initialize the Minecraft server
        MinecraftServer server = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Enable Mojang authentication
        //MojangAuth.init();

        // Register the main listener
        new BackendEventListener();

        // Register the services
        registerCommands();
        TjalpDimension.registerDimensions();
        TjalpBiome.registerBiomes();

        // Set some useful values
        //MinecraftServer.setChunkViewDistance(10);
        MinecraftServer.setBrandName("tjalp");

        // Create the instance
        overworld = instanceManager.createInstanceContainer(TjalpDimension.OVERWORLD);
        overworld.setChunkGenerator(new SimpleGenerator());

        // Specify a shutdownTask
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::shutdown);

        // Start the server
        server.start("0.0.0.0", 25565);
    }

    /**
     * Do some stuff on shutdown
     */
    public void shutdown() {
        System.out.println("Noticed that the server is shutting down");
    }

    /**
     * Register the commands the server should use
     */
    private void registerCommands() {
        CommandManager man = MinecraftServer.getCommandManager();

        man.register(new GamemodeCommand());
        man.register(new SkinCommand());
        man.register(new StopCommand());
        man.register(new TeleportCommand());
    }
}
