package net.tjalp.peach.peel.config

/**
 * An interface that is applied to classes used
 * for [JsonConfig]
 */
interface Configurable {

    /**
    * Called when the configuration
    * is loaded from the disk
    */
    fun onLoad() {
        return
    }

    /**
     * Called when the configuration
     * is saved to the disk
     */
    fun onSave() {
        return
    }
}