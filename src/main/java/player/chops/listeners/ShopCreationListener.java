package player.chops.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import player.chops.PlayserShops;
import player.chops.managers.EconomyManager;
import player.chops.managers.ShopManager;
import player.chops.utils.Utils;

public class ShopCreationListener implements Listener {

    private static final Map<UUID, PendingCreation> pendingCreations = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 30000;

    private static class PendingCreation {
        final Location scaffoldingLocation;
        final long timestamp;

        PendingCreation(Location scaffoldingLocation) {
            this.scaffoldingLocation = scaffoldingLocation;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();

        Material creationItem = Material.matchMaterial(
            PlayserShops.getInstance().getConfig().getString("creation-item", "EMERALD")
        );
        if (creationItem == null) creationItem = Material.EMERALD;

        if (player.getInventory().getItemInMainHand().getType() != creationItem) return;

        if (!isCarpet(block.getType())) return;

        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.SCAFFOLDING) return;

        if (ShopManager.getInstance().isShopLocation(block.getLocation())) return;

        event.setCancelled(true);

        int maxShops = PlayserShops.getInstance().getConfig().getInt("max-shops-per-player", 3);
        if (ShopManager.getInstance().getShopCount(player.getUniqueId()) >= maxShops) {
            player.sendMessage(Utils.colorize("&cVoc\u00ea j\u00e1 atingiu o m\u00e1ximo de lojas (" + maxShops + ")!"));
            return;
        }

        double cost = PlayserShops.getInstance().getConfig().getDouble("shop-creation-cost", 1000.0);
        if (cost > 0 && !EconomyManager.getInstance().hasBalance(player.getUniqueId(), cost)) {
            player.sendMessage(Utils.colorize("&cVoc\u00ea precisa de " + EconomyManager.getInstance().format(cost) + " para criar uma loja!"));
            return;
        }

        UUID playerId = player.getUniqueId();
        pendingCreations.put(playerId, new PendingCreation(below.getLocation()));

        player.sendMessage(Utils.colorize("&aDeseja criar uma loja por " + EconomyManager.getInstance().format(cost) + "?"));
        player.sendMessage(Utils.colorize("&7Digite &a\u2714 sim &7para confirmar ou &c\u2716 n\u00e3o &7para cancelar."));

        Bukkit.getScheduler().runTaskLater(PlayserShops.getInstance(), () -> {
            if (pendingCreations.containsKey(playerId)) {
                pendingCreations.remove(playerId);
                player.sendMessage(Utils.colorize("&cCria\u00e7\u00e3o de loja cancelada (tempo esgotado)."));
            }
        }, 600L);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!pendingCreations.containsKey(playerId)) return;

        event.setCancelled(true);
        PendingCreation pending = pendingCreations.remove(playerId);

        String msg = event.getMessage().trim().toLowerCase();

        if (msg.equals("sim") || msg.equals("s") || msg.equals("yes") || msg.equals("y")) {
            double cost = PlayserShops.getInstance().getConfig().getDouble("shop-creation-cost", 1000.0);

            if (cost > 0) {
                if (!EconomyManager.getInstance().hasBalance(playerId, cost)) {
                    player.sendMessage(Utils.colorize("&cSaldo insuficiente!"));
                    return;
                }
                EconomyManager.getInstance().withdraw(playerId, cost);
            }

            ShopManager.getInstance().createShop(playerId, player.getName(), pending.scaffoldingLocation);
            player.sendMessage(Utils.colorize("&aLoja criada com sucesso! Use &e/lojinha &apara configur\u00e1-la."));
            if (cost > 0) {
                player.sendMessage(Utils.colorize("&7Cobrado " + EconomyManager.getInstance().format(cost) + " pela cria\u00e7\u00e3o da loja."));
            }
        } else if (msg.equals("n\u00e3o") || msg.equals("nao") || msg.equals("n") || msg.equals("no")) {
            player.sendMessage(Utils.colorize("&cCria\u00e7\u00e3o de loja cancelada."));
        } else {
            player.sendMessage(Utils.colorize("&cResposta inv\u00e1lida! Digite &a\u2714 sim &7ou &c\u2716 n\u00e3o&c."));
            pendingCreations.put(playerId, pending);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingCreations.remove(event.getPlayer().getUniqueId());
    }

    private boolean isCarpet(Material type) {
        if (type == Material.MOSS_CARPET) return true;
        String name = type.name();
        return name.endsWith("_CARPET") && !name.contains("WOOL");
    }
}
