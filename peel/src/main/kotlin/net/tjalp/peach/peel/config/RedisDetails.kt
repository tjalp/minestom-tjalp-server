package net.tjalp.peach.peel.config

/**
 * The details for the redis connection
 */
class RedisDetails : Configurable {

    /** The target address for redis */
    var server = "127.0.0.1"

    /** The target port for redis */
    var port = 6379

    /** The password for redis */
    var password = ""
}