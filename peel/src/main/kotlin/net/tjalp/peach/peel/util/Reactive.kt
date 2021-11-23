package net.tjalp.peach.peel.util

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import reactor.core.publisher.Mono

/**
 * Convert a ListenableFuture into a reactive Mono
 *
 * @return Mono stream
 */
fun <T> ListenableFuture<T>.reactive() : Mono<T> {
    return Mono.create { sink ->
        Futures.addCallback(this, object : FutureCallback<T> {

            override fun onSuccess(result: T?) {
                sink.success(result)
            }

            override fun onFailure(err: Throwable) {
                sink.error(err)
            }

        }, MoreExecutors.directExecutor())
    }
}