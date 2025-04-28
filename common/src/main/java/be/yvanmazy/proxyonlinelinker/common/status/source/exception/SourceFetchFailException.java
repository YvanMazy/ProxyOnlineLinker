package be.yvanmazy.proxyonlinelinker.common.status.source.exception;

public class SourceFetchFailException extends Exception {

    public SourceFetchFailException(final String message) {
        super(message);
    }

    public SourceFetchFailException(final String message, final Throwable cause) {
        super(message, cause);
    }

}