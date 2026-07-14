package player.chops.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import player.chops.gui.ShopGUI;
import player.chops.managers.ShopManager;
import player.chops.model.Shop;
import player.chops.utils.Utils;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!isCarpet(block.getType())) return;
        Shop shop = ShopManager.getInstance().getShopAt(block.getLocation());
        if (shop == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (shop.getOwnerId().equals(player.getUniqueId())) {
            ShopGUI.openShopConfigGUI(player, shop, 0);
        } else {
            ShopGUI.openShopBuyGUI(player, shop, 0);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Shop shop = ShopManager.getInstance().getShopAt(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Utils.colorize("&cProtegido! Use a GUI para deletar a loja."));
        }
    }

    private boolean isCarpet(Material type) {
        if (type == Material.MOSS_CARPET) return true;
        return type.name().endsWith("_CARPET");
    }
}
