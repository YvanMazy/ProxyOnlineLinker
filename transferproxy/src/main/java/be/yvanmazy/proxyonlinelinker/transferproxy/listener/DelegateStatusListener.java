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