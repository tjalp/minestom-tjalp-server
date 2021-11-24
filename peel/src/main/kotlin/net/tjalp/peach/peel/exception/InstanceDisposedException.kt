package net.tjalp.peach.peel.exception

/**
 * Thrown when access to a disposed resource is attempted.
 *
 * This exception is exclusively for usage with [reactor.core.Disposable]
 */
class InstanceDisposedException : RuntimeException {
    constructor() : super("Attempted to access disposed resource")
    constructor(error: String) : super("Attempted to access disposed resource: $error")
    constructor(error: String, ex: Exception?) : super("Attempted to access disposed resource: $error", ex)
}