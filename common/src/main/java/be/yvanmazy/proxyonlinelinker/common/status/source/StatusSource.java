package be.yvanmazy.proxyonlinelinker.common.status.source;

import org.jetbrains.annotations.CheckReturnValue;

public interface StatusSource {

    @CheckReturnValue
    int fetch();

}