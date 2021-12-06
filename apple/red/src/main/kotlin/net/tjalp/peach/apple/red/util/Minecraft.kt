package net.tjalp.peach.apple.red.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.TextComponent

/**
 * Convert a kyori [Component] to a Minecraft [TextComponent]
 */
fun Component.toMinecraftComponent(): TextComponent {
    return net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serializeToTree(this)) as TextComponent
}