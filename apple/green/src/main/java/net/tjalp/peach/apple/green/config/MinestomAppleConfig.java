package net.tjalp.peach.apple.green.config;

import net.tjalp.peach.peel.config.Configurable;
import net.tjalp.peach.peel.config.RedisDetails;

public class MinestomAppleConfig implements Configurable {

    /** Redis connection instructions **/
    public RedisDetails redis = new RedisDetails();
}
