package net.tjalp.peach.peel.config

/**
 * The details for a server with docker
 * that can be used for nodes
 */
class DockerDetails : Configurable {

    /**
     * The target address for this docker node
     */
    var server: String = "host.docker.internal"

    /**
     * The target port for this docker node
     */
    var port: Int = 2375

    /**
     * How many connections may be made
     */
    var maxConnections: Int = 100

}