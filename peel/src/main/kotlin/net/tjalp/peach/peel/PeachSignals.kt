package net.tjalp.peach.peel

import net.tjalp.peach.peel.database.RedisManager.SignalKey
import net.tjalp.peach.peel.signal.AppleNodeRegisterSignal
import net.tjalp.peach.peel.signal.AppleNodeUnregisterSignal
import net.tjalp.peach.peel.signal.EmptySignal
import net.tjalp.peach.peel.signal.PlayerSwitchSignal

/**
 * Request a connection to Pumpkin
 */
val REQUEST_PUMPKIN_CONNECT = SignalKey("peach.request_pumpkin_connect", EmptySignal::class)

/**
 * An apple node has been registered
 */
val APPLE_NODE_REGISTER = SignalKey("peach.apple_node_register", AppleNodeRegisterSignal::class)

/**
 * An apple node has been unregistered
 */
val APPLE_NODE_UNREGISTER = SignalKey("peach.apple_node_unregister", AppleNodeUnregisterSignal::class)

/**
 * Switch a player between apple nodes
 */
val PLAYER_SWITCH = SignalKey("peach.player_switch", PlayerSwitchSignal::class)