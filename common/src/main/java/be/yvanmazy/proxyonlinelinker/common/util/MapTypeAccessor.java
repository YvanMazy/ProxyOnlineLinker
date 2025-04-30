package be.yvanmazy.proxyonlinelinker.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class MapTypeAccessor {

    public static final MapTypeAccessor EMPTY = new MapTypeAccessor(Map.of());

    private final Map<String, Object> map;

    public MapTypeAccessor(final @NotNull Map<String, Object> map) {
        this.map = Objects.requireNonNull(map, "map must not be null");
    }

    public @Nullable Object getRawObject(final @NotNull String key) {
        return this.map.get(key);
    }

    public @NotNull Object getObject(final @NotNull String key) {
        final Object value = this.getRawObject(key);
        if (value == null) {
            throw new NullPointerException("No value found for key " + key);
        }
        return value;
    }

    public @NotNull String getString(final @NotNull String key) {
        if (this.getObject(key) instanceof final String string) {
            return string;
        }
        throw new ClassCastException("Value for key " + key + " is not a String");
    }

    public @NotNull String getString(final @NotNull String key, final @NotNull String defaultValue) {
        if (this.getRawObject(key) instanceof final String string) {
            return string;
        }
        return defaultValue;
    }

    public double getDouble(final @NotNull String key) {
        if (this.getObject(key) instanceof final Number number) {
            return number.doubleValue();
        }
        throw new ClassCastException("Value for key " + key + " is not a Number");
    }

    public double getDouble(final @NotNull String key, final double defaultValue) {
        if (this.getRawObject(key) instanceof final Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    public int getInt(final @NotNull String key) {
        if (this.getObject(key) instanceof final Number number) {
            return number.intValue();
        }
        throw new ClassCastException("Value for key " + key + " is not a Number");
    }

    public int getInt(final @NotNull String key, final int defaultValue) {
        if (this.getRawObject(key) instanceof final Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    public long getLong(final @NotNull String key) {
        if (this.getObject(key) instanceof final Number number) {
            return number.longValue();
        }
        throw new ClassCastException("Value for key " + key + " is not a Number");
    }

    public long getLong(final @NotNull String key, final long defaultValue) {
        if (this.getRawObject(key) instanceof final Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public @NotNull MapTypeAccessor getSubAccessor(final @NotNull String key) {
        if (this.getRawObject(key) instanceof final Map<?, ?> rawMap && isCorrectlyTyped(rawMap)) {
            return new MapTypeAccessor((Map<String, Object>) rawMap);
        }
        throw new ClassCastException("Value for key " + key + " is not a Map");
    }

    @SuppressWarnings("unchecked")
    public @NotNull MapTypeAccessor getSubAccessor(final @NotNull String key, final @NotNull MapTypeAccessor defaultValue) {
        if (this.getRawObject(key) instanceof final Map<?, ?> rawMap && isCorrectlyTyped(rawMap)) {
            return new MapTypeAccessor((Map<String, Object>) rawMap);
        }
        return defaultValue;
    }

    private static boolean isCorrectlyTyped(final Map<?, ?> map) {
        if (map.isEmpty()) {
            return true;
        }
        return map.keySet().iterator().next() instanceof String;
    }

}