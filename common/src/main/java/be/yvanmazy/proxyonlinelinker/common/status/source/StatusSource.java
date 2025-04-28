package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.status.source.exception.SourceFetchFailException;
import org.jetbrains.annotations.CheckReturnValue;

// TODO: Add cache layer source
public interface StatusSource {

    @CheckReturnValue
    int fetch() throws SourceFetchFailException;

}