package net.tjalp.peach.apple;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.tjalp.peach.apple.command.GamemodeCommand;
import net.tjalp.peach.apple.command.SkinCommand;
import net.tjalp.peach.apple.command.StopCommand;
import net.tjalp.peach.apple.command.TeleportCommand;
import net.tjalp.peach.apple.generator.SimpleGenerator;
import net.tjalp.peach.apple.listener.AppleEventListener;
import net.tjalp.peach.apple.registry.TjalpBiome;
import net.tjalp.peach.apple.registry.TjalpDimension;

public class AppleServer {

    /** The static server instance, there should only be one */
    private static AppleServer instance;

    public static void main(String[] args) {
        AppleServer server = new AppleServer();

        // Initialize the BackendServer
        server.init();
    }

    /**
     * Get the server that is currently running
     *
     * @return the server that is currently running
     */
    public static AppleServer get() {
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
        new AppleEventListener();

        // Register the services
        registerCommands();
        TjalpDimension.registerDimensions();
        TjalpBiome.registerBiomes();

        // Set some useful values
        //MinecraftServer.setChunkViewDistance(10);
        MinecraftServer.setBrandName("apple");

        // Enable Velocity proxy
        VelocityProxy.enable("OpkUJU3FGM3I"); // TODO should probably make this secured & private

        // Create the instance
        overworld = instanceManager.createInstanceContainer(TjalpDimension.OVERWORLD);
        overworld.setChunkGenerator(new SimpleGenerator());

        // Specify a shutdownTask
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::shutdown).schedule();

        // Start the server
        server.start("0.0.0.0", 25000);
    }

    /**
     * Do some stuff on shutdown
     */
    public void shutdown() {
        MinecraftServer.LOGGER.info("Noticed that the server is shutting down");
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