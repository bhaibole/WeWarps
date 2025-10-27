package dev.allenalt.wewarps.command;

import dev.allenalt.wewarps.manager.Warp;
import dev.allenalt.wewarps.manager.WarpManager;
import dev.allenalt.wewarps.WeWarps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletionException;

/**
 * Handles /warp <name> command.
 */
public class WarpCommand implements CommandExecutor {

    private final WarpManager manager;
    private final File messagesFile;

    public WarpCommand(WarpManager manager) {
        this.manager = manager;
        this.messagesFile = new File(WeWarps.getInstance().getDataFolder(), "messages.yml");
    }

    private String msg(String path, String def) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(messagesFile);
        return cfg.getString(path, def).replace("&", "§");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.only-players", "&cOnly players can use that command."));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("wewarps.use")) {
            player.sendMessage(msg("messages.no-permission", "&cYou don't have permission to do that."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(msg("messages.invalid-usage", "&cUsage: {usage}")
                    .replace("{usage}", "/warp <name>"));
            return true;
        }

        String name = args[0].toLowerCase();
        Optional<Warp> opt = manager.getWarp(name);

        if (!opt.isPresent()) {
            player.sendMessage(msg("messages.warp-not-found", "&cWarp '{warp}' not found.")
                    .replace("{warp}", name));
            return true;
        }

        Warp warp = opt.get();

        player.sendMessage(msg("messages.warp-teleport", "&aTeleporting to warp '{warp}'...")
                .replace("{warp}", warp.getName()));

        // Use modern Paper API teleport
        try {
            player.teleportAsync(warp.getLocation())
                    .thenRun(() -> { /* success */ })
                    .exceptionally(ex -> {
                        player.sendMessage("§cTeleport failed: " + ex.getMessage());
                        WeWarps.getInstance().getLogger().warning("Teleport error for player "
                                + player.getName() + " to warp " + warp.getName() + ": " + ex.getMessage());
                        return null;
                    });
        } catch (NoSuchMethodError | CompletionException ex) {
            // Fallback if Paper async teleport isn't available
            player.teleport(warp.getLocation());
        }

        return true;
    }
}
