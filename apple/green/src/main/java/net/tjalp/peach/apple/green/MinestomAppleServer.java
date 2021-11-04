package net.tjalp.peach.apple.green;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.tjalp.peach.apple.green.command.GamemodeCommand;
import net.tjalp.peach.apple.green.command.SkinCommand;
import net.tjalp.peach.apple.green.command.StopCommand;
import net.tjalp.peach.apple.green.command.TeleportCommand;
import net.tjalp.peach.apple.green.config.MinestomAppleConfig;
import net.tjalp.peach.apple.green.generator.SimpleGenerator;
import net.tjalp.peach.apple.green.listener.AppleEventListener;
import net.tjalp.peach.apple.green.registry.TjalpBiome;
import net.tjalp.peach.apple.green.registry.TjalpDimension;
import net.tjalp.peach.apple.pit.AppleServer;
import net.tjalp.peach.peel.config.JsonConfig;
import net.tjalp.peach.peel.database.RedisManager;

import java.io.File;

public class MinestomAppleServer implements AppleServer {

    /** The static server instance, there should only be one */
    private static MinestomAppleServer instance;

    public static void main(String[] args) {
        MinestomAppleServer server = new MinestomAppleServer();

        // Initialize the BackendServer
        server.start();
    }

    /**
     * Get the server that is currently running
     *
     * @return the server that is currently running
     */
    public static MinestomAppleServer get() {
        return MinestomAppleServer.instance;
    }

    /** The MinestomApple config */
    private JsonConfig<MinestomAppleConfig> config;

    /** The redis manager service */
    private RedisManager redis;

    /** The main instance which is loaded at all times */
    public Instance overworld = null;

    @Override
    public void start() {
        instance = this;
        config = new JsonConfig<>(new File("config.json"), MinestomAppleConfig.class);

        // Initialize the Minecraft server
        MinecraftServer server = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Initialize various services
        redis = new RedisManager(MinecraftServer.LOGGER, config().redis);

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
     * Initiate the shutdown sequence
     * To shut down the server, use
     * MinecraftServer.stopCleanly()
     */
    @Override
    public void shutdown() {
        MinecraftServer.LOGGER.info("Shutting down services...");

        redis().dispose();
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

    public MinestomAppleConfig config() {
        return this.config.data();
    }

    public RedisManager redis() {
        return this.redis;
    }
}
