package player.chops.managers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyManager {

    private static EconomyManager instance;
    private Economy economy;

    private EconomyManager() {}

    public static EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }

    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean deposit(UUID playerId, double amount) {
        if (economy == null) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        EconomyResponse resp = economy.depositPlayer(player, amount);
        return resp.transactionSuccess();
    }

    public boolean withdraw(UUID playerId, double amount) {
        if (economy == null) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        EconomyResponse resp = economy.withdrawPlayer(player, amount);
        return resp.transactionSuccess();
    }

    public boolean hasBalance(UUID playerId, double amount) {
        if (economy == null) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return economy.has(player, amount);
    }

    public double getBalance(UUID playerId) {
        if (economy == null) return 0;
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return economy.getBalance(player);
    }

    public String format(double amount) {
        if (economy == null) return "$" + amount;
        return economy.format(amount);
    }
}
