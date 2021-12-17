package net.tjalp.peach.apple.pit.map

import reactor.core.Disposable

/**
 * A [Map] is the visual
 */
interface Map : Disposable {

    /**
     * The unique identifier of this [Map]
     */
    val identifier: String

    /**
     * Load the [Map] so it can be used
     * on the current apple implementation
     */
    fun load()

    /**
     * Unload the [Map] so it can no longer
     * be used on the current apple
     * implementation
     */
    fun unload()

    /**
     * Whether the map is loaded or not
     *
     * @return true if loaded
     */
    fun isLoaded(): Boolean

    override fun dispose() {
        unload()
    }
}