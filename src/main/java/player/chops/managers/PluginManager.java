package player.chops.managers;

import player.chops.PlayserShops;

public class PluginManager {
    private static PluginManager instance;

    private PluginManager() {}

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void initialize() {
        if (!EconomyManager.getInstance().setup()) {
            PlayserShops.getInstance().getLogger().severe("Vault economy not found! Disabling plugin.");
            PlayserShops.getInstance().getServer().getPluginManager().disablePlugin(PlayserShops.getInstance());
            return;
        }
        PlayserShops.getInstance().getLogger().info("Vault economy hooked successfully!");

        ShopManager.getInstance().initialize();
    }

    public void shutdown() {
        ShopManager.getInstance().shutdown();
    }
}
