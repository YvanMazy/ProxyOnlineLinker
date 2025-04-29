package be.yvanmazy.proxyonlinelinker.common.config;

import be.yvanmazy.proxyonlinelinker.common.broadcasting.BroadcastingMode;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import be.yvanmazy.proxyonlinelinker.common.util.StateValidator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface Configuration extends StateValidator {

    @Contract(pure = true)
    @NotNull General general();

    @Contract(pure = true)
    @NotNull Broadcasting broadcasting();

    @Contract(pure = true)
    @NotNull Status status();

    @Override
    default void validate() {
        this.general().validate();
        this.broadcasting().validate();
        this.status().validate();
    }

    interface General extends StateValidator {

        @Override
        default void validate() {
        }

    }

    interface Broadcasting extends StateValidator {

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        @NotNull BroadcastingMode mode();

        @Override
        default void validate() {
            Preconditions.checkNotNull(this.mode(), "mode");
        }

    }

    interface Status extends StateValidator {

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        @Range(from = -1L, to = Long.MAX_VALUE)
        long globalCacheExpiration();

        @Contract(pure = true)
        boolean requestOnDemand();

        @Contract(pure = true)
        @NotNull List<StatusSource> sources();

        @Override
        default void validate() {
            Preconditions.checkRange(this.globalCacheExpiration(), -1L, Long.MAX_VALUE, "globalCacheExpiration");
            Preconditions.checkNotNull(this.sources(), "sources");
        }

    }

}