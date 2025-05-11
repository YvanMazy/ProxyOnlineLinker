/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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