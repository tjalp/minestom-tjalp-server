package net.tjalp.peach.peel.network

import io.grpc.ServerCall.Listener

/**
 * A listener implementation which does not forward
 * the call, and simply drops the next pipeline entry.
 */
class BlankListener<ReqT> : Listener<ReqT>()