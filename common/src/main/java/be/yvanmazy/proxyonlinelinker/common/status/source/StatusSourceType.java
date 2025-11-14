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

package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.util.MapTypeAccessor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.function.Function;

public enum StatusSourceType {

    SELF(accessor -> new SelfSource()),
    PING(accessor -> {
        final String host = accessor.getString("host");
        final int port = accessor.getInt("port", 25565);
        final int timeout = accessor.getInt("timeout", 5000);
        final int protocol = accessor.getInt("protocol", 759); // (759 = 1.20.4)

        final Proxy proxy;
        final MapTypeAccessor proxyAccessor = accessor.getSubAccessor("proxy", MapTypeAccessor.EMPTY);
        if (proxyAccessor != MapTypeAccessor.EMPTY) {
            final String proxyType = proxyAccessor.getString("type", Proxy.Type.SOCKS.name());
            final Proxy.Type type = Proxy.Type.valueOf(proxyType.toUpperCase());
            final String proxyHost = proxyAccessor.getString("host");
            final int proxyPort = proxyAccessor.getInt("port");
            proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
        } else {
            proxy = null;
        }

        return new PingSource(host, port, timeout, protocol, proxy);
    }),
    REDIS(accessor -> {
        final String setKey = accessor.getString("set-key");
        return new RedisSource(setKey);
    });

    private final Function<MapTypeAccessor, StatusSource> factory;

    StatusSourceType(final Function<MapTypeAccessor, StatusSource> factory) {
        this.factory = factory;
    }

    public @NotNull StatusSource create(final @NotNull MapTypeAccessor accessor) {
        return this.factory.apply(Objects.requireNonNull(accessor, "accessor must not be null"));
    }

}