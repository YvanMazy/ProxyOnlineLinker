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