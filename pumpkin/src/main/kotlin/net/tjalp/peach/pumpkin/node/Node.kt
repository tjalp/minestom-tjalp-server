package net.tjalp.peach.pumpkin.node

import reactor.core.Disposable

/**
 * Represents any external server instance that
 * can communicate to melon.
 */
interface Node : Comparable<Node>, Disposable {

    /**
     * The unique string that has been assigned to
     * this node by pumpkin
     */
    val nodeIdentifier: String

    /**
     * Contains true when pumpkin is confident this
     * node is currently online and available
     */
    val isOnline: Boolean

    /**
     * The docker node this node is running on
     */
    val dockerNode: DockerNode

}