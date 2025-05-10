package be.yvanmazy.proxyonlinelinker.common;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingManager;
import be.yvanmazy.proxyonlinelinker.common.broadcasting.DefaultBroadcastingManager;
import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import be.yvanmazy.proxyonlinelinker.common.config.ConfigurationReader;
import be.yvanmazy.proxyonlinelinker.common.redis.DefaultJedisProvider;
import be.yvanmazy.proxyonlinelinker.common.redis.JedisProvider;
import be.yvanmazy.proxyonlinelinker.common.status.DefaultOnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class ProxyOnlineLinker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyOnlineLinker.class);

    private final String implementationName;
    private final IntSupplier onlineSupplier;
    private final BiConsumer<OnlineManager, ReplacementStrategy> replacementConsumer;
    private Configuration configuration;

    private BroadcastingManager broadcastingManager;
    private OnlineManager onlineManager;

    public ProxyOnlineLinker(final @NotNull String implementationName,
                             final @NotNull IntSupplier onlineSupplier,
                             final @NotNull BiConsumer<OnlineManager, ReplacementStrategy> replacementConsumer) {
        this.implementationName = Preconditions.requireNonBlank(implementationName, "implementationName");
        this.onlineSupplier = Objects.requireNonNull(onlineSupplier, "onlineSupplier must not be null");
        this.replacementConsumer = Objects.requireNonNull(replacementConsumer, "replacementConsumer must not be null");
    }

    public void onEnable(final @NotNull Path configPath) {
        try {
            this.configuration = ConfigurationReader.read(configPath);
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
        this.broadcastingManager = new DefaultBroadcastingManager(this.onlineSupplier);
        this.broadcastingManager.start(this.configuration.broadcasting());
    }

    private void initStatusHandling() {
        this.onlineManager = new DefaultOnlineManager();
        this.onlineManager.start(this.configuration);

        final Configuration.Status.Replacement replacement = this.configuration.status().replacement();

        if (replacement.strategy() == ReplacementStrategy.NONE) {
            return;
        }

        this.replacementConsumer.accept(this.onlineManager, replacement.strategy());
    }

    @Contract(pure = true)
    public @NotNull String getImplementationName() {
        return this.implementationName;
    }

    @Contract(pure = true)
    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    @Contract(pure = true)
    public @NotNull BroadcastingManager getBroadcastingManager() {
        return this.broadcastingManager;
    }

    @Contract(pure = true)
    public @NotNull OnlineManager getOnlineManager() {
        return this.onlineManager;
    }

}