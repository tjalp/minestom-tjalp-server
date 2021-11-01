package net.tjalp.minestom.proxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.tjalp.minestom.proxy.Proxy;

import java.util.Optional;

public class ProxyEventListener {

    private final Proxy proxy;

    public ProxyEventListener(Proxy proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    private void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Optional<RegisteredServer> server = proxy.proxy().getServer("minestom");

        if (server.isPresent()) {
            event.setInitialServer(server.get());
            return;
        }

        event.setInitialServer(null);
    }
}
