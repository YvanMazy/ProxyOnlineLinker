package be.yvanmazy.proxyonlinelinker.velocity.listener;

import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DelegateStatusListener {

    private final OnlineManager onlineManager;

    public DelegateStatusListener(final @NotNull OnlineManager onlineManager) {
        this.onlineManager = Objects.requireNonNull(onlineManager, "statusManager must not be null");
    }

    @Subscribe
    public void onPing(final ProxyPingEvent event) {
        event.setPing(event.getPing().asBuilder().onlinePlayers(this.onlineManager.getOnlineCount()).build());
    }

}