package be.yvanmazy.proxyonlinelinker.common.broadcasting;

import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import org.jetbrains.annotations.NotNull;

public interface BroadcastingManager {

    void start(final @NotNull Configuration.Broadcasting configuration);

    void stop();

}