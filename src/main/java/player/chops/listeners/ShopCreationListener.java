package player.chops.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import player.chops.PlayserShops;
import player.chops.managers.EconomyManager;
import player.chops.managers.ShopManager;
import player.chops.utils.Utils;

public class ShopCreationListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!isCarpet(block.getType())) return;

        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.SCAFFOLDING) return;

        if (ShopManager.getInstance().isShopLocation(block.getLocation())) return;

        int maxShops = PlayserShops.getInstance().getConfig().getInt("max-shops-per-player", 3);
        if (ShopManager.getInstance().getShopCount(player.getUniqueId()) >= maxShops) {
            event.setCancelled(true);
            player.sendMessage(Utils.colorize("&cYou already have the maximum number of shops (" + maxShops + ")!"));
            return;
        }

        double cost = PlayserShops.getInstance().getConfig().getDouble("shop-creation-cost", 1000.0);
        if (cost > 0 && !EconomyManager.getInstance().hasBalance(player.getUniqueId(), cost)) {
            event.setCancelled(true);
            player.sendMessage(Utils.colorize("&cYou need " + EconomyManager.getInstance().format(cost) + " to create a shop!"));
            return;
        }

        if (cost > 0) {
            EconomyManager.getInstance().withdraw(player.getUniqueId(), cost);
        }

        Location shopLoc = below.getLocation();
        ShopManager.getInstance().createShop(player.getUniqueId(), player.getName(), shopLoc);
        player.sendMessage(Utils.colorize("&aShop created! Use &e/lojinha &ato configure it."));
        if (cost > 0) {
            player.sendMessage(Utils.colorize("&7Paid " + EconomyManager.getInstance().format(cost) + " for shop creation."));
        }
    }

    private boolean isCarpet(Material type) {
        if (type == Material.MOSS_CARPET) return true;
        String name = type.name();
        return name.endsWith("_CARPET") && !name.contains("WOOL");
    }
}
