package net.tjalp.peach.peel.config;

/**
 * An interface that is applied to classes used
 * for [JsonConfig]
 */
public interface Configurable {

    /**
     * Called when the configuration
     * is loaded from the disk
     */
    default void onLoad() {}

    /**
     * Called when the configuration
     * is saved to the disk
     */
    default void onSave() {}
}
