package net.tjalp.peach.apple.green

import net.minestom.server.Bootstrap
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader

fun main(args: Array<String>) {

    // Allow mixins without removing arguments (BROKEN)
//        String[] argsWithMixins = new String[args.length+2];
//        System.arraycopy(args, 0, argsWithMixins, 0, args.length);
//        argsWithMixins[argsWithMixins.length-2] = "--mixin";
//        argsWithMixins[argsWithMixins.length-1] = "mixins.tjalp.json";

    // Prevent lettuce from using Minestom's class loader
    val packages = MinestomRootClassLoader.getInstance().protectedPackages
    packages.add("io.lettuce")
    packages.add("reactor.core")

    Bootstrap.bootstrap(MinestomAppleServer::class.qualifiedName + "Kt", args)
}