package net.tjalp.peach.pumpkin

import java.io.File
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val server = PumpkinServer()

    server.mainThread.syncTask {
        server.init(File("config.json"))
        server.start()
    }

    Runtime.getRuntime().addShutdownHook(thread(false, name = "Shutdown Thread") {
        server.shutdown()
    })
}