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

package be.yvanmazy.proxyonlinelinker.common.broadcasting.target;

import be.yvanmazy.proxyonlinelinker.common.util.MapTypeAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

// TODO: Add other broadcasting modes
public enum BroadcastingTargetType {

    REDIS(accessor -> {
        final String serverId = accessor.getString("server-id");
        final String setKey = accessor.getString("set-key");
        final int expireSeconds = accessor.getInt("expire-seconds");
        return new RedisBroadcasting(serverId, setKey, expireSeconds);
    });

    private final Function<MapTypeAccessor, BroadcastingTarget> factory;

    BroadcastingTargetType(final Function<MapTypeAccessor, BroadcastingTarget> factory) {
        this.factory = factory;
    }

    public @NotNull BroadcastingTarget create(final @NotNull MapTypeAccessor accessor) {
        return this.factory.apply(Objects.requireNonNull(accessor, "accessor must not be null"));
    }
}