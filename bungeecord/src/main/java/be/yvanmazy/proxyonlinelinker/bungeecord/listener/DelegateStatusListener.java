package be.yvanmazy.proxyonlinelinker.bungeecord.listener;

import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DelegateStatusListener implements Listener {

    private final OnlineManager onlineManager;

    public DelegateStatusListener(final @NotNull OnlineManager onlineManager) {
        this.onlineManager = Objects.requireNonNull(onlineManager, "statusManager must not be null");
    }

    @EventHandler
    public void onStatusRequest(final ProxyPingEvent event) {
        final ServerPing response = event.getResponse();
        final ServerPing.Players players = response.getPlayers();
        players.setOnline(this.onlineManager.getOnlineCount());
        response.setPlayers(players);
    }

}