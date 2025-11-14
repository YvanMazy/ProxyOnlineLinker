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

import be.yvanmazy.proxyonlinelinker.common.InitializableElement;
import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import be.yvanmazy.proxyonlinelinker.common.redis.JedisProvider;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.AbstractTransaction;
import redis.clients.jedis.UnifiedJedis;

import java.util.Objects;

public class RedisBroadcasting implements BroadcastingTarget, InitializableElement {

    private final String serverId;
    private final String setKey;
    private final int expireSeconds;

    private JedisProvider jedisProvider;

    public RedisBroadcasting(final @NotNull String serverId, final @NotNull String setKey, final int expireSeconds) {
        this.serverId = Objects.requireNonNull(serverId, "serverId must not be null");
        this.setKey = Objects.requireNonNull(setKey, "setKey must not be null");
        this.expireSeconds = Math.max(expireSeconds, 0);
    }

    @Override
    public void init(final @NotNull ProxyOnlineLinker proxyOnlineLinker) {
        this.jedisProvider = proxyOnlineLinker.getSafeJedisProvider();
    }

    @Override
    public void broadcast(final int online) {
        final UnifiedJedis jedis = this.jedisProvider.getJedis();
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
        this.jedisProvider.getJedis().hdel(this.setKey, this.serverId);
    }

    @Override
    public @NotNull BroadcastingTargetType type() {
        return BroadcastingTargetType.REDIS;
    }

}