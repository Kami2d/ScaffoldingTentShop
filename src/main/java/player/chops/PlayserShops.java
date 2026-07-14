package player.chops;

import org.bukkit.plugin.java.JavaPlugin;

import player.chops.command.ShopCommand;
import player.chops.gui.AddProductGUI;
import player.chops.gui.ShopGUI;
import player.chops.listeners.PlayerListener;
import player.chops.listeners.ShopCreationListener;
import player.chops.managers.PluginManager;

public class PlayserShops extends JavaPlugin {

    private static PlayserShops instance;

    public static PlayserShops getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        PluginManager.getInstance().initialize();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new ShopCreationListener(), this);
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);
        getServer().getPluginManager().registerEvents(new AddProductGUI(), this);

        getCommand("lojinha").setExecutor(new ShopCommand());

        getLogger().info("=============================================");
        getLogger().info("  ___ScaffoldingTentShop___");
        getLogger().info("  Powered by Kame");
        getLogger().info("  Discord: code.tg");
        getLogger().info("=============================================");
    }

    @Override
    public void onDisable() {
        PluginManager.getInstance().shutdown();
        getLogger().info(getDescription().getName() + " has been disabled!");
    }
}
