package be.yvanmazy.proxyonlinelinker.common.config;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingMode;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.common.status.source.CacheLayerSource;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSourceType;
import be.yvanmazy.proxyonlinelinker.common.util.MapTypeAccessor;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DummyConfiguration implements Configuration {

    private General general;
    private Broadcasting broadcasting;
    private Status status;

    @Override
    public @NotNull General general() {
        return ensureLoaded(this.general);
    }

    @Override
    public @NotNull Broadcasting broadcasting() {
        return ensureLoaded(this.broadcasting);
    }

    @Override
    public @NotNull Status status() {
        return ensureLoaded(this.status);
    }

    public void setGeneral(final General general) {
        this.general = general;
    }

    public void setBroadcasting(final Broadcasting broadcasting) {
        this.broadcasting = broadcasting;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DummyConfiguration{" + "general=" + this.general + ", broadcasting=" + this.broadcasting + ", status=" + this.status + '}';
    }

    private static <T> @NotNull T ensureLoaded(final T value) {
        return Objects.requireNonNull(value, "This configuration is not initialized");
    }

    public static class General implements Configuration.General {

    }

    public static class Broadcasting implements Configuration.Broadcasting {

        private boolean enabled;
        private BroadcastingMode mode;

        @Override
        public boolean enabled() {
            return this.enabled;
        }

        @Override
        public @NotNull BroadcastingMode mode() {
            return this.mode;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public void setMode(final BroadcastingMode mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "Broadcasting{" + "enabled=" + this.enabled + ", mode=" + this.mode + '}';
        }

    }

    public static class Status implements Configuration.Status {

        private boolean enabled;
        private long globalCacheExpiration;
        private boolean requestOnDemand;
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

        public void setSources(final List<Map<String, Object>> sources) {
            final List<StatusSource> list = new ArrayList<>(sources.size());

            for (final Map<String, Object> source : sources) {
                final MapTypeAccessor accessor = new MapTypeAccessor(source);
                final String rawType = accessor.getString("type");
                final StatusSourceType type = StatusSourceType.valueOf(rawType.toUpperCase());

                StatusSource builtSource = type.create(accessor);

                final long cacheExpiration = accessor.getLong("cache-expiration", -1L);
                if (cacheExpiration > 0) {
                    builtSource = new CacheLayerSource(builtSource, cacheExpiration);
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

}