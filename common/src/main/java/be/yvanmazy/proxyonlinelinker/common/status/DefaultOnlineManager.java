package be.yvanmazy.proxyonlinelinker.common.status;

import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultOnlineManager implements OnlineManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOnlineManager.class);

    private Configuration.Status config;
    private ScheduledExecutorService executorService;

    private long lastUpdate;
    private int online;

    @Override
    public void start(final @NotNull Configuration configuration) {
        this.config = Objects.requireNonNull(configuration, "configuration must not be null").status();
        if (!this.config.enabled()) {
            throw new IllegalArgumentException("Status must be enabled!");
        }

        if (this.config.sources().isEmpty()) {
            LOGGER.warn("No status sources configured, online count will always be 0!");
        }

        if (!this.config.requestOnDemand()) {
            final long expiration = this.config.globalCacheExpiration();
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.executorService.scheduleWithFixedDelay(() -> this.checkOnline(false), expiration, expiration, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
    }

    @Override
    public int getOnlineCount() {
        if (this.config.requestOnDemand()) {
            this.checkOnline(true);
        }
        return this.online;
    }

    private void checkOnline(final boolean checkExpiration) {
        if (checkExpiration) {
            final long now = System.currentTimeMillis();
            if (now - this.lastUpdate < this.config.globalCacheExpiration()) {
                return;
            }
            this.lastUpdate = now;
        }

        int total = 0;
        for (final StatusSource source : this.config.sources()) {
            try {
                final int fetched = source.fetch();
                if (fetched < 0) { // TODO: Handle fallback
                    continue;
                }
                total += fetched;
            } catch (final Exception exception) {
                LOGGER.error("Failed to fetch status ", exception);
            }
        }
        this.online = total;
    }

    @Contract(pure = true)
    public int getCachedOnline() {
        return this.online;
    }

}