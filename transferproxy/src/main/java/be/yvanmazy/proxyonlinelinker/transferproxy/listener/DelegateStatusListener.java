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

package be.yvanmazy.proxyonlinelinker.transferproxy.listener;

import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.event.listener.StatusListener;
import net.transferproxy.api.event.status.StatusRequestEvent;
import net.transferproxy.api.status.StatusResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DelegateStatusListener implements StatusListener {

    private final OnlineManager onlineManager;

    public DelegateStatusListener(final @NotNull OnlineManager onlineManager) {
        this.onlineManager = Objects.requireNonNull(onlineManager, "statusManager must not be null");
    }

    @Override
    public void handle(final @NotNull StatusRequestEvent event) {
        StatusResponse response = event.getResponse();
        if (response == null) {
            response = TransferProxy.getInstance()
                    .getModuleManager()
                    .getStatusManager()
                    .buildDefaultResponse(event.getConnection().getProtocol());
        }
        event.setResponse(response.toBuilder().online(this.onlineManager.getOnlineCount()).build());
    }

}