package be.yvanmazy.proxyonlinelinker.velocity;

import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.velocity.listener.DelegateStatusListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;

@Plugin(id = "proxyonlinelinker", name = "ProxyOnlineLinker", version = "1.0")
public final class Main {

    private final ProxyServer proxyServer;
    private final Path dataDirectory;

    private ProxyOnlineLinker proxyOnlineLinker;

    @Inject
    public Main(final ProxyServer proxyServer, final @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onEnable(final ProxyInitializeEvent event) {
        this.proxyOnlineLinker = new ProxyOnlineLinker("Velocity", this.proxyServer::getPlayerCount, this::initReplacement);
        this.proxyOnlineLinker.onEnable(this.dataDirectory.resolve("config.yml"));
    }

    @Subscribe
    public void onDisable(final ProxyShutdownEvent event) {
        if (this.proxyOnlineLinker != null) {
            this.proxyOnlineLinker.onDisable();
        }
    }

    private void initReplacement(final OnlineManager onlineManager, final ReplacementStrategy replacementStrategy) {
        if (replacementStrategy == ReplacementStrategy.DELEGATE) {
            this.proxyServer.getEventManager().register(this, new DelegateStatusListener(onlineManager));
            return;
        }
        throw new UnsupportedOperationException("Replacement strategy " + replacementStrategy + " is not supported");
    }

}