package net.tjalp.peach.pumpkin;

public class Pumpkin {

    public static void main(String[] args) {
        PumpkinServer server = new PumpkinServer();

        server.init();

        server.start("0.0.0.0", 37474);
    }
}
