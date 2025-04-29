package be.yvanmazy.proxyonlinelinker.common.config;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingMode;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
            return List.of();
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

        @Override
        public String toString() {
            return "Status{" + "enabled=" + this.enabled + '}';
        }

    }

}