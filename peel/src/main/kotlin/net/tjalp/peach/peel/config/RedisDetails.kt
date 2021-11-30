package net.tjalp.peach.peel.config

/**
 * The details for the redis connection
 */
class RedisDetails {

    /** The target address for redis */
    var server = "host.docker.internal"

    /** The target port for redis */
    var port = 6379

    /** The password for redis */
    var password = ""
}