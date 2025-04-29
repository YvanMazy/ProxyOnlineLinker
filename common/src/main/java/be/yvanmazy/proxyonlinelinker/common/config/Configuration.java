package be.yvanmazy.proxyonlinelinker.common.config;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingMode;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface Configuration {

    @Contract(pure = true)
    @NotNull General general();

    @Contract(pure = true)
    @NotNull Broadcasting broadcasting();

    @Contract(pure = true)
    @NotNull Status status();

    interface General {

    }

    interface Broadcasting {

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        @NotNull BroadcastingMode mode();

    }

    interface Status {

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        @Range(from = -1L, to = Long.MAX_VALUE)
        long globalCacheExpiration();

        @Contract(pure = true)
        boolean requestOnDemand();

        @Contract(pure = true)
        @NotNull List<StatusSource> sources();

    }

}