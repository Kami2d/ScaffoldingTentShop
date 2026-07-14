package player.chops.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import player.chops.PlayserShops;
import player.chops.model.Shop;
import player.chops.utils.Utils;

public class HologramManager {

    private static HologramManager instance;
    private final Map<UUID, TextDisplay[]> holograms = new HashMap<>();

    private HologramManager() {}

    public static HologramManager getInstance() {
        if (instance == null) {
            instance = new HologramManager();
        }
        return instance;
    }

    public void createHologram(Shop shop) {
        removeHologram(shop);

        World world = Bukkit.getWorld(shop.getWorldName());
        if (world == null) return;

        double offset = PlayserShops.getInstance().getConfig().getDouble("hologram.offset-y", 2.5);
        Location base = shop.getLocation(world).add(0, offset, 0);

        String[] lines = {
            Utils.colorize(PlayserShops.getInstance().getConfig().getString("hologram.lines.0", "&b&l[PLAYER SHOP]")),
            Utils.colorize(PlayserShops.getInstance().getConfig().getString("hologram.lines.1", "&f%shop_name%").replace("%shop_name%", shop.getName())),
            Utils.colorize(PlayserShops.getInstance().getConfig().getString("hologram.lines.2", "&e%shop_message%").replace("%shop_message%", shop.getMessage()))
        };

        TextDisplay[] displays = new TextDisplay[lines.length];
        for (int i = 0; i < lines.length; i++) {
            Location loc = base.clone().subtract(0, i * 0.3, 0);
            TextDisplay display = world.spawn(loc, TextDisplay.class);
            display.setText(lines[i]);
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(false);
            display.setInterpolationDuration(0);
            Transformation t = display.getTransformation();
            t.getScale().set(0.8f);
            display.setTransformation(t);
            display.setPersistent(false);
            display.setGravity(false);
            displays[i] = display;
        }

        holograms.put(shop.getId(), displays);
    }

    public void updateHologram(Shop shop) {
        createHologram(shop);
    }

    public void removeHologram(Shop shop) {
        TextDisplay[] displays = holograms.remove(shop.getId());
        if (displays != null) {
            for (TextDisplay display : displays) {
                if (display != null && !display.isDead()) {
                    display.remove();
                }
            }
        }
    }

    public void removeAll() {
        for (TextDisplay[] displays : holograms.values()) {
            for (TextDisplay display : displays) {
                if (display != null && !display.isDead()) {
                    display.remove();
                }
            }
        }
        holograms.clear();
    }
}
