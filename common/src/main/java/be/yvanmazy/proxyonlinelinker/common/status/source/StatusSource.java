package be.yvanmazy.proxyonlinelinker.common.status.source;

import org.jetbrains.annotations.CheckReturnValue;

// TODO: Add cache layer source
public interface StatusSource {

    @CheckReturnValue
    int fetch();

}