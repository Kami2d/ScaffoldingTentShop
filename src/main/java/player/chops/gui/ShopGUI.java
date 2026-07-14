package player.chops.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import player.chops.PlayserShops;
import player.chops.managers.EconomyManager;
import player.chops.managers.ShopManager;
import player.chops.model.Shop;
import player.chops.model.ShopItem;
import player.chops.utils.Utils;

public class ShopGUI implements Listener {

    private static final String PREFIX = Utils.colorize("&8[&bShop&8] &r");

    // Chat awaiting tracking
    private static final Map<UUID, ChatAction> pendingChat = new HashMap<>();

    private enum ChatType { SHOP_NAME, SHOP_MESSAGE, ITEM_SELL_PRICE, ITEM_BUY_PRICE }

    private static class ChatAction {
        final ChatType type;
        final Shop shop;
        final int itemIndex;
        final int page;

        ChatAction(ChatType type, Shop shop, int itemIndex, int page) {
            this.type = type; this.shop = shop; this.itemIndex = itemIndex; this.page = page;
        }
    }

    public ShopGUI() {}

    // ========================================================================
    // GLOBAL SHOP BROWSER
    // ========================================================================

    public static void openShopsBrowser(Player player, int page) {
        List<Shop> shops = ShopManager.getInstance().getAllShops();
        int maxPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) shops.size() / maxPerPage));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 54,
            Utils.colorize("&8&l\u26a1 Shops &7(" + (page + 1) + "/" + totalPages + ")"));

        int start = page * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, shops.size()); i++) {
            Shop shop = shops.get(i);
            int sellCount = 0, buyCount = 0;
            for (ShopItem si : shop.getItems()) {
                if (si.getSellPrice() > 0) sellCount++;
                if (si.getBuyPrice() > 0) buyCount++;
            }

            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(Utils.colorize("&b" + shop.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(Utils.colorize("&7Dono: &f" + shop.getOwnerName()));
            lore.add(Utils.colorize("&7Local: &f" + shop.getWorldName() + " " + shop.getX() + " " + shop.getY() + " " + shop.getZ()));
            lore.add(Utils.colorize("&7\u23f1 Tempo: &f" + formatTime(shop.getRemainingTime())));
            lore.add("");
            lore.add(Utils.colorize("&a\u25b6 Venda: &f" + sellCount));
            lore.add(Utils.colorize("&e\u25c0 Compra: &f" + buyCount));
            lore.add("");
            lore.add(Utils.colorize("&eClique para ver itens!"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.setItem(i - start, icon);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            pMeta.setDisplayName(Utils.colorize("&aP\u00e1gina anterior"));
            prev.setItemMeta(pMeta); inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            nMeta.setDisplayName(Utils.colorize("&aPr\u00f3xima p\u00e1gina"));
            next.setItemMeta(nMeta); inv.setItem(53, next);
        }
        ItemStack myShops = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta mMeta = myShops.getItemMeta();
        mMeta.setDisplayName(Utils.colorize("&6Minhas Lojas"));
        mMeta.setLore(List.of(Utils.colorize("&7Clique para ver suas lojas")));
        myShops.setItemMeta(mMeta);
        inv.setItem(49, myShops);
        player.openInventory(inv);
    }

    // ========================================================================
    // ADMIN SHOPS LIST
    // ========================================================================

    public static void openAdminGUI(Player player, int page) {
        List<Shop> shops = ShopManager.getInstance().getAllShops();
        int maxPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) shops.size() / maxPerPage));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 54,
            Utils.colorize("&8&l\u2699 Admin Shops &7(" + (page + 1) + "/" + totalPages + ")"));

        int start = page * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, shops.size()); i++) {
            Shop shop = shops.get(i);
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(Utils.colorize("&b" + shop.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(Utils.colorize("&7Dono: &f" + shop.getOwnerName()));
            lore.add(Utils.colorize("&7Local: &f" + shop.getWorldName() + " " + shop.getX() + " " + shop.getY() + " " + shop.getZ()));
            lore.add(Utils.colorize("&7\u23f1 Tempo: &f" + formatTime(shop.getRemainingTime())));
            lore.add("");
            lore.add(Utils.colorize("&cClique direito para deletar"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.setItem(i - start, icon);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            pMeta.setDisplayName(Utils.colorize("&aP\u00e1gina anterior"));
            prev.setItemMeta(pMeta); inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            nMeta.setDisplayName(Utils.colorize("&aPr\u00f3xima p\u00e1gina"));
            next.setItemMeta(nMeta); inv.setItem(53, next);
        }
        player.openInventory(inv);
    }

    // ========================================================================
    // OWN SHOPS LIST
    // ========================================================================

    public static void openOwnShopsGUI(Player player, int page) {
        List<Shop> owned = ShopManager.getInstance().getShopsByOwner(player.getUniqueId());
        int maxPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) owned.size() / maxPerPage));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 54,
            Utils.colorize("&8&l\u2699 Suas Lojas &7(" + (page + 1) + "/" + totalPages + ")"));

        int start = page * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, owned.size()); i++) {
            Shop shop = owned.get(i);
            int sellCount = 0, buyCount = 0;
            for (ShopItem si : shop.getItems()) {
                if (si.getSellPrice() > 0) sellCount++;
                if (si.getBuyPrice() > 0) buyCount++;
            }
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(Utils.colorize("&b" + shop.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(Utils.colorize("&7Local: &f" + shop.getWorldName() + " " + shop.getX() + " " + shop.getY() + " " + shop.getZ()));
            lore.add(Utils.colorize("&7\u23f1 Tempo: &f" + formatTime(shop.getRemainingTime())));
            lore.add("");
            lore.add(Utils.colorize("&a\u25b6 Venda: &f" + sellCount + "  |  &e\u25c0 Compra: &f" + buyCount));
            lore.add("");
            lore.add(Utils.colorize("&eClique para configurar!"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.setItem(i - start, icon);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            pMeta.setDisplayName(Utils.colorize("&aP\u00e1gina anterior"));
            prev.setItemMeta(pMeta); inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            nMeta.setDisplayName(Utils.colorize("&aPr\u00f3xima p\u00e1gina"));
            next.setItemMeta(nMeta); inv.setItem(53, next);
        }
        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(Utils.colorize("&7\u2190 Voltar"));
        back.setItemMeta(bMeta); inv.setItem(48, back);
        player.openInventory(inv);
    }

    // ========================================================================
    // SHOP BUY GUI
    // ========================================================================

    public static void openShopBuyGUI(Player player, Shop shop, int page) {
        List<ShopItem> items = shop.getItems();
        int maxPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / maxPerPage));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 54,
            Utils.colorize("&8Loja: &b" + shop.getName() + " &7(" + (page + 1) + "/" + totalPages + ")"));

        // Info bar
        ItemStack infoBar = new ItemStack(Material.BOOK);
        ItemMeta iMeta = infoBar.getItemMeta();
        iMeta.setDisplayName(Utils.colorize("&6&l" + shop.getName()));
        List<String> iLore = new ArrayList<>();
        iLore.add(Utils.colorize("&7Dono: &f" + shop.getOwnerName()));
        iLore.add(Utils.colorize("&7\u23f1 Tempo: &f" + formatTime(shop.getRemainingTime())));
        iLore.add("");
        iLore.add(Utils.colorize("&a\u25b6 Esquerdo = Comprar"));
        iLore.add(Utils.colorize("&e\u25c0 Direito = Vender"));
        iMeta.setLore(iLore);
        infoBar.setItemMeta(iMeta);
        inv.setItem(4, infoBar);

        int start = page * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, items.size()); i++) {
            ShopItem si = items.get(i);
            ItemStack display = si.getItem().clone();
            display.setAmount(Math.max(Math.min(si.getStock(), 64), 1));
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (si.getSellPrice() > 0 && si.getStock() > 0)
                lore.add(Utils.colorize("&a\u25b6 Comprar: &f" + EconomyManager.getInstance().format(si.getSellPrice()) + " &7(unidade)"));
            else if (si.getSellPrice() > 0)
                lore.add(Utils.colorize("&7\u25b6 Esgotado"));
            else
                lore.add(Utils.colorize("&7\u25b6 N\u00e3o \u00e0 venda"));
            if (si.getBuyPrice() > 0 && si.getStock() < si.getMaxStock())
                lore.add(Utils.colorize("&e\u25c0 Vender: &f" + EconomyManager.getInstance().format(si.getBuyPrice()) + " &7(unidade)"));
            else if (si.getBuyPrice() > 0)
                lore.add(Utils.colorize("&7\u25c0 Estoque cheio"));
            else
                lore.add(Utils.colorize("&7\u25c0 N\u00e3o comprando"));
            lore.add("");
            lore.add(Utils.colorize("&7Estoque: &f" + si.getStock() + "/" + si.getMaxStock()));
            lore.add("");
            lore.add(Utils.colorize("&e\uc6e1 Comprar | &e\uc6e2 Vender"));
            if (meta.hasLore()) {
                List<String> existing = meta.getLore();
                if (existing != null) lore.addAll(0, existing);
            }
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(i - start + 9, display);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            pMeta.setDisplayName(Utils.colorize("&aP\u00e1gina anterior"));
            prev.setItemMeta(pMeta); inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            nMeta.setDisplayName(Utils.colorize("&aPr\u00f3xima p\u00e1gina"));
            next.setItemMeta(nMeta); inv.setItem(53, next);
        }
        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(Utils.colorize("&7\u2190 Voltar"));
        back.setItemMeta(bMeta); inv.setItem(48, back);
        player.openInventory(inv);
    }

    // ========================================================================
    // SHOP CONFIG GUI
    // ========================================================================

    public static void openShopConfigGUI(Player player, Shop shop, int page) {
        List<ShopItem> items = shop.getItems();
        int maxPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / maxPerPage));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 54,
            Utils.colorize("&8Config: &b" + shop.getName()));

        // Name
        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nMeta = nameItem.getItemMeta();
        nMeta.setDisplayName(Utils.colorize("&6Nome da Loja"));
        nMeta.setLore(List.of(Utils.colorize("&7Atual: &f" + shop.getName()), "", Utils.colorize("&eClique para alterar")));
        nameItem.setItemMeta(nMeta);
        inv.setItem(0, nameItem);

        // Message
        ItemStack msgItem = new ItemStack(Material.PAPER);
        ItemMeta mMeta = msgItem.getItemMeta();
        mMeta.setDisplayName(Utils.colorize("&6Mensagem"));
        mMeta.setLore(List.of(Utils.colorize("&7Atual: &f" + shop.getMessage()), "", Utils.colorize("&eClique para alterar")));
        msgItem.setItemMeta(mMeta);
        inv.setItem(1, msgItem);

        // Add product
        ItemStack addItem = new ItemStack(Material.HOPPER);
        ItemMeta aMeta = addItem.getItemMeta();
        aMeta.setDisplayName(Utils.colorize("&a&lAdicionar Produto"));
        aMeta.setLore(List.of(Utils.colorize("&7Clique para abrir o menu"), Utils.colorize("&7com quantidade e pre\u00e7o")));
        addItem.setItemMeta(aMeta);
        inv.setItem(4, addItem);

        // Delete shop
        ItemStack removeShop = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = removeShop.getItemMeta();
        rMeta.setDisplayName(Utils.colorize("&c&lDeletar Loja"));
        rMeta.setLore(List.of(Utils.colorize("&7Remove a loja e dropa os itens")));
        removeShop.setItemMeta(rMeta);
        inv.setItem(8, removeShop);

        // Items list
        int start = page * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, items.size()); i++) {
            ShopItem si = items.get(i);
            ItemStack display = si.getItem().clone();
            display.setAmount(Math.min(si.getStock(), 64));
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(Utils.colorize("&7Estoque: &f" + si.getStock() + "/" + si.getMaxStock()));
            lore.add(Utils.colorize((si.getSellPrice() > 0 ? "&aVenda: &f" + EconomyManager.getInstance().format(si.getSellPrice()) : "&7N\u00e3o \u00e0 venda")));
            lore.add(Utils.colorize((si.getBuyPrice() > 0 ? "&eCompra: &f" + EconomyManager.getInstance().format(si.getBuyPrice()) : "&7N\u00e3o comprando")));
            lore.add("");
            lore.add(Utils.colorize("&eEsquerdo: editar pre\u00e7o"));
            lore.add(Utils.colorize("&cDireito: remover"));
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(i - start + 9, display);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pMeta = prev.getItemMeta();
            pMeta.setDisplayName(Utils.colorize("&aP\u00e1gina anterior"));
            prev.setItemMeta(pMeta); inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta2 = next.getItemMeta();
            nMeta2.setDisplayName(Utils.colorize("&aPr\u00f3xima p\u00e1gina"));
            next.setItemMeta(nMeta2); inv.setItem(53, next);
        }
        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(Utils.colorize("&7\u2190 Voltar"));
        back.setItemMeta(bMeta); inv.setItem(48, back);
        player.openInventory(inv);
    }

    // ========================================================================
    // INVENTORY CLICK HANDLER
    // ========================================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(Utils.colorize("&8Adicionar Produto"))) {
            AddProductGUI.handleClick(player, event);
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        if (title.contains("\u26a1 Shops")) {
            event.setCancelled(true);
            handleBrowserClick(player, event, title);
            return;
        }
        if (title.contains("\u2699 Suas Lojas")) {
            event.setCancelled(true);
            handleOwnShopsClick(player, event, title);
            return;
        }
        if (title.contains("Admin Shops")) {
            event.setCancelled(true);
            handleAdminClick(player, event, title);
            return;
        }
        if (title.contains("Loja:") && !title.contains("Config:")) {
            event.setCancelled(true);
            handleBuyGUIClick(player, event);
            return;
        }
        if (title.contains("Config:")) {
            event.setCancelled(true);
            handleConfigGUIClick(player, event);
            return;
        }
    }

    private void handleBrowserClick(Player player, InventoryClickEvent event, String title) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.ARROW) {
            int page = extractPage(title);
            if (item.getItemMeta().getDisplayName().contains("anterior"))
                openShopsBrowser(player, page - 1);
            else openShopsBrowser(player, page + 1);
            return;
        }
        if (item.getType() == Material.PLAYER_HEAD) {
            openOwnShopsGUI(player, 0);
            return;
        }
        if (item.getType() == Material.CHEST) {
            openShopAtSlot(player, event.getSlot(), extractPage(title), 36);
        }
    }

    private void handleOwnShopsClick(Player player, InventoryClickEvent event, String title) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.ARROW) {
            int page = extractPage(title);
            if (item.getItemMeta().getDisplayName().contains("anterior"))
                openOwnShopsGUI(player, page - 1);
            else openOwnShopsGUI(player, page + 1);
            return;
        }
        if (item.getType() == Material.SPECTRAL_ARROW) {
            openShopsBrowser(player, 0);
            return;
        }
        if (item.getType() == Material.CHEST) {
            int page = extractPage(title);
            int index = page * 36 + event.getSlot();
            List<Shop> owned = ShopManager.getInstance().getShopsByOwner(player.getUniqueId());
            if (index >= 0 && index < owned.size()) {
                openShopConfigGUI(player, owned.get(index), 0);
            }
        }
    }

    private void handleAdminClick(Player player, InventoryClickEvent event, String title) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.ARROW) {
            int page = extractPage(title);
            if (item.getItemMeta().getDisplayName().contains("anterior"))
                openAdminGUI(player, page - 1);
            else openAdminGUI(player, page + 1);
            return;
        }
        if (item.getType() == Material.CHEST) {
            int page = extractPage(title);
            int index = page * 36 + event.getSlot();
            List<Shop> all = ShopManager.getInstance().getAllShops();
            if (index >= 0 && index < all.size()) {
                Shop shop = all.get(index);
                if (event.getClick() == ClickType.RIGHT) {
                    ShopManager.getInstance().removeShop(shop);
                    player.sendMessage(PREFIX + Utils.colorize("&cLoja &f" + shop.getName() + " &cdeletada por admin!"));
                    openAdminGUI(player, page);
                } else {
                    openShopConfigGUI(player, shop, 0);
                }
            }
        }
    }

    private void openShopAtSlot(Player player, int slot, int page, int perPage) {
        List<Shop> shops = ShopManager.getInstance().getAllShops();
        int index = page * perPage + slot;
        if (index < 0 || index >= shops.size()) return;
        Shop shop = shops.get(index);
        if (shop.getOwnerId().equals(player.getUniqueId()))
            openShopConfigGUI(player, shop, 0);
        else
            openShopBuyGUI(player, shop, 0);
    }

    private void handleBuyGUIClick(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        String title = event.getView().getTitle();
        Shop shop = getShopFromBuyTitle(title);
        if (shop == null) return;

        if (item.getType() == Material.ARROW) {
            int page = extractPage(title);
            if (item.getItemMeta().getDisplayName().contains("anterior"))
                openShopBuyGUI(player, shop, page - 1);
            else openShopBuyGUI(player, shop, page + 1);
            return;
        }
        if (item.getType() == Material.BOOK || item.getType() == Material.SPECTRAL_ARROW) {
            player.closeInventory();
            return;
        }

        int page = extractPage(title);
        int index = page * 45 + (event.getSlot() - 9);
        if (index < 0 || index >= shop.getItems().size()) return;
        ShopItem si = shop.getItems().get(index);
        UUID ownerId = shop.getOwnerId();
        double price;

        if (event.getClick() == ClickType.LEFT) {
            if (si.getSellPrice() <= 0) { player.sendMessage(PREFIX + Utils.colorize("&cN\u00e3o \u00e0 venda.")); return; }
            if (si.getStock() <= 0) { player.sendMessage(PREFIX + Utils.colorize("&cSem estoque.")); return; }
            price = si.getSellPrice();
            if (!EconomyManager.getInstance().hasBalance(player.getUniqueId(), price)) { player.sendMessage(PREFIX + Utils.colorize("&cSem dinheiro!")); return; }
            if (player.getInventory().firstEmpty() == -1) { player.sendMessage(PREFIX + Utils.colorize("&cInvent\u00e1rio cheio!")); return; }
            EconomyManager.getInstance().withdraw(player.getUniqueId(), price);
            EconomyManager.getInstance().deposit(ownerId, price);
            si.setStock(si.getStock() - 1);
            player.getInventory().addItem(si.getItem().clone());
            ShopManager.getInstance().saveShops();
            player.sendMessage(PREFIX + Utils.colorize("&aComprou &f" + si.getItem().getType().name() + " &apor " + EconomyManager.getInstance().format(price)));
            openShopBuyGUI(player, shop, page);
        } else if (event.getClick() == ClickType.RIGHT) {
            if (si.getBuyPrice() <= 0) { player.sendMessage(PREFIX + Utils.colorize("&cN\u00e3o est\u00e1 comprando.")); return; }
            if (si.getStock() >= si.getMaxStock()) { player.sendMessage(PREFIX + Utils.colorize("&cEstoque cheio.")); return; }
            ItemStack required = si.getItem().clone(); required.setAmount(1);
            if (!player.getInventory().containsAtLeast(required, 1)) { player.sendMessage(PREFIX + Utils.colorize("&cVoc\u00ea n\u00e3o tem!")); return; }
            price = si.getBuyPrice();
            if (!EconomyManager.getInstance().hasBalance(ownerId, price)) { player.sendMessage(PREFIX + Utils.colorize("&cDono sem fundos!")); return; }
            player.getInventory().removeItem(required);
            EconomyManager.getInstance().deposit(player.getUniqueId(), price);
            EconomyManager.getInstance().withdraw(ownerId, price);
            si.setStock(si.getStock() + 1);
            ShopManager.getInstance().saveShops();
            player.sendMessage(PREFIX + Utils.colorize("&eVendeu &f" + required.getType().name() + " &epor " + EconomyManager.getInstance().format(price)));
            openShopBuyGUI(player, shop, page);
        }
    }

    private void handleConfigGUIClick(Player player, InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Shop shop = getShopFromConfigTitle(title);
        if (shop == null) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        int slot = event.getSlot();

        if (item.getType() == Material.SPECTRAL_ARROW) {
            openOwnShopsGUI(player, 0); return;
        }

        // Name
        if (slot == 0) {
            player.closeInventory();
            pendingChat.put(player.getUniqueId(), new ChatAction(ChatType.SHOP_NAME, shop, -1, 0));
            player.sendMessage(PREFIX + Utils.colorize("&aDigite o novo nome da loja no chat:"));
            player.sendMessage(PREFIX + Utils.colorize("&7(Digite 'cancelar' para cancelar)"));
            return;
        }

        // Message
        if (slot == 1) {
            player.closeInventory();
            pendingChat.put(player.getUniqueId(), new ChatAction(ChatType.SHOP_MESSAGE, shop, -1, 0));
            player.sendMessage(PREFIX + Utils.colorize("&aDigite a nova mensagem da loja no chat:"));
            player.sendMessage(PREFIX + Utils.colorize("&7(Digite 'cancelar' para cancelar)"));
            return;
        }

        // Add product
        if (slot == 4) {
            AddProductGUI.open(player, shop);
            return;
        }

        // Delete shop
        if (slot == 8) {
            player.closeInventory();
            ShopManager.getInstance().removeShop(shop);
            player.sendMessage(PREFIX + Utils.colorize("&cLoja deletada! Itens dropados."));
            return;
        }

        // Edit/remove item
        int page = extractPage(title);
        int index = page * 36 + (slot - 9);
        if (index >= 0 && index < shop.getItems().size()) {
            ShopItem si = shop.getItems().get(index);
            if (event.getClick() == ClickType.LEFT) {
                player.closeInventory();
                pendingChat.put(player.getUniqueId(), new ChatAction(ChatType.ITEM_SELL_PRICE, shop, index, page));
                player.sendMessage(PREFIX + Utils.colorize("&aDigite o PRE\u00c7O DE VENDA (0 desativa):"));
                player.sendMessage(PREFIX + Utils.colorize("&7Atual: " + EconomyManager.getInstance().format(si.getSellPrice())));
                return;
            } else if (event.getClick() == ClickType.RIGHT) {
                shop.getItems().remove(index);
                ShopManager.getInstance().updateShop(shop);
                player.sendMessage(PREFIX + Utils.colorize("&cItem removido!"));
                openShopConfigGUI(player, shop, page);
            }
        }
    }

    // ========================================================================
    // CHAT INPUT HANDLER
    // ========================================================================

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatAction action = pendingChat.get(player.getUniqueId());
        if (action == null) return;

        event.setCancelled(true);
        pendingChat.remove(player.getUniqueId());

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancelar")) {
            player.sendMessage(PREFIX + Utils.colorize("&cCancelado."));
            Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> {
                if (action.type == ChatType.ITEM_SELL_PRICE || action.type == ChatType.ITEM_BUY_PRICE) {
                    openShopConfigGUI(player, action.shop, action.page);
                } else {
                    openShopConfigGUI(player, action.shop, 0);
                }
            });
            return;
        }

        Bukkit.getScheduler().runTask(PlayserShops.getInstance(), () -> {
            switch (action.type) {
                case SHOP_NAME: {
                    if (msg.length() > 32) {
                        player.sendMessage(PREFIX + Utils.colorize("&cM\u00e1ximo 32 caracteres!"));
                        openShopConfigGUI(player, action.shop, 0);
                        return;
                    }
                    action.shop.setName(msg);
                    ShopManager.getInstance().updateShop(action.shop);
                    player.sendMessage(PREFIX + Utils.colorize("&aNome alterado para &f" + msg));
                    openShopConfigGUI(player, action.shop, 0);
                    return;
                }
                case SHOP_MESSAGE: {
                    if (msg.length() > 48) {
                        player.sendMessage(PREFIX + Utils.colorize("&cM\u00e1ximo 48 caracteres!"));
                        openShopConfigGUI(player, action.shop, 0);
                        return;
                    }
                    action.shop.setMessage(msg);
                    ShopManager.getInstance().updateShop(action.shop);
                    player.sendMessage(PREFIX + Utils.colorize("&aMensagem alterada para &f" + msg));
                    openShopConfigGUI(player, action.shop, 0);
                    return;
                }
                case ITEM_SELL_PRICE: {
                    try {
                        double price = Double.parseDouble(msg.replace(",", "."));
                        if (price < 0) {
                            player.sendMessage(PREFIX + Utils.colorize("&cPre\u00e7o inv\u00e1lido!"));
                            openShopConfigGUI(player, action.shop, action.page);
                            return;
                        }
                        List<ShopItem> items = action.shop.getItems();
                        if (action.itemIndex < 0 || action.itemIndex >= items.size()) {
                            openShopConfigGUI(player, action.shop, action.page);
                            return;
                        }
                        items.get(action.itemIndex).setSellPrice(price);
                        ShopManager.getInstance().updateShop(action.shop);
                        player.sendMessage(PREFIX + Utils.colorize("&aPre\u00e7o de venda alterado para " + EconomyManager.getInstance().format(price)));
                    } catch (NumberFormatException e) {
                        player.sendMessage(PREFIX + Utils.colorize("&cN\u00famero inv\u00e1lido!"));
                    }
                    openShopConfigGUI(player, action.shop, action.page);
                    return;
                }
                case ITEM_BUY_PRICE: {
                    try {
                        double price = Double.parseDouble(msg.replace(",", "."));
                        if (price < 0) {
                            player.sendMessage(PREFIX + Utils.colorize("&cPre\u00e7o inv\u00e1lido!"));
                            openShopConfigGUI(player, action.shop, action.page);
                            return;
                        }
                        List<ShopItem> items = action.shop.getItems();
                        if (action.itemIndex < 0 || action.itemIndex >= items.size()) {
                            openShopConfigGUI(player, action.shop, action.page);
                            return;
                        }
                        items.get(action.itemIndex).setBuyPrice(price);
                        ShopManager.getInstance().updateShop(action.shop);
                        player.sendMessage(PREFIX + Utils.colorize("&aPre\u00e7o de compra alterado para " + EconomyManager.getInstance().format(price)));
                    } catch (NumberFormatException e) {
                        player.sendMessage(PREFIX + Utils.colorize("&cN\u00famero inv\u00e1lido!"));
                    }
                    openShopConfigGUI(player, action.shop, action.page);
                    return;
                }
            }
        });
    }

    // ========================================================================
    // INVENTORY CLOSE
    // ========================================================================

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().equals(Utils.colorize("&8Adicionar Produto"))) {
            AddProductGUI.handleClose(player);
        }
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private static int extractPage(String title) {
        int idx = title.indexOf('(');
        if (idx == -1) return 0;
        int slash = title.indexOf('/', idx);
        if (slash == -1) return 0;
        try { return Integer.parseInt(title.substring(idx + 1, slash).trim()) - 1; } catch (NumberFormatException e) { return 0; }
    }

    private static Shop getShopFromBuyTitle(String title) {
        int start = title.indexOf("Loja:") + 5;
        int end = title.indexOf(" (");
        if (start < 5 || end == -1) return null;
        String shopName = org.bukkit.ChatColor.stripColor(title.substring(start, end).trim());
        for (Shop s : ShopManager.getInstance().getAllShops()) {
            if (org.bukkit.ChatColor.stripColor(s.getName()).equals(shopName)) return s;
        }
        return null;
    }

    private static Shop getShopFromConfigTitle(String title) {
        int start = title.indexOf("Config:") + 7;
        if (start < 7) return null;
        String shopName = org.bukkit.ChatColor.stripColor(title.substring(start).trim());
        for (Shop s : ShopManager.getInstance().getAllShops()) {
            if (org.bukkit.ChatColor.stripColor(s.getName()).equals(shopName)) return s;
        }
        return null;
    }

    private static String formatTime(long millis) {
        if (millis <= 0) return Utils.colorize("&cExpirado");
        long seconds = millis / 1000;
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
}
