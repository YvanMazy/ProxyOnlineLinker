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