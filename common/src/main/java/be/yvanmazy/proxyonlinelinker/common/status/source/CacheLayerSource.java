package be.yvanmazy.proxyonlinelinker.common.status.source;

import be.yvanmazy.proxyonlinelinker.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class CacheLayerSource implements StatusSource {

    private final StatusSource delegate;
    private final long expirationDelay;

    private int lastFetched;
    private long lastUpdate;

    public CacheLayerSource(final @NotNull StatusSource delegate, final @Range(from = 1L, to = Long.MAX_VALUE) long expirationDelay) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        Preconditions.checkRange(expirationDelay, 1L, Long.MAX_VALUE, "expirationDelay");
        this.expirationDelay = expirationDelay;
        // TODO: Add an option to don't cache failures
    }

    @Override
    public int fetch() {
        final long now = System.currentTimeMillis();
        if (now - this.lastUpdate < this.expirationDelay) {
            return this.lastFetched;
        }
        this.lastFetched = this.delegate.fetch();
        this.lastUpdate = now;
        return this.lastFetched;
    }

    @Override
    public @NotNull StatusSourceType type() {
        return this.delegate.type();
    }

}