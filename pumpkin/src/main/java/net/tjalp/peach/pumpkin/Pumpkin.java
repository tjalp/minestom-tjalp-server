package net.tjalp.peach.pumpkin;

import java.io.File;

public class Pumpkin {

    public static void main(String[] args) {
        PumpkinServer server = new PumpkinServer();

        server.init(new File("config.json"));

        server.start("0.0.0.0", 34040);
    }
}
