package dev.allenalt.wewarps.manager;

import dev.allenalt.wewarps.WeWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles loading/saving warps to warps.yml
 */
public class WarpManager {

    private final Plugin plugin;
    private final File warpsFile;
    private final FileConfiguration warpsConfig;

    // Map of warp name -> Warp
    private final Map<String, Warp> warps = new HashMap<>();

    public WarpManager(WeWarps plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!warpsFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        loadWarps();
    }

    private void loadWarps() {
        warps.clear();
        for (String key : warpsConfig.getKeys(false)) {
            try {
                String path = key + ".";
                String worldName = warpsConfig.getString(path + "world");
                double x = warpsConfig.getDouble(path + "x");
                double y = warpsConfig.getDouble(path + "y");
                double z = warpsConfig.getDouble(path + "z");
                float yaw = (float) warpsConfig.getDouble(path + "yaw", 0.0);
                float pitch = (float) warpsConfig.getDouble(path + "pitch", 0.0);
                String owner = warpsConfig.getString(path + "owner", "server");
                String desc = warpsConfig.getString(path + "description", "");

                if (worldName == null) continue;
                if (Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("Warp '" + key + "' references missing world '" + worldName + "'. Skipping.");
                    continue;
                }

                Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                Warp warp = new Warp(key.toLowerCase(), loc, owner, desc);
                warps.put(warp.getName(), warp);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to load warp: " + key, ex);
            }
        }
        plugin.getLogger().info("Loaded " + warps.size() + " warp(s).");
    }

    public synchronized void save() {
        try {
            // Clear file then write
            for (String key : new HashSet<>(warpsConfig.getKeys(false))) {
                warpsConfig.set(key, null);
            }
            for (Warp warp : warps.values()) {
                String path = warp.getName() + ".";
                Location l = warp.getLocation();
                warpsConfig.set(path + "world", l.getWorld().getName());
                warpsConfig.set(path + "x", l.getX());
                warpsConfig.set(path + "y", l.getY());
                warpsConfig.set(path + "z", l.getZ());
                warpsConfig.set(path + "yaw", l.getYaw());
                warpsConfig.set(path + "pitch", l.getPitch());
                warpsConfig.set(path + "owner", warp.getOwner());
                warpsConfig.set(path + "description", warp.getDescription());
            }
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save warps.yml", e);
        }
    }

    public synchronized Optional<Warp> getWarp(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(warps.get(name.toLowerCase()));
    }

    public synchronized List<Warp> listWarps() {
        return new ArrayList<>(warps.values());
    }

    public synchronized boolean createWarp(String name, Location loc, String owner, String description) {
        String key = name.toLowerCase();
        if (warps.containsKey(key)) return false;
        Warp warp = new Warp(key, loc, owner, description == null ? "" : description);
        warps.put(key, warp);
        save(); // persist immediately
        plugin.getLogger().info("[WeWarps] Created warp '" + key + "' by " + owner);
        return true;
    }

    public synchronized boolean deleteWarp(String name) {
        String key = name.toLowerCase();
        if (!warps.containsKey(key)) return false;
        warps.remove(key);
        save(); // persist immediately
        plugin.getLogger().info("[WeWarps] Deleted warp '" + key + "'");
        return true;
    }

    public synchronized Set<String> getWarpNames() {
        return new HashSet<>(warps.keySet());
    }
}
