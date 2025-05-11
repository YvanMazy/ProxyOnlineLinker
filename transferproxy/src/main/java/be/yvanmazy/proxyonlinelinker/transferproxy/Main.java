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

package be.yvanmazy.proxyonlinelinker.transferproxy;

import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import be.yvanmazy.proxyonlinelinker.transferproxy.listener.DelegateStatusListener;
import io.netty.channel.group.ChannelGroup;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.StatusListener;
import net.transferproxy.api.network.NetworkServer;
import net.transferproxy.api.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Main implements Plugin {

    private ProxyOnlineLinker proxyOnlineLinker;

    @Override
    public void onEnable() {
        this.proxyOnlineLinker = new ProxyOnlineLinker("TransferProxy", this::fetchOnline, this::initReplacement);
        this.proxyOnlineLinker.onEnable(this.getConfigPath());
    }

    @Override
    public void onDisable() {
        if (this.proxyOnlineLinker != null) {
            this.proxyOnlineLinker.onDisable();
        }
    }

    private int fetchOnline() {
        final NetworkServer networkServer = TransferProxy.getInstance().getNetworkServer();
        if (networkServer == null) {
            return 0;
        }
        final ChannelGroup group = networkServer.getGroup();
        if (group == null) {
            return 0;
        }
        return group.size();
    }

    private void initReplacement(final OnlineManager onlineManager, final ReplacementStrategy replacementStrategy) {
        if (replacementStrategy == ReplacementStrategy.DELEGATE) {
            this.getEventManager().<StatusListener>addListener(EventType.STATUS, new DelegateStatusListener(onlineManager));
            return;
        }
        throw new UnsupportedOperationException("Replacement strategy " + replacementStrategy + " is not supported");
    }

    @Contract(pure = true)
    public @NotNull ProxyOnlineLinker getProxyOnlineLinker() {
        return this.proxyOnlineLinker;
    }

}