package be.yvanmazy.proxyonlinelinker.transferproxy;

import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import be.yvanmazy.proxyonlinelinker.common.config.ConfigurationReader;
import be.yvanmazy.proxyonlinelinker.common.status.DefaultOnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.transferproxy.listener.DelegateStatusListener;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.StatusListener;
import net.transferproxy.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class Main implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Configuration configuration;

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

        if (this.configuration.status().enabled()) {
            this.initStatusHandling();
        }
    }

    @Override
    public void onDisable() {
        if (this.onlineManager != null) {
            this.onlineManager.stop();
        }
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