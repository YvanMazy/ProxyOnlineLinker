package be.yvanmazy.proxyonlinelinker.common.broadcasting.target;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface BroadcastingTarget {

    void broadcast(final int online);

    void shutdown();

    @Contract(pure = true)
    @NotNull BroadcastingTargetType type();

}