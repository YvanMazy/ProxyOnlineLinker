package be.yvanmazy.proxyonlinelinker.transferproxy;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingManager;
import be.yvanmazy.proxyonlinelinker.common.broadcasting.DefaultBroadcastingManager;
import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import be.yvanmazy.proxyonlinelinker.common.config.ConfigurationReader;
import be.yvanmazy.proxyonlinelinker.common.redis.DefaultJedisProvider;
import be.yvanmazy.proxyonlinelinker.common.redis.JedisProvider;
import be.yvanmazy.proxyonlinelinker.common.status.DefaultOnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.transferproxy.listener.DelegateStatusListener;
import io.netty.channel.group.ChannelGroup;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.StatusListener;
import net.transferproxy.api.network.NetworkServer;
import net.transferproxy.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class Main implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Configuration configuration;

    private BroadcastingManager broadcastingManager;
    private OnlineManager onlineManager;

    @Override
    public void onEnable() {
        final Path path = this.getConfigPath();
        try {
            this.configuration = ConfigurationReader.read(path);
        } catch (final IOException exception) {
            LOGGER.error("Failed to read configuration", exception);
            return;
        }

        if (this.configuration.needRedis()) {
            JedisProvider.INSTANCE.set(new DefaultJedisProvider(this.configuration.redis()));
        }

        if (this.configuration.broadcasting().enabled()) {
            this.initBroadcasting();
        }

        if (this.configuration.status().enabled()) {
            this.initStatusHandling();
        }
    }

    @Override
    public void onDisable() {
        if (this.broadcastingManager != null) {
            this.broadcastingManager.stop();
        }
        if (this.onlineManager != null) {
            this.onlineManager.stop();
        }
        if (JedisProvider.INSTANCE.isDefined()) {
            JedisProvider.INSTANCE.get().stop();
        }
    }

    private void initBroadcasting() {
        this.broadcastingManager = new DefaultBroadcastingManager(() -> {
            final NetworkServer networkServer = TransferProxy.getInstance().getNetworkServer();
            if (networkServer == null) {
                return 0;
            }
            final ChannelGroup group = networkServer.getGroup();
            if (group == null) {
                return 0;
            }
            return group.size();
        });
        this.broadcastingManager.start(this.configuration.broadcasting());
    }

    private void initStatusHandling() {
        this.onlineManager = new DefaultOnlineManager();
        this.onlineManager.start(this.configuration);

        final Configuration.Status.Replacement replacement = this.configuration.status().replacement();

        if (replacement.strategy() == ReplacementStrategy.NONE) {
            return;
        }

        if (replacement.strategy() == ReplacementStrategy.DELEGATE) {
            this.getEventManager().<StatusListener>addListener(EventType.STATUS, new DelegateStatusListener(this.onlineManager));
        } else {
            throw new UnsupportedOperationException("Replacement strategy " + replacement.strategy() + " is not supported");
        }
    }

}