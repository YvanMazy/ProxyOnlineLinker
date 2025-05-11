package be.yvanmazy.proxyonlinelinker.common.broadcasting.target;

import be.yvanmazy.proxyonlinelinker.common.redis.JedisProvider;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.AbstractTransaction;
import redis.clients.jedis.UnifiedJedis;

import java.util.Objects;

public class RedisBroadcasting implements BroadcastingTarget {

    private final String serverId;
    private final String setKey;
    private final int expireSeconds;

    public RedisBroadcasting(final @NotNull String serverId, final @NotNull String setKey, final int expireSeconds) {
        this.serverId = Objects.requireNonNull(serverId, "serverId must not be null");
        this.setKey = Objects.requireNonNull(setKey, "setKey must not be null");
        this.expireSeconds = Math.max(expireSeconds, 0);
    }

    @Override
    public void broadcast(final int online) {
        final UnifiedJedis jedis = JedisProvider.INSTANCE.get().getJedis();
        try (final AbstractTransaction transaction = jedis.multi()) {
            transaction.hset(this.setKey, this.serverId, String.valueOf(online));
            if (this.expireSeconds > 0) {
                transaction.hexpire(this.setKey, this.expireSeconds, this.serverId);
            }
            transaction.exec();
        }
    }

    @Override
    public void shutdown() {
        JedisProvider.INSTANCE.get().getJedis().hdel(this.setKey, this.serverId);
    }

    @Override
    public @NotNull BroadcastingTargetType type() {
        return BroadcastingTargetType.REDIS;
    }

}