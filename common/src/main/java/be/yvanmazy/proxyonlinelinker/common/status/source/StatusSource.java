package be.yvanmazy.proxyonlinelinker.common.status.source;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface StatusSource {

    @CheckReturnValue
    int fetch();

    @Contract(pure = true)
    @NotNull StatusSourceType type();

}