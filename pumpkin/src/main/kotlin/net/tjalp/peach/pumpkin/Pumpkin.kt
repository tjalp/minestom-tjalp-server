package net.tjalp.peach.pumpkin

import java.io.File

fun main(args: Array<String>) {
    val server = PumpkinServer()

    server.init(File("config.json"))

    server.start("0.0.0.0", 34040)
}