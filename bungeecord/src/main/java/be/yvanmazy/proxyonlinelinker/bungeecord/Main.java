package be.yvanmazy.proxyonlinelinker.bungeecord;

import be.yvanmazy.proxyonlinelinker.bungeecord.listener.DelegateStatusListener;
import be.yvanmazy.proxyonlinelinker.common.config.Configuration;
import be.yvanmazy.proxyonlinelinker.common.config.ConfigurationReader;
import be.yvanmazy.proxyonlinelinker.common.status.DefaultOnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.OnlineManager;
import be.yvanmazy.proxyonlinelinker.common.status.replacement.ReplacementStrategy;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public final class Main extends Plugin {

    private Configuration configuration;

    private OnlineManager onlineManager;

    @Override
    public void onEnable() {
        final Path path = this.getDataFolder().toPath().resolve("config.yml");
        try {
            this.configuration = ConfigurationReader.read(path);
        } catch (final IOException exception) {
            this.getLogger().log(Level.SEVERE, "Failed to read configuration", exception);
            return;
        }

        if (this.configuration.status().enabled()) {
            this.initStatusHandling();
        }
    }

    @Override
    public void onDisable() {
        if (this.onlineManager != null) {
            this.onlineManager.stop();
        }
    }

    private void initStatusHandling() {
        this.onlineManager = new DefaultOnlineManager();
        this.onlineManager.start(this.configuration);

        final Configuration.Status.Replacement replacement = this.configuration.status().replacement();

        if (replacement.strategy() == ReplacementStrategy.NONE) {
            return;
        }

        if (replacement.strategy() == ReplacementStrategy.DELEGATE) {
            this.getProxy().getPluginManager().registerListener(this, new DelegateStatusListener(this.onlineManager));
        } else {
            throw new UnsupportedOperationException("Replacement strategy " + replacement.strategy() + " is not supported");
        }
    }

}