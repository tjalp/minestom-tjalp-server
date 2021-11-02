package net.tjalp.peach.apple;

public class Bootstrap {

    public static void main(String[] args) {

        // Allow mixins without removing arguments
//        String[] argsWithMixins = new String[args.length+2];
//        System.arraycopy(args, 0, argsWithMixins, 0, args.length);
//        argsWithMixins[argsWithMixins.length-2] = "--mixin";
//        argsWithMixins[argsWithMixins.length-1] = "mixins.tjalp.json";

        net.minestom.server.Bootstrap.bootstrap(AppleServer.class.getName(), args);
    }
}
