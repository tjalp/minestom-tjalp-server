package net.tjalp.peach.pumpkin.config;

import net.tjalp.peach.peel.config.Configurable;
import net.tjalp.peach.peel.config.RedisDetails;

public class PumpkinConfig implements Configurable {

    /** Redis connection instructions **/
    public RedisDetails redis = new RedisDetails();
}
