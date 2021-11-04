package net.tjalp.peach.apple.green.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "end");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Stopping server");

            MinecraftServer.stopCleanly();
        });
    }
}
