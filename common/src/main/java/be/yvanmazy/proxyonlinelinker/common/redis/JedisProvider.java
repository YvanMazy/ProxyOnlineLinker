package be.yvanmazy.proxyonlinelinker.common.redis;

import be.yvanmazy.proxyonlinelinker.common.util.Constant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.UnifiedJedis;

public interface JedisProvider {

    Constant<JedisProvider> INSTANCE = new Constant<>();

    @Contract(pure = true)
    @NotNull UnifiedJedis getJedis();

    void stop();

}