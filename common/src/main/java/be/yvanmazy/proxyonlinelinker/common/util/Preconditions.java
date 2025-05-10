package be.yvanmazy.proxyonlinelinker.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Preconditions {

    private Preconditions() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    @Contract(pure = true)
    public static int requirePort(final int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        return port;
    }

    @Contract(pure = true)
    public static <T> T @NotNull [] requireNonNullEntries(final T[] array, final String name) {
        checkNotNull(array, name);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                throw new IllegalArgumentException(name + ": element at " + i + " must not be null");
            }
        }
        return array;
    }

    @Contract(pure = true)
    public static <T, I extends Iterable<T>> @NotNull I requireNonNullEntries(final I iterable, final String name) {
        checkNotNull(iterable, name);
        int i = 0;
        for (final T value : iterable) {
            if (value == null) {
                throw new IllegalArgumentException(name + ": element at " + i + " must not be null");
            }
            i++;
        }
        return iterable;
    }

    @Contract(pure = true)
    public static void checkNotNull(final Object object, final String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null");
        }
    }

    @Contract(pure = true)
    public static void checkRange(final long value, final long minValue, final long maxValue, final String name) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(name + " must be between " + minValue + " and " + maxValue);
        }
    }

    @Contract(pure = true)
    public static @NotNull String requireNonBlank(final String string, final String name) {
        checkNotNull(string, name);
        if (string.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return string;
    }

}