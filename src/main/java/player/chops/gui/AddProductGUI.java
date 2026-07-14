package player.chops.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import player.chops.PlayserShops;
import player.chops.managers.EconomyManager;
import player.chops.managers.ShopManager;
import player.chops.model.Shop;
import player.chops.model.ShopItem;
import player.chops.utils.Utils;

public class AddProductGUI implements Listener {

    private static final Map<UUID, AddSession> sessions = new HashMap<>();
    private static final Set<UUID> awaitingPrice = new HashSet<>();
    private static final Set<UUID> rebuilding = new HashSet<>();
    private static final String TITLE = Utils.colorize("&8Adicionar Produto");

    static class AddSession {
        final Shop shop;
        ItemStack item;
        int quantity;
        double price;
        boolean isBuying;
        boolean closingForPrice;

        AddSession(Shop shop, ItemStack hand) {
            this.shop = shop;
            this.item = (hand != null && hand.getType() != Material.AIR) ? hand.clone() : null;
            this.quantity = 0;
            this.price = 10.0;
            this.isBuying = false;
            this.closingForPrice = false;
        }
    }

    // ========================================================================
    // OPEN GUI
    // ========================================================================

    public static void open(Player player, Shop shop) {
        ItemStack hand = player.getInventory().getItemInMainHand().clone();
        AddSession session = new AddSession(shop, hand);

        if (session.item == null) {
            // Let player choose from inventory via the slot
            session.item = null;
            session.quantity = 0;
        }

        sessions.put(player.getUniqueId(), session);
        rebuildGUI(player);
    }

    private static void rebuildGUI(Player player) {
        AddSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // === BORDER ===
        ItemStack border = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        bMeta.setDisplayName(Utils.colorize("&8\u2731"));
        border.setItemMeta(bMeta);

        // Highlight around item slot
        ItemStack highlight = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta hMeta = highlight.getItemMeta();
        hMeta.setDisplayName(Utils.colorize("&b\u2731"));
        highlight.setItemMeta(hMeta);

        // Fill edges with border
        for (int i = 0; i < 54; i++) {
            int x = i % 9;
            int y = i / 9;
            if (x == 0 || x == 8 || y == 0 || y == 5) {
                inv.setItem(i, border.clone());
            }
        }

        // Highlight frame around slot 13
        int[] frameSlots = {4, 5, 6, 7, 12, 14, 21, 22, 23, 24};
        for (int s : frameSlots) {
            inv.setItem(s, highlight.clone());
        }

        // === ITEM SLOT (13) ===
        if (session.item != null) {
            ItemStack show = session.item.clone();
            show.setAmount(Math.min(session.quantity > 0 ? session.quantity : 1, 64));
            ItemMeta showMeta = show.getItemMeta();
            showMeta.setEnchantmentGlintOverride(true);
            show.setItemMeta(showMeta);
            inv.setItem(13, show);
        }

        // === STATUS INFO (slot 22 - center of highlight) ===
        ItemStack status = new ItemStack(Material.BOOK);
        ItemMeta sMeta = status.getItemMeta();
        sMeta.setDisplayName(Utils.colorize("&6&l\u26a1 Produto"));
        java.util.List<String> sLore = new java.util.ArrayList<>();
        if (session.item != null) {
            sLore.add(Utils.colorize("&7\u2726 Item: &f" + session.item.getType().name()));
            sLore.add(Utils.colorize("&7\u2726 Quantidade: &f" + session.quantity));
        } else {
            sLore.add(Utils.colorize("&7\u2726 Nenhum item"));
            sLore.add(Utils.colorize("&7\u2726 Segure e clique na loja"));
        }
        sLore.add("");
        sLore.add(Utils.colorize("&7\u2726 Modo: " + (session.isBuying ? "&eComprando" : "&aVendendo")));
        sLore.add(Utils.colorize("&7\u2726 Pre\u00e7o: &f" + EconomyManager.getInstance().format(session.price)));
        sMeta.setLore(sLore);
        status.setItemMeta(sMeta);
        inv.setItem(22, status);

        // === MODE TOGGLE (slot 0) ===
        ItemStack mode = new ItemStack(session.isBuying ? Material.GOLD_INGOT : Material.EMERALD);
        ItemMeta mMeta = mode.getItemMeta();
        mMeta.setDisplayName(Utils.colorize("&6&l\u21c4 Modo: " + (session.isBuying ? "&eComprando" : "&aVendendo")));
        mMeta.setEnchantmentGlintOverride(true);
        mMeta.setLore(java.util.List.of(
            Utils.colorize("&7Clique para alternar"),
            "",
            Utils.colorize("&aVendendo &7\u2192 jogador compra da loja"),
            Utils.colorize("&eComprando &7\u2192 loja compra do jogador")
        ));
        mode.setItemMeta(mMeta);
        inv.setItem(0, mode);

        // === + QUANTITY BUTTONS (slots 19-23) ===
        int[] plusSlots = {19, 20, 21, 22, 23};
        int[] plusAmounts = {1, 16, 32, 64, -1};
        String[] plusLabels = {"&a&l+1", "&a&l+16", "&a&l+32", "&a&l+64", "&a&l+Tudo"};

        for (int i = 0; i < 5; i++) {
            ItemStack btn = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta pMeta = btn.getItemMeta();
            pMeta.setDisplayName(Utils.colorize(plusLabels[i]));
            pMeta.setEnchantmentGlintOverride(true);
            java.util.List<String> pLore = new java.util.ArrayList<>();
            if (session.item != null) {
                pLore.add(Utils.colorize("&7Adicionar " + (plusAmounts[i] == -1 ? "todo o estoque" : plusAmounts[i] + " unidade(s)")));
                pLore.add(Utils.colorize("&7do seu invent\u00e1rio"));
            } else {
                pLore.add(Utils.colorize("&7Selecione um item primeiro"));
            }
            pMeta.setLore(pLore);
            btn.setItemMeta(pMeta);
            inv.setItem(plusSlots[i], btn);
        }

        // === - QUANTITY BUTTONS (slots 28-32) ===
        int[] minusSlots = {28, 29, 30, 31, 32};
        int[] minusAmounts = {1, 16, 32, 64, -1};
        String[] minusLabels = {"&c&l-1", "&c&l-16", "&c&l-32", "&c&l-64", "&c&l-Tudo"};

        for (int i = 0; i < 5; i++) {
            ItemStack btn = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta mMeta2 = btn.getItemMeta();
            mMeta2.setDisplayName(Utils.colorize(minusLabels[i]));
            mMeta2.setEnchantmentGlintOverride(true);
            java.util.List<String> mLore = new java.util.ArrayList<>();
            if (session.quantity > 0) {
                mLore.add(Utils.colorize("&7Remover " + (minusAmounts[i] == -1 ? "todas" : minusAmounts[i] + " unidade(s)")));
                mLore.add(Utils.colorize("&7e devolver ao invent\u00e1rio"));
            } else {
                mLore.add(Utils.colorize("&7Nada para remover"));
            }
            mMeta2.setLore(mLore);
            btn.setItemMeta(mMeta2);
            inv.setItem(minusSlots[i], btn);
        }

        // === PRICE BUTTON (slot 38) ===
        ItemStack priceBtn = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta pMeta2 = priceBtn.getItemMeta();
        pMeta2.setDisplayName(Utils.colorize("&d&l\u2605 Definir Pre\u00e7o"));
        pMeta2.setEnchantmentGlintOverride(true);
        pMeta2.setLore(java.util.List.of(
            Utils.colorize("&7Atual: &f" + EconomyManager.getInstance().format(session.price)),
            "",
            Utils.colorize("&eClique para digitar no chat")
        ));
        priceBtn.setItemMeta(pMeta2);
        inv.setItem(38, priceBtn);

        // === CANCEL BUTTON (slot 45) ===
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cMeta = cancel.getItemMeta();
        cMeta.setDisplayName(Utils.colorize("&c&l\u2716 Cancelar"));
        cMeta.setLore(java.util.List.of(
            Utils.colorize("&7Retorna os itens e volta"),
            Utils.colorize("&7para a configura\u00e7\u00e3o da loja")
        ));
        cancel.setItemMeta(cMeta);
        inv.setItem(45, cancel);

        // === CONFIRM BUTTON (slot 53) ===
        ItemStack confirm = new ItemStack(Material.BEACON);
        ItemMeta coMeta = confirm.getItemMeta();
        coMeta.setDisplayName(Utils.colorize("&a&l\u2714 Confirmar Produto"));
        coMeta.setEnchantmentGlintOverride(true);
        java.util.List<String> coLore = new java.util.ArrayList<>();
        if (session.item != null && session.quantity > 0) {
            coLore.add(Utils.colorize("&7Adicionar &f" + session.quantity + "x " + session.item.getType().name()));
            coLore.add(Utils.colorize("&7Modo: " + (session.isBuying ? "&eComprando" : "&aVendendo")));
            coLore.add(Utils.colorize("&7Pre\u00e7o: &f" + EconomyManager.getInstance().format(session.price)));
        } else {
            coLore.add(Utils.colorize("&7Configure o produto primeiro"));
            coLore.add(Utils.colorize("&7antes de confirmar"));
        }
        coMeta.setLore(coLore);
        confirm.setItemMeta(coMeta);
        inv.setItem(53, confirm);

        UUID pid = player.getUniqueId();
        rebuilding.add(pid);
        player.openInventory(inv);
        rebuilding.remove(pid);
    }

    // ========================================================================
    // CLICK HANDLER
    // ========================================================================

    public static void handleClick(Player player, InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) return;

        AddSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) {
            event.setCancelled(true);
            return;
        }

        // Slot 13: blocked - only selection via open() method
        if (slot == 13) {
            event.setCancelled(true);
            return;
        }

        // All other top slots: cancel and handle as buttons
        event.setCancelled(true);

        // --- Mode toggle (slot 0) ---
        if (slot == 0) {
            session.isBuying = !session.isBuying;
            rebuildGUI(player);
            return;
        }

        // --- + buttons (slots 19-23) ---
        int[] plusSlots = {19, 20, 21, 22, 23};
        int[] plusAmounts = {1, 16, 32, 64, -1};
        for (int i = 0; i < 5; i++) {
            if (slot == plusSlots[i]) {
                if (session.item == null) {
                    player.sendMessage(Utils.colorize("&cSelecione um item primeiro!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                int amount = plusAmounts[i] == -1 ? 9999 : plusAmounts[i];
                int taken = takeFromInventory(player, session.item.clone(), amount);
                if (taken > 0) {
                    session.quantity += taken;
                    rebuildGUI(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    player.sendMessage(Utils.colorize("&a+" + taken + " " + session.item.getType().name()));
                } else {
                    player.sendMessage(Utils.colorize("&cVoc\u00ea n\u00e3o tem esse item no invent\u00e1rio!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
        }

        // --- - buttons (slots 28-32) ---
        int[] minusSlots = {28, 29, 30, 31, 32};
        int[] minusAmounts = {1, 16, 32, 64, -1};
        for (int i = 0; i < 5; i++) {
            if (slot == minusSlots[i]) {
                if (session.quantity <= 0) return;
                int amount = minusAmounts[i] == -1 ? session.quantity : Math.min(minusAmounts[i], session.quantity);
                ItemStack returnItem = session.item.clone();
                returnItem.setAmount(amount);
                giveOrDrop(player, returnItem);
                session.quantity -= amount;
                if (session.quantity <= 0) {
                    session.item = null;
                    session.quantity = 0;
                }
                rebuildGUI(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
                player.sendMessage(Utils.colorize("&c-" + amount + " " + (session.item != null ? session.item.getType().name() : "itens")));
                return;
            }
        }

        // --- Set price (slot 38) ---
        if (slot == 38) {
            session.closingForPrice = true;
            awaitingPrice.add(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(Utils.colorize("&aDigite o pre\u00e7o no chat (ou 'cancelar'):"));
            return;
        }

        // --- Cancel (slot 45) ---
        if (slot == 45) {
            returnItems(player, session);
            cleanup(player);
            ShopGUI.openShopConfigGUI(player, session.shop, 0);
            return;
        }

        // --- Confirm (slot 53) ---
        if (slot == 53) {
            if (session.item == null || session.quantity <= 0) {
                player.sendMessage(Utils.colorize("&cColoque um item e ajuste a quantidade primeiro!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Anti-dump: max 64 per product
            if (session.quantity > 64) {
                player.sendMessage(Utils.colorize("&cM\u00e1ximo de 64 itens por produto!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Anti-dump: max 54 products per shop
            if (!ShopManager.getInstance().canAddItem(session.shop)) {
                player.sendMessage(Utils.colorize("&cLimite de 54 produtos por loja!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Anti-dump: no duplicate items
            if (ShopManager.getInstance().hasItem(session.shop, session.item)) {
                player.sendMessage(Utils.colorize("&cEste item j\u00e1 existe na loja!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            ShopItem si = new ShopItem(session.item, session.quantity, Math.max(session.quantity, 64), 0, 0);
            if (session.isBuying) {
                si.setBuyPrice(session.price);
                si.setSellPrice(0);
            } else {
                si.setSellPrice(session.price);
                si.setBuyPrice(0);
            }

            session.shop.getItems().add(si);
            ShopManager.getInstance().updateShop(session.shop);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            player.sendMessage(Utils.colorize("&aProduto adicionado: &f" + session.quantity + "x " + session.item.getType().name()));
            cleanup(player);
            ShopGUI.openShopConfigGUI(player, session.shop, 0);
            return;
        }
    }

    // ========================================================================
    // CHAT INPUT HANDLER
    // ========================================================================

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingPrice.contains(player.getUniqueId())) return;

        event.setCancelled(true);
        awaitingPrice.remove(player.getUniqueId());

        AddSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.closingForPrice = false;

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancelar")) {
            player.sendMessage(Utils.colorize("&cPre\u00e7o cancelado."));
            Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> rebuildGUI(player));
            return;
        }

        try {
            double price = Double.parseDouble(msg.replace(",", "."));
            if (price < 0) {
                player.sendMessage(Utils.colorize("&cPre\u00e7o n\u00e3o pode ser negativo!"));
                Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> rebuildGUI(player));
                return;
            }
            session.price = price;
            player.sendMessage(Utils.colorize("&aPre\u00e7o definido para " + EconomyManager.getInstance().format(price)));
            Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> rebuildGUI(player));
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.colorize("&cN\u00famero inv\u00e1lido! Digite um valor num\u00e9rico."));
            Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> rebuildGUI(player));
        }
    }

    // ========================================================================
    // CLOSE HANDLER
    // ========================================================================

    public static void handleClose(Player player) {
        UUID pid = player.getUniqueId();
        if (rebuilding.contains(pid)) return;

        AddSession session = sessions.get(pid);
        if (session != null && session.closingForPrice) {
            session.closingForPrice = false;
            return;
        }

        awaitingPrice.remove(pid);
        returnItems(player, session);
        cleanup(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        awaitingPrice.remove(player.getUniqueId());
        AddSession session = sessions.get(player.getUniqueId());
        returnItems(player, session);
        cleanup(player);
    }

    private static void returnItems(Player player, AddSession session) {
        if (session == null || session.item == null || session.quantity <= 0) return;
        ItemStack returnItem = session.item.clone();
        returnItem.setAmount(session.quantity);
        giveOrDrop(player, returnItem);
    }

    private static void giveOrDrop(Player player, ItemStack item) {
        java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack left : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }
    }

    private static void cleanup(Player player) {
        sessions.remove(player.getUniqueId());
        awaitingPrice.remove(player.getUniqueId());
    }

    // ========================================================================
    // UTILS
    // ========================================================================

    private static int countInInventory(Player player, ItemStack item) {
        int count = 0;
        ItemStack template = item.clone();
        template.setAmount(1);
        for (ItemStack inv : player.getInventory().getStorageContents()) {
            if (inv != null && inv.isSimilar(template)) {
                count += inv.getAmount();
            }
        }
        return count;
    }

    private static int takeFromInventory(Player player, ItemStack item, int maxAmount) {
        ItemStack template = item.clone();
        template.setAmount(1);
        int needed = maxAmount;
        for (int i = 0; i < player.getInventory().getStorageContents().length && needed > 0; i++) {
            ItemStack inv = player.getInventory().getItem(i);
            if (inv != null && inv.isSimilar(template)) {
                int take = Math.min(inv.getAmount(), needed);
                inv.setAmount(inv.getAmount() - take);
                if (inv.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
                needed -= take;
            }
        }
        return maxAmount - needed;
    }
}
