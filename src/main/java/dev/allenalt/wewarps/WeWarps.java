package dev.allenalt.wewarps;

import dev.allenalt.wewarps.command.*;
import dev.allenalt.wewarps.manager.WarpManager;
import dev.allenalt.wewarps.util.WarpTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class WeWarps extends JavaPlugin {

    private static WeWarps instance;
    private WarpManager warpManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // in case you extend plugin config later
        // Save default message and warps templates if not present
        saveResource("messages.yml", false);
        saveResource("warps.yml", false);

        // Initialize warp manager (loads warps.yml)
        this.warpManager = new WarpManager(this);
        getLogger().info("WeWarps v" + getDescription().getVersion() + " enabled.");

        // Register commands
        this.getCommand("warp").setExecutor(new WarpCommand(this.warpManager));
        this.getCommand("setwarp").setExecutor(new SetWarpCommand(this.warpManager));
        this.getCommand("delwarp").setExecutor(new DelWarpCommand(this.warpManager));
        this.getCommand("warps").setExecutor(new WarpsListCommand(this.warpManager));

        // Tab completer for warp names + admin
        WarpTabCompleter tab = new WarpTabCompleter(this.warpManager);
        this.getCommand("warp").setTabCompleter(tab);
        this.getCommand("delwarp").setTabCompleter(tab);
        this.getCommand("setwarp").setTabCompleter(tab);
        this.getCommand("warps").setTabCompleter(tab);
    }

    @Override
    public void onDisable() {
        // Persist warps on shutdown
        if (this.warpManager != null) {
            this.warpManager.save();
        }
        getLogger().info("WeWarps disabled.");
    }

    public static WeWarps getInstance() {
        return instance;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
}
