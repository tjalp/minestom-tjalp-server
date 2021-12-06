package net.tjalp.peach.apple.red.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.tjalp.peach.apple.pit.command.NODE_ID
import net.tjalp.peach.apple.pit.command.NODE_TYPE
import net.tjalp.peach.apple.red.PaperAppleServer
import net.tjalp.peach.proto.apple.Apple
import org.bukkit.craftbukkit.v1_18_R1.CraftServer

class PeachCommand(
    val apple: PaperAppleServer
) {

    init {
        val server = apple.plugin.server
        val cbServer = server as CraftServer
        val nmsServer = cbServer.server

        nmsServer.vanillaCommandDispatcher.dispatcher.register(
            literal<CommandSourceStack>("peach")
                .requires { source ->
                    source.hasPermission(2)
                }
                .then(literal<CommandSourceStack>("node")
                    .then(literal<CommandSourceStack>("create")
                        .then(argument<CommandSourceStack, String>(NODE_TYPE, string())
                            .then(argument<CommandSourceStack, String>(NODE_ID, string())
                                .executes { context ->
                                    this.executeNodeCreate(context, getString(context, NODE_ID))
                                })
                            .executes(this::executeNodeCreate)))
                    .then(literal<CommandSourceStack>("stop")
                        .then(argument<CommandSourceStack, String>(NODE_ID, string())
                            .executes(this::executeNodeStop)))
                    .then(literal<CommandSourceStack>("kill")
                        .then(argument<CommandSourceStack?, String>(NODE_ID, string())
                            .executes(this::executeNodeKill))))
        )
    }

    private fun executeNodeCreate(context: CommandContext<CommandSourceStack>, nodeId: String? = null): Int {
        val sender = context.source.bukkitSender
        val nodeType = getString(context, NODE_TYPE)

        sender.sendMessage(Component.text("Creating node...").color(NamedTextColor.YELLOW))

        GlobalScope.launch {
            val request = Apple.CreateNodeRequest.newBuilder()
                .setNodeType(nodeType)

            if (nodeId != null) request.nodeIdentifier = nodeId

            val response = apple.rpcStub.createNode(request.build())

            if (response.success) {
                sender.sendMessage(
                    Component.text("Succesfully created node with type ").color(NamedTextColor.GREEN)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_GREEN))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier).color(NamedTextColor.DARK_GREEN))
                )
            } else {
                sender.sendMessage(
                    Component.text("Failed to create node with type ").color(NamedTextColor.RED)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_RED))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier).color(NamedTextColor.DARK_RED))
                )
            }
        }

        return Command.SINGLE_SUCCESS
    }

    private fun executeNodeStop(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.bukkitSender
        val nodeId = getString(context, NODE_ID)

        sender.sendMessage(
            Component.text("Stopping node with identifier ").color(NamedTextColor.YELLOW)
                .append(Component.text(nodeId).color(NamedTextColor.GOLD))
        )

        GlobalScope.launch {
            val request = Apple.StopNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.stopNode(request)

            sender.sendMessage(Component.text("Succesfully stopped node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }

        return Command.SINGLE_SUCCESS
    }

    private fun executeNodeKill(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.bukkitSender
        val nodeId = getString(context, NODE_ID)

        sender.sendMessage(
            Component.text("Killing node with identifier ").color(NamedTextColor.YELLOW)
                .append(Component.text(nodeId).color(NamedTextColor.GOLD))
        )

        GlobalScope.launch {
            val request = Apple.KillNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.killNode(request)

            sender.sendMessage(Component.text("Succesfully killed node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }

        return Command.SINGLE_SUCCESS
    }
}