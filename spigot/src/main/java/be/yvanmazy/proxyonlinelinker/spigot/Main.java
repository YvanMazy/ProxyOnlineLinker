package be.yvanmazy.proxyonlinelinker.spigot;

import be.yvanmazy.proxyonlinelinker.common.ProxyOnlineLinker;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private ProxyOnlineLinker proxyOnlineLinker;

    @Override
    public void onEnable() {
        this.proxyOnlineLinker = new ProxyOnlineLinker("Spigot", Bukkit.getOnlinePlayers()::size, this::initReplacement);
        this.proxyOnlineLinker.onEnable(this.getDataFolder().toPath().resolve("config.yml"));
    }

    @Override
    public void onDisable() {
        if (this.proxyOnlineLinker != null) {
            this.proxyOnlineLinker.onDisable();
        }
    }

    private void initReplacement(final OnlineManager onlineManager, final ReplacementStrategy replacementStrategy) {
        throw new UnsupportedOperationException("Replacement strategies is currently not supported on Spigot.");
    }

}