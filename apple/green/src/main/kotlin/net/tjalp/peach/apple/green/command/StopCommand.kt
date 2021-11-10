package net.tjalp.peach.apple.green.command

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command

class StopCommand : Command("stop", "end", "shutdown") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Stopping server..."))

            MinecraftServer.stopCleanly()
        }
    }
}