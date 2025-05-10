package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.redis.JedisProvider;
import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;

public class RedisSource implements StatusSource {

    private final String setKey;

    public RedisSource(final @NotNull String setKey) {
        this.setKey = Preconditions.requireNonBlank(setKey, "setKey");
    }

    @Override
    public int fetch() {
        int total = 0;
        for (final String string : JedisProvider.INSTANCE.get().getJedis().hvals(this.setKey)) {
            try {
                total += Integer.parseInt(string);
            } catch (final NumberFormatException ignored) {
            }
        }
        return total;
    }

    @Override
    public @NotNull StatusSourceType type() {
        return StatusSourceType.REDIS;
    }

}