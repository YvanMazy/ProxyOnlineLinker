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

package be.yvanmazy.proxyonlinelinker.common.redis;

import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.ClusterConnectionProvider;
import redis.clients.jedis.providers.ConnectionProvider;
import redis.clients.jedis.providers.PooledConnectionProvider;
import redis.clients.jedis.providers.SentineledConnectionProvider;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultJedisProvider implements JedisProvider {

    private final UnifiedJedis unifiedJedis;

    @SuppressWarnings("resource")
    public DefaultJedisProvider(final Configuration.Redis configuration) {
        final JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .user(configuration.username().isBlank() ? null : configuration.username())
                .password(configuration.password().isBlank() ? null : configuration.password())
                .database(configuration.database())
                .timeoutMillis(configuration.timeoutMillis())
                .build();

        final ConnectionProvider connectionProvider = switch (configuration.mode()) {
            case STANDALONE -> {
                final Configuration.Redis.Standalone standalone = configuration.standalone();
                final HostAndPort hostAndPort = new HostAndPort(standalone.host(), standalone.port());
                yield new PooledConnectionProvider(hostAndPort, clientConfig);
            }
            case SENTINEL -> {
                final Configuration.Redis.Sentinel sentinel = configuration.sentinel();
                final Set<HostAndPort> sentinels = sentinel.sentinels().stream().map(HostAndPort::from).collect(Collectors.toSet());
                yield new SentineledConnectionProvider(sentinel.masterName(), clientConfig, sentinels, clientConfig);
            }
            case CLUSTER -> {
                final Configuration.Redis.Cluster cluster = configuration.cluster();
                final Set<HostAndPort> nodes = cluster.clusterNodes().stream().map(HostAndPort::from).collect(Collectors.toSet());
                yield new ClusterConnectionProvider(nodes, clientConfig);
            }
        };

        final int maxAttempts = configuration.maxAttempts();
        final Duration maxTotalRetriesDuration = Duration.ofMillis(configuration.maxTotalRetriesDuration());

        this.unifiedJedis = new UnifiedJedis(connectionProvider, maxAttempts, maxTotalRetriesDuration);

        if (this.unifiedJedis.ping().isBlank()) {
            throw new IllegalStateException("Redis ping request failed!");
        }
    }

    @Override
    public @NotNull UnifiedJedis getJedis() {
        return this.unifiedJedis;
    }

    @Override
    public void stop() {
        this.unifiedJedis.close();
    }

}