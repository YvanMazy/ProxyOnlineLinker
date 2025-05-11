/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package be.yvanmazy.proxyonlinelinker.common.broadcasting;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.target.BroadcastingTarget;
import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

public class DefaultBroadcastingManager implements BroadcastingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBroadcastingManager.class);

    private final IntSupplier onlineSupplier;

    private Configuration.Broadcasting config;
    private ScheduledExecutorService executorService;

    private int lastOnlineCount = -1;

    public DefaultBroadcastingManager(final @NotNull IntSupplier onlineSupplier) {
        this.onlineSupplier = Objects.requireNonNull(onlineSupplier, "onlineSupplier must not be null");
    }

    @Override
    public void start(final @NotNull Configuration.Broadcasting configuration) {
        this.config = Objects.requireNonNull(configuration, "configuration must not be null");
        if (!this.config.enabled()) {
            throw new IllegalArgumentException("Status must be enabled!");
        }

        if (this.config.targets().isEmpty()) {
            LOGGER.warn("No broadcasting targets configured, online count will always be 0!");
        }

        final long interval = this.config.updatingInterval();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleWithFixedDelay(this::update, 0L, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
        if (this.config == null) {
            return;
        }
        for (final BroadcastingTarget target : this.config.targets()) {
            try {
                target.shutdown();
            } catch (final Exception e) {
                LOGGER.error("Failed to shutdown broadcasting target: {}", target, e);
            }
        }
    }

    private void update() {
        final int online;
        try {
            online = this.onlineSupplier.getAsInt();
        } catch (final Exception e) {
            LOGGER.error("Failed to fetch online count", e);
            return;
        }

        if (online < 0) {
            LOGGER.warn("Received invalid online count: {}", online);
            return;
        }

        if (online == this.lastOnlineCount && this.config.onlyOnChange()) {
            return;
        }
        this.lastOnlineCount = online;

        for (final BroadcastingTarget target : this.config.targets()) {
            try {
                target.broadcast(online);
            } catch (final Exception e) {
                LOGGER.error("Failed to broadcast online count on target: {}", target, e);
            }
        }
    }

}