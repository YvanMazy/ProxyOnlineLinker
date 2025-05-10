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