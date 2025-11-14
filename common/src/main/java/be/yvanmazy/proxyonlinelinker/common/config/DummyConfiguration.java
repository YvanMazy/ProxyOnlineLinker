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

package be.yvanmazy.proxyonlinelinker.common.config;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.target.BroadcastingTarget;
import be.yvanmazy.proxyonlinelinker.common.broadcasting.target.BroadcastingTargetType;
import be.yvanmazy.proxyonlinelinker.common.redis.RedisMode;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.common.status.source.CacheLayerSource;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSourceType;
import be.yvanmazy.proxyonlinelinker.common.util.MapTypeAccessor;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DummyConfiguration implements Configuration {

    private Broadcasting broadcasting;
    private Status status;
    private Redis redis;

    @Override
    public @NotNull Broadcasting broadcasting() {
        return ensureLoaded(this.broadcasting);
    }

    @Override
    public @NotNull Status status() {
        return ensureLoaded(this.status);
    }

    @Override
    public @NotNull Redis redis() {
        return ensureLoaded(this.redis);
    }

    public void setBroadcasting(final Broadcasting broadcasting) {
        this.broadcasting = broadcasting;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public void setRedis(final Redis redis) {
        this.redis = redis;
    }

    @Override
    public String toString() {
        return "DummyConfiguration{" + "broadcasting=" + this.broadcasting + ", status=" + this.status + ", redis=" + this.redis + '}';
    }

    private static <T> @NotNull T ensureLoaded(final T value) {
        return Objects.requireNonNull(value, "This configuration is not initialized");
    }

    public static class Broadcasting implements Configuration.Broadcasting {

        private boolean enabled;
        private boolean onlyOnChange;
        private long updatingInterval;
        private List<BroadcastingTarget> targets;

        @Override
        public boolean enabled() {
            return this.enabled;
        }

        @Override
        public boolean onlyOnChange() {
            return this.onlyOnChange;
        }

        @Override
        public long updatingInterval() {
            return this.updatingInterval;
        }

        @Override
        public @NotNull List<BroadcastingTarget> targets() {
            return this.targets;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public void setOnlyOnChange(final boolean onlyOnChange) {
            this.onlyOnChange = onlyOnChange;
        }

        public void setUpdatingInterval(final long updatingInterval) {
            this.updatingInterval = updatingInterval;
        }

        public void setTargets(final List<Map<String, Object>> targets) {
            final List<BroadcastingTarget> list = new ArrayList<>(targets.size());

            for (final Map<String, Object> source : targets) {
                final MapTypeAccessor accessor = new MapTypeAccessor(source);
                final String rawType = accessor.getString("type");
                final BroadcastingTargetType type = BroadcastingTargetType.valueOf(rawType.toUpperCase());

                final BroadcastingTarget builtTarget = type.create(accessor);

                list.add(builtTarget);
            }

            this.targets = Collections.unmodifiableList(list);
        }

        public void setRawTargets(final List<BroadcastingTarget> targets) {
            this.targets = targets;
        }

        @Override
        public String toString() {
            return "Broadcasting{" + "enabled=" + this.enabled + ", onlyOnUpdate=" + this.onlyOnChange + ", updatingInterval=" +
                    this.updatingInterval + ", targets=" + this.targets + '}';
        }

    }

    public static class Status implements Configuration.Status {

        private boolean enabled;
        private long globalCacheExpiration;
        private boolean requestOnDemand;
        private boolean parallelRequestOnDemand;
        private List<StatusSource> sources;
        private Replacement replacement;

        @Override
        public boolean enabled() {
            return this.enabled;
        }

        @Override
        public long globalCacheExpiration() {
            return this.globalCacheExpiration;
        }

        @Override
        public boolean requestOnDemand() {
            return this.requestOnDemand;
        }

        @Override
        public boolean parallelRequestOnDemand() {
            return this.parallelRequestOnDemand;
        }

        @Override
        public @NotNull List<StatusSource> sources() {
            return this.sources;
        }

        @Override
        public Configuration.Status.@NotNull Replacement replacement() {
            return this.replacement;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public void setGlobalCacheExpiration(final long globalCacheExpiration) {
            this.globalCacheExpiration = globalCacheExpiration;
        }

        public void setRequestOnDemand(final boolean requestOnDemand) {
            this.requestOnDemand = requestOnDemand;
        }

        public void setParallelRequestOnDemand(final boolean parallelRequestOnDemand) {
            this.parallelRequestOnDemand = parallelRequestOnDemand;
        }

        public void setSources(final List<Map<String, Object>> sources) {
            final List<StatusSource> list = new ArrayList<>(sources.size());

            for (final Map<String, Object> source : sources) {
                final MapTypeAccessor accessor = new MapTypeAccessor(source);
                final String rawType = accessor.getString("type");
                final StatusSourceType type = StatusSourceType.valueOf(rawType.toUpperCase());

                StatusSource builtSource = type.create(accessor);

                final long cacheExpiration = accessor.getLong("cache-expiration", -1L);
                if (cacheExpiration > 0) {
                    final boolean cacheFailure = accessor.getBoolean("cache-failure", false);
                    builtSource = new CacheLayerSource(builtSource, cacheExpiration, cacheFailure);
                }

                list.add(builtSource);
            }

            this.sources = Collections.unmodifiableList(list);
        }

        public void setRawSources(final @NotNull List<StatusSource> sources) {
            this.sources = Preconditions.requireNonNullEntries(sources, "sources");
        }

        public void setReplacement(final Replacement replacement) {
            this.replacement = replacement;
        }

        @Override
        public String toString() {
            return "Status{" + "enabled=" + this.enabled + ", globalCacheExpiration=" + this.globalCacheExpiration + ", requestOnDemand=" +
                    this.requestOnDemand + ", sources=" + this.sources + '}';
        }

        public static final class Replacement implements Configuration.Status.Replacement {

            private ReplacementStrategy strategy;

            @Override
            public @NotNull ReplacementStrategy strategy() {
                return this.strategy;
            }

            public void setStrategy(final ReplacementStrategy strategy) {
                this.strategy = strategy;
            }

            @Override
            public String toString() {
                return "Replacement{" + "strategy=" + this.strategy + '}';
            }

        }

    }

    public static class Redis implements Configuration.Redis {

        private RedisMode mode;
        private String username;
        private String password;
        private int database;
        private int timeoutMillis;
        private int maxAttempts;
        private long maxTotalRetriesDuration;
        private Standalone standalone;
        private Sentinel sentinel;
        private Cluster cluster;

        @Override
        public @NotNull RedisMode mode() {
            return this.mode;
        }

        @Override
        public @NotNull String username() {
            return this.username;
        }

        @Override
        public @NotNull String password() {
            return this.password;
        }

        @Override
        public int database() {
            return this.database;
        }

        @Override
        public int timeoutMillis() {
            return this.timeoutMillis;
        }

        @Override
        public int maxAttempts() {
            return this.maxAttempts;
        }

        @Override
        public long maxTotalRetriesDuration() {
            return this.maxTotalRetriesDuration;
        }

        @Override
        public Configuration.Redis.@NotNull Standalone standalone() {
            return this.standalone;
        }

        @Override
        public Configuration.Redis.@NotNull Sentinel sentinel() {
            return this.sentinel;
        }

        @Override
        public Configuration.Redis.@NotNull Cluster cluster() {
            return this.cluster;
        }

        public void setMode(final RedisMode mode) {
            this.mode = mode;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public void setDatabase(final int database) {
            this.database = database;
        }

        public void setTimeoutMillis(final int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        public void setMaxAttempts(final int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public void setMaxTotalRetriesDuration(final long maxTotalRetriesDuration) {
            this.maxTotalRetriesDuration = maxTotalRetriesDuration;
        }

        public void setStandalone(final Standalone standalone) {
            this.standalone = standalone;
        }

        public void setSentinel(final Sentinel sentinel) {
            this.sentinel = sentinel;
        }

        public void setCluster(final Cluster cluster) {
            this.cluster = cluster;
        }

        @Override
        public String toString() {
            return "Redis{" + "mode=" + this.mode + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' +
                    ", database=" + this.database + ", timeoutMillis=" + this.timeoutMillis + ", maxAttempts=" + this.maxAttempts +
                    ", maxTotalRetriesDuration=" + this.maxTotalRetriesDuration + ", standalone=" + this.standalone + ", sentinel=" +
                    this.sentinel + ", cluster=" + this.cluster + '}';
        }

        public static class Standalone implements Configuration.Redis.Standalone {

            private String host;
            private int port;

            @Override
            public @NotNull String host() {
                return this.host;
            }

            @Override
            public int port() {
                return this.port;
            }

            public void setHost(final String host) {
                this.host = host;
            }

            public void setPort(final int port) {
                this.port = port;
            }

            @Override
            public String toString() {
                return "Standalone{" + "host='" + this.host + '\'' + ", port=" + this.port + '}';
            }

        }

        public static class Sentinel implements Configuration.Redis.Sentinel {

            private String masterName;
            private List<String> sentinels;

            @Override
            public @NotNull String masterName() {
                return this.masterName;
            }

            @Override
            public @NotNull List<String> sentinels() {
                return this.sentinels;
            }

            public void setMasterName(final String masterName) {
                this.masterName = masterName;
            }

            public void setSentinels(final List<String> sentinels) {
                this.sentinels = sentinels;
            }

            @Override
            public String toString() {
                return "Sentinel{" + "masterName='" + this.masterName + '\'' + ", sentinels=" + this.sentinels + '}';
            }

        }

        public static class Cluster implements Configuration.Redis.Cluster {

            private List<String> clusterNodes;

            @Override
            public @NotNull List<String> clusterNodes() {
                return this.clusterNodes;
            }

            public void setClusterNodes(final List<String> clusterNodes) {
                this.clusterNodes = clusterNodes;
            }

            @Override
            public String toString() {
                return "Cluster{" + "clusterNodes=" + this.clusterNodes + '}';
            }

        }

    }

}