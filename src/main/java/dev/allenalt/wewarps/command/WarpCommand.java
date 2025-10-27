package dev.allenalt.wewarps.command;

import dev.allenalt.wewarps.manager.Warp;
import dev.allenalt.wewarps.manager.WarpManager;
import dev.allenalt.wewarps.WeWarps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;
import java.util.concurrent.CompletionException;

/**
 * Teleports player to warp: /warp <name>
 */
public class WarpCommand implements CommandExecutor {

    private final WarpManager manager;
    private final FileConfiguration messages;

    public WarpCommand(WarpManager manager) {
        this.manager = manager;
        this.messages = WeWarps.getInstance().getConfig(); // not used; we read messages.yml directly below
    }

    private String msg(String key) {
        return WeWarps.getInstance().getResourceBundle() == null ? "" : "";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Read messages.yml manually
        FileConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.File(WeWarps.getInstance().getDataFolder(), "messages.yml")
        );

        if (!(sender instanceof Player)) {
            sender.sendMessage(cfg.getString("messages.only-players", "&cOnly players can use that command.").replace("&", "§"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("wewarps.use")) {
            player.sendMessage(cfg.getString("messages.no-permission", "&cYou don't have permission to do that.").replace("&", "§"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(cfg.getString("messages.invalid-usage", "&cUsage: {usage}")
                    .replace("{usage}", "/warp <name>").replace("&", "§"));
            return true;
        }

        String name = args[0].toLowerCase();
        Optional<Warp> opt = manager.getWarp(name);
        if (!opt.isPresent()) {
            player.sendMessage(cfg.getString("messages.warp-not-found", "&cWarp '{warp}' not found.")
                    .replace("{warp}", name).replace("&", "§"));
            return true;
        }

        Warp warp = opt.get();
        player.sendMessage(cfg.getString("messages.warp-teleport", "&aTeleporting to warp '{warp}'...")
                .replace("{warp}", warp.getName()).replace("&", "§"));

        // Use modern Paper teleport API: teleportAsync
        try {
            player.teleportAsync(warp.getLocation())
                .thenRun(() -> {
                    // Success — nothing to do, but could send a confirmation or play a sound
                })
                .exceptionally(ex -> {
                    player.sendMessage("§cTeleport failed: " + ex.getMessage());
                    WeWarps.getInstance().getLogger().warning("Teleport exception for player " + player.getName() + " to warp " + warp.getName() + ": " + ex.getMessage());
                    return null;
                });
        } catch (NoSuchMethodError | CompletionException ex) {
            // In case older API, fallback
            player.teleport(warp.getLocation());
        }

        return true;
    }
}
