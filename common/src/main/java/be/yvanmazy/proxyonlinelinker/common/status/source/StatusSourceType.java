package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.util.MapTypeAccessor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.function.Function;

public enum StatusSourceType {

    PING(accessor -> {
        final String host = accessor.getString("host");
        final int port = accessor.getInt("port", 25565);
        final int timeout = accessor.getInt("timeout", 5000);

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

        return new PingSource(host, port, timeout, proxy);
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