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

package be.yvanmazy.proxyonlinelinker.bungeecord;

import be.yvanmazy.proxyonlinelinker.bungeecord.listener.DelegateStatusListener;
import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import net.md_5.bungee.api.plugin.Plugin;

public final class Main extends Plugin {

    private ProxyOnlineLinker proxyOnlineLinker;

    @Override
    public void onEnable() {
        this.proxyOnlineLinker = new ProxyOnlineLinker("BungeeCord", () -> this.getProxy().getOnlineCount(), this::initReplacement);
        this.proxyOnlineLinker.onEnable(this.getDataFolder().toPath().resolve("config.yml"));
    }

    @Override
    public void onDisable() {
        if (this.proxyOnlineLinker != null) {
            this.proxyOnlineLinker.onDisable();
        }
    }

    private void initReplacement(final OnlineManager onlineManager, final ReplacementStrategy replacementStrategy) {
        if (replacementStrategy == ReplacementStrategy.DELEGATE) {
            this.getProxy().getPluginManager().registerListener(this, new DelegateStatusListener(onlineManager));
            return;
        }
        throw new UnsupportedOperationException("Replacement strategy " + replacementStrategy + " is not supported");
    }

}