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
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSource;
import be.yvanmazy.proxyonlinelinker.common.status.source.StatusSourceType;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import be.yvanmazy.proxyonlinelinker.common.util.StateValidator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface Configuration extends StateValidator {

    @Contract(pure = true)
    @NotNull Broadcasting broadcasting();

    @Contract(pure = true)
    @NotNull Status status();

    @Contract(pure = true)
    @NotNull Redis redis();

    @Override
    default void validate() {
        this.broadcasting().validate();
        this.status().validate();
        this.redis().validate();
    }

    default boolean needRedis() {
        return (this.broadcasting().enabled() &&
                this.broadcasting().targets().stream().map(BroadcastingTarget::type).anyMatch(t -> t == BroadcastingTargetType.REDIS)) ||
                (this.status().enabled() &&
                        this.status().sources().stream().map(StatusSource::type).anyMatch(t -> t == StatusSourceType.REDIS));
    }

    interface Broadcasting extends StateValidator {

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        boolean onlyOnChange();

        @Contract(pure = true)
        long updatingInterval();

        @Contract(pure = true)
        @NotNull List<BroadcastingTarget> targets();

        @Override
        default void validate() {
            Preconditions.checkRange(this.updatingInterval(), 0L, Long.MAX_VALUE, "updatingInterval");
            Preconditions.requireNonNullEntries(this.targets(), "targets");
        }

    }

    interface Status extends StateValidator {

        // TODO: Add fallback strategy

        @Contract(pure = true)
        boolean enabled();

        @Contract(pure = true)
        @Range(from = -1L, to = Long.MAX_VALUE)
        long globalCacheExpiration();

        @Contract(pure = true)
        boolean requestOnDemand();

        @Contract(pure = true)
        @NotNull List<StatusSource> sources();

        @Contract(pure = true)
        @NotNull Replacement replacement();

        @Override
        default void validate() {
            Preconditions.requireNonNullEntries(this.sources(), "sources");
            this.replacement().validate();
        }

        interface Replacement extends StateValidator {

            @Contract(pure = true)
            @NotNull ReplacementStrategy strategy();

            @Override
            default void validate() {
                Preconditions.checkNotNull(this.strategy(), "strategy");
            }

        }

    }

    interface Redis extends StateValidator {

        @Contract(pure = true)
        @NotNull RedisMode mode();

        @Contract(pure = true)
        @NotNull String username();

        @Contract(pure = true)
        @NotNull String password();

        @Contract(pure = true)
        int database();

        @Contract(pure = true)
        int timeoutMillis();

        @Contract(pure = true)
        int maxAttempts();

        @Contract(pure = true)
        long maxTotalRetriesDuration();

        @Contract(pure = true)
        @NotNull Standalone standalone();

        @Contract(pure = true)
        @NotNull Sentinel sentinel();

        @Contract(pure = true)
        @NotNull Cluster cluster();

        @Override
        default void validate() {
            Preconditions.checkNotNull(this.mode(), "mode");
            Preconditions.checkNotNull(this.username(), "username");
            Preconditions.checkNotNull(this.password(), "password");
            this.standalone().validate();
            this.sentinel().validate();
            this.cluster().validate();
        }

        interface Standalone extends StateValidator {

            @Contract(pure = true)
            @NotNull String host();

            @Contract(pure = true)
            int port();

            @Override
            default void validate() {
                Preconditions.checkNotNull(this.host(), "host");
                Preconditions.requirePort(this.port());
            }

        }

        interface Sentinel extends StateValidator {

            @Contract(pure = true)
            @NotNull String masterName();

            @Contract(pure = true)
            @NotNull List<String> sentinels();

            @Override
            default void validate() {
                Preconditions.checkNotNull(this.masterName(), "masterName");
                Preconditions.requireNonNullEntries(this.sentinels(), "sentinels");
            }

        }

        interface Cluster extends StateValidator {

            @Contract(pure = true)
            @NotNull List<String> clusterNodes();

            @Override
            default void validate() {
                Preconditions.requireNonNullEntries(this.clusterNodes(), "clusterNodes");
            }

        }


    }

}