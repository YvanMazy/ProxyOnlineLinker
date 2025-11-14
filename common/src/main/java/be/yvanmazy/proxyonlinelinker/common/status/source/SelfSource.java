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

import be.yvanmazy.proxyonlinelinker.common.InitializableElement;
import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntSupplier;

public class SelfSource implements StatusSource, InitializableElement {

    private IntSupplier onlineSupplier;

    public SelfSource(final @NotNull IntSupplier onlineSupplier) {
        this.onlineSupplier = Objects.requireNonNull(onlineSupplier, "onlineSupplier must not be null");
    }

    public SelfSource() {
    }

    @Override
    public void init(final @NotNull ProxyOnlineLinker proxyOnlineLinker) {
        this.onlineSupplier = proxyOnlineLinker.getOnlineSupplier();
    }

    @Override
    public int fetch() {
        return this.onlineSupplier.getAsInt();
    }

    @Override
    public @NotNull StatusSourceType type() {
        return StatusSourceType.SELF;
    }

}