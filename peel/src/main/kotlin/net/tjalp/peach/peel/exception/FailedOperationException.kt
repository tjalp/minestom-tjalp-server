package net.tjalp.peach.peel.exception

/**
 * Thrown when an important operation fails to complete it's task(s).
 * When a FailedOperationException is catched, a fallback solution is
 * expected to be executed.
 *
 * Copyright 2019-2021 (c) Exodius Studios. All Rights Reserved.
 *
 * @author Jøøls
 */
open class FailedOperationException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}