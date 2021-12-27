package net.tjalp.peach.apple.red.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.tjalp.peach.apple.pit.command.DOCKER_NODE
import net.tjalp.peach.apple.pit.command.NODE_ID
import net.tjalp.peach.apple.pit.command.NODE_PORT
import net.tjalp.peach.apple.pit.command.NODE_TYPE
import net.tjalp.peach.apple.red.PaperAppleServer
import net.tjalp.peach.peel.node.NodeType
import net.tjalp.peach.proto.apple.Apple
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R1.CraftServer
import java.util.concurrent.CompletableFuture

class PeachCommand(
    val apple: PaperAppleServer
) {

    private lateinit var nodeListCache: List<Apple.NodeInfo>
    private var lastCacheTime = 0L

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
                            .then(argument<CommandSourceStack?, String?>(DOCKER_NODE, string())
                                .then(argument<CommandSourceStack, String>(NODE_ID, string())
                                    .then(argument<CommandSourceStack?, Int?>(NODE_PORT, integer())
                                        .executes { context ->
                                            this.executeNodeCreate(context, getString(context, DOCKER_NODE), getString(context, NODE_ID), getInteger(context, NODE_PORT))
                                        })
                                    .executes { context ->
                                        this.executeNodeCreate(context, getString(context, DOCKER_NODE), getString(context, NODE_ID))
                                    })
                                .executes { context ->
                                    this.executeNodeCreate(context, getString(context, DOCKER_NODE))
                                })
                            .suggests { _, builder ->
                                val input = builder.remainingLowerCase
                                for (value in NodeType.values()) {
                                    val name = value.name.lowercase()
                                    if (input == "" || name.startsWith(input)) builder.suggest(name)
                                }
                                builder.buildFuture()
                            }
                            .executes(this::executeNodeCreate)))
                    .then(literal<CommandSourceStack>("stop")
                        .then(argument<CommandSourceStack, String>(NODE_ID, string())
                            .suggests(this::suggestNodeIdList)
                            .executes(this::executeNodeStop)))
                    .then(literal<CommandSourceStack>("kill")
                        .then(argument<CommandSourceStack?, String>(NODE_ID, string())
                            .suggests(this::suggestNodeIdList)
                            .executes(this::executeNodeKill))))
        )
    }

    private fun executeNodeCreate(context: CommandContext<CommandSourceStack>, dockerNode: String? = null, nodeId: String? = null, nodePort: Int? = null): Int {
        val sender = context.source.bukkitSender
        val nodeType = getString(context, NODE_TYPE)

        sender.sendMessage(Component.text("Creating node...").color(NamedTextColor.YELLOW))

        apple.scheduler.launch {
            val request = Apple.CreateNodeRequest.newBuilder()
                .setNodeType(nodeType)

            if (nodeId != null) request.nodeIdentifier = nodeId
            if (nodePort != null) request.nodePort = nodePort

            val response = apple.rpcStub.createNode(request.build())

            if (response.success) {
                sender.sendMessage(
                    Component.text("Succesfully created node with type ").color(NamedTextColor.GREEN)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_GREEN))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier)
                            .color(NamedTextColor.DARK_GREEN)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy!").color(NamedTextColor.GREEN)))
                            .clickEvent(ClickEvent.copyToClipboard(response.nodeIdentifier)))
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

        apple.scheduler.launch {
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

        apple.scheduler.launch {
            val request = Apple.KillNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.killNode(request)

            sender.sendMessage(Component.text("Succesfully killed node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }

        return Command.SINGLE_SUCCESS
    }

    private fun suggestNodeIdList(context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val input = builder.remainingLowerCase

        context.source.bukkitSender.sendMessage("Starting suggestions... (${Bukkit.getServer().currentTick})")

        return CompletableFuture.supplyAsync {
            if (System.currentTimeMillis() - lastCacheTime >= 5000) {
                context.source.bukkitSender.sendMessage("§eFetching nodes... (${Bukkit.getServer().currentTick})")
                runBlocking {
                    nodeListCache = apple.fetchNodes().sortedBy {
                        it.nodeIdentifier
                    }
                    lastCacheTime = System.currentTimeMillis()
                }
                context.source.bukkitSender.sendMessage("§aFetched nodes! (${Bukkit.getServer().currentTick})")
            }

            for (info in nodeListCache) {
                val nodeId = info.nodeIdentifier

                if (nodeId.lowercase().startsWith(input)) {
                    context.source.bukkitSender.sendMessage("§dAdding node $nodeId... (${Bukkit.getServer().currentTick})")
                    builder.suggest(nodeId)
                }
            }
            context.source.bukkitSender.sendMessage("§2Done (${Bukkit.getServer().currentTick})!")
            builder.build()
        }
    }
}