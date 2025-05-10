package be.yvanmazy.proxyonlinelinker.common.broadcasting.target;

public interface BroadcastingTarget {

    void broadcast(final int online);

    void shutdown();

}