package net.tjalp.peach.peel

import net.tjalp.peach.peel.database.RedisManager.SignalKey
import net.tjalp.peach.peel.signal.AppleNodeRegisterSignal
import net.tjalp.peach.peel.signal.AppleNodeUnregisterSignal

/**
 * An apple node has been registered
 */
val APPLE_NODE_REGISTER = SignalKey("peach.apple_node_register", AppleNodeRegisterSignal::class)

/**
 * An apple node has been unregistered
 */
val APPLE_NODE_UNREGISTER = SignalKey("peach.apple_node_unregister", AppleNodeUnregisterSignal::class)