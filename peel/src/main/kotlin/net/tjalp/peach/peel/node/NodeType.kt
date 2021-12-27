package net.tjalp.peach.peel.node

/**
 * All the node types that exist
 *
 * @param fullName The full, user-friendly name of the current node type
 * @param shortName The short name of the current node type, typically used for node identifiers
 * @param imageName The image name that should be used when creating a node of the current [NodeType]
 */
enum class NodeType(val fullName: String, val shortName: String, val imageName: String) {
    MELON("Melon", "m", "melon"),
    APPLE_GREEN("Apple Green", "ag", "apple-green"),
    APPLE_RED("Apple Red", "ar", "apple-red");
}