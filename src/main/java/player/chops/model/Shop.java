package player.chops.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public class Shop {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private String worldName;
    private int x, y, z;
    private String name;
    private String message;
    private long creationTime;
    private long duration;
    private List<ShopItem> items;

    public Shop(UUID ownerId, String ownerName, Location location) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.name = ownerName + "'s Shop";
        this.message = "Welcome!";
        this.creationTime = System.currentTimeMillis();
        this.duration = 10800;
        this.items = new ArrayList<>();
    }

    public Shop() {
        this.id = UUID.randomUUID();
        this.items = new ArrayList<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long creationTime) { this.creationTime = creationTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public List<ShopItem> getItems() { return items; }
    public void setItems(List<ShopItem> items) { this.items = items; }

    public long getRemainingTime() {
        return (creationTime + (duration * 1000)) - System.currentTimeMillis();
    }

    public boolean isExpired() {
        return getRemainingTime() <= 0;
    }

    public Location getLocation(World world) {
        return new Location(world, x + 0.5, y, z + 0.5);
    }
}
