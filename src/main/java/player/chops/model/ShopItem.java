package player.chops.model;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private ItemStack item;
    private int stock;
    private int maxStock;
    private double sellPrice;
    private double buyPrice;

    public ShopItem(ItemStack item, int stock, int maxStock, double sellPrice, double buyPrice) {
        this.item = item.clone();
        this.item.setAmount(1);
        this.stock = stock;
        this.maxStock = maxStock;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }

    public ItemStack getItem() { return item.clone(); }
    public void setItem(ItemStack item) { this.item = item.clone(); this.item.setAmount(1); }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }

    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
}
