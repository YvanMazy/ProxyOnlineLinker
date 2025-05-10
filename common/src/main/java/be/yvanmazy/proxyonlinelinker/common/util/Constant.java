package be.yvanmazy.proxyonlinelinker.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Constant<T> {

    private T value;

    @Contract(pure = true)
    public @NotNull T get() {
        final T value = this.value;
        if (value == null) {
            throw new IllegalStateException("This constant is not set");
        }
        return value;
    }

    public void set(final @NotNull T value) {
        if (this.value != null) {
            throw new IllegalStateException("This constant is already set");
        }
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    @Contract(pure = true)
    public boolean isDefined() {
        return this.value != null;
    }

}