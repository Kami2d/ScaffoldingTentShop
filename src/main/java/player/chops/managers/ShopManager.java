package player.chops.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import player.chops.PlayserShops;
import player.chops.model.Shop;
import player.chops.model.ShopItem;
import player.chops.utils.Utils;

public class ShopManager {

    private static ShopManager instance;
    private final Map<UUID, Shop> shops = new HashMap<>();
    private final Map<Location, UUID> locationIndex = new HashMap<>();
    private File dataFile;
    private Gson gson;
    private int taskId = -1;

    private ShopManager() {}

    public static ShopManager getInstance() {
        if (instance == null) {
            instance = new ShopManager();
        }
        return instance;
    }

    public void initialize() {
        gson = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new Utils.ItemStackAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

        dataFile = new File(PlayserShops.getInstance().getDataFolder(), "shops.json");
        loadShops();
        rebuildLocationIndex();
        startExpirationTask();
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        saveShops();
        HologramManager.getInstance().removeAll();
    }

    private void startExpirationTask() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlayserShops.getInstance(), () -> {
            List<Shop> expired = new ArrayList<>();
            for (Shop shop : shops.values()) {
                if (shop.isExpired()) {
                    expired.add(shop);
                }
            }
            for (Shop shop : expired) {
                removeShop(shop);
                returnItemsToOwner(shop);
            }
        }, 1200L, 600L);
    }

    private void returnItemsToOwner(Shop shop) {
        if (shop.getItems().isEmpty()) return;
        // Drop items at the shop location as a fallback
        World world = Bukkit.getWorld(shop.getWorldName());
        if (world == null) return;
        Location loc = shop.getLocation(world);
        for (ShopItem si : shop.getItems()) {
            if (si.getStock() > 0) {
                ItemStack drop = si.getItem().clone();
                drop.setAmount(si.getStock());
                world.dropItemNaturally(loc, drop);
            }
        }
    }

    public Shop createShop(UUID ownerId, String ownerName, Location location) {
        Shop shop = new Shop(ownerId, ownerName, location);
        shop.setDuration(PlayserShops.getInstance().getConfig().getLong("shop-duration", 10800));
        shops.put(shop.getId(), shop);
        locationIndex.put(location.getBlock().getLocation(), shop.getId());
        HologramManager.getInstance().createHologram(shop);
        saveShops();
        return shop;
    }

    public void removeShop(Shop shop) {
        HologramManager.getInstance().removeHologram(shop);
        locationIndex.remove(new Location(
            Bukkit.getWorld(shop.getWorldName()),
            shop.getX(), shop.getY(), shop.getZ()
        ).getBlock().getLocation());
        shops.remove(shop.getId());
        saveShops();
    }

    public Shop getShop(UUID id) {
        return shops.get(id);
    }

    public Shop getShopAt(Location location) {
        Location blockLoc = location.getBlock().getLocation();
        UUID id = locationIndex.get(blockLoc);
        if (id != null) return shops.get(id);

        // Check one block below (carpet on scaffolding)
        Location below = blockLoc.clone().add(0, -1, 0);
        id = locationIndex.get(below);
        return id != null ? shops.get(id) : null;
    }

    public boolean isShopLocation(Location location) {
        return locationIndex.containsKey(location.getBlock().getLocation());
    }

    public List<Shop> getShopsByOwner(UUID ownerId) {
        return shops.values().stream()
            .filter(s -> s.getOwnerId().equals(ownerId))
            .collect(Collectors.toList());
    }

    public int getShopCount(UUID ownerId) {
        return (int) shops.values().stream()
            .filter(s -> s.getOwnerId().equals(ownerId))
            .count();
    }

    public List<Shop> getAllShops() {
        return new ArrayList<>(shops.values());
    }

    private void rebuildLocationIndex() {
        locationIndex.clear();
        for (Shop shop : shops.values()) {
            Location loc = new Location(
                Bukkit.getWorld(shop.getWorldName()),
                shop.getX(), shop.getY(), shop.getZ()
            ).getBlock().getLocation();
            locationIndex.put(loc, shop.getId());
        }
    }

    private void loadShops() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<Shop>>() {}.getType();
            List<Shop> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                for (Shop shop : loaded) {
                    shops.put(shop.getId(), shop);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canAddItem(Shop shop) {
        return shop.getItems().size() < 54;
    }

    public boolean hasItem(Shop shop, ItemStack item) {
        ItemStack template = item.clone();
        template.setAmount(1);
        for (ShopItem si : shop.getItems()) {
            if (si.getItem().isSimilar(template)) return true;
        }
        return false;
    }

    public void saveShops() {
        if (!PlayserShops.getInstance().getDataFolder().exists()) {
            PlayserShops.getInstance().getDataFolder().mkdirs();
        }
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(new ArrayList<>(shops.values()), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateShop(Shop shop) {
        HologramManager.getInstance().updateHologram(shop);
        saveShops();
    }
}
