package be.yvanmazy.proxyonlinelinker.common.util;

public final class Preconditions {

    private Preconditions() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static int requirePort(final int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        return port;
    }

}