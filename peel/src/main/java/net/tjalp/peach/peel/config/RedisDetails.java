package net.tjalp.peach.peel.config;

/**
 * The details for the redis connection
 */
public class RedisDetails implements Configurable {

    /** The target address for redis */
    public String address = "127.0.0.1";

    /** The target port for redis */
    public int port = 6379;

    /** The password for redis */
    public String password = "";
}
