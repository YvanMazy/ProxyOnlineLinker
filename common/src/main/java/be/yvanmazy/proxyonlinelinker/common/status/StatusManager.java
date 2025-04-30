package be.yvanmazy.proxyonlinelinker.common.status;

import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface StatusManager {

    void start(final @NotNull Configuration configuration);

    void stop();

    @CheckReturnValue
    int getOnlineCount();

}