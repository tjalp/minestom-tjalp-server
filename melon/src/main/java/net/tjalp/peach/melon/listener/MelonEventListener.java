package net.tjalp.peach.melon.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.tjalp.peach.melon.MelonServer;

import java.util.Optional;

public class MelonEventListener {

    private final MelonServer melonServer;

    public MelonEventListener(MelonServer melonServer) {
        this.melonServer = melonServer;
    }

    @Subscribe
    private void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Optional<RegisteredServer> server = melonServer.proxy().getServer("apple");

        if (server.isPresent()) {
            event.setInitialServer(server.get());
            return;
        }

        event.setInitialServer(null);
    }
}
