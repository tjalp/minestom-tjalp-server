package net.tjalp.peach.pumpkin

import java.io.File

fun main(args: Array<String>) {
    val server = PumpkinServer()

    server.mainThread.syncTask {
        server.init(File("config.json"))
        server.start()
    }
}