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
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultOnlineManager implements OnlineManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOnlineManager.class);

    private Configuration.Status config;
    private ScheduledExecutorService executorService;

    private long lastUpdate;
    private int online;

    private final AtomicBoolean updating = new AtomicBoolean();

    @Override
    public void start(final @NotNull Configuration configuration) {
        this.config = Objects.requireNonNull(configuration, "configuration must not be null").status();
        if (!this.config.enabled()) {
            throw new IllegalArgumentException("Status must be enabled!");
        }

        if (this.config.sources().isEmpty()) {
            LOGGER.warn("No status sources configured, online count will always be 0!");
        }

        if (!this.config.requestOnDemand() || this.config.parallelRequestOnDemand()) {
            this.executorService = Executors.newSingleThreadScheduledExecutor();
        }

        if (!this.config.requestOnDemand()) {
            final long expiration = this.config.globalCacheExpiration();
            if (expiration <= 1) {
                throw new IllegalArgumentException("Global cache expiration must be > 1 when not requesting on demand!");
            }
            // TODO: Add an option to sleep scheduler after X seconds of inactivity
            this.executorService.scheduleWithFixedDelay(this::updateOnline, 0L, expiration, TimeUnit.MILLISECONDS);
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
        if (this.config.requestOnDemand() && this.processExpiration()) {
            if (this.config.parallelRequestOnDemand()) {
                if (this.updating.compareAndSet(false, true)) {
                    this.executorService.execute(this::parallelUpdateOnline);
                }
            } else {
                this.updateOnline();
            }
        }
        return this.online;
    }

    private void parallelUpdateOnline() {
        try {
            this.updateOnline();
        } finally {
            this.updating.set(false);
        }
    }

    private void updateOnline() {
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

    private boolean processExpiration() {
        if (this.config.globalCacheExpiration() <= 0) {
            return true;
        }
        final long now = System.currentTimeMillis();
        if (now - this.lastUpdate < this.config.globalCacheExpiration()) {
            return false;
        }
        this.lastUpdate = now;
        return true;
    }

    @Contract(pure = true)
    public int getCachedOnline() {
        return this.online;
    }

}