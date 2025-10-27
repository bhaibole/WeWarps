package dev.allenalt.wewarps.command;

import dev.allenalt.wewarps.manager.WarpManager;
import dev.allenalt.wewarps.WeWarps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class DelWarpCommand implements CommandExecutor {

    private final WarpManager manager;
    private final File messagesFile;

    public DelWarpCommand(WarpManager manager) {
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

        if (!player.hasPermission("wewarps.admin")) {
            player.sendMessage(msg("messages.no-permission", "&cYou don't have permission to do that."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(msg("messages.invalid-usage", "&cUsage: {usage}").replace("{usage}", "/delwarp <name>"));
            return true;
        }

        String name = args[0].toLowerCase();
        if (!manager.getWarp(name).isPresent()) {
            player.sendMessage(msg("messages.warp-not-found", "&cWarp '{warp}' not found.").replace("{warp}", name));
            return true;
        }

        boolean deleted = manager.deleteWarp(name);
        if (deleted) {
            player.sendMessage(msg("messages.warp-deleted", "&aWarp '{warp}' deleted.").replace("{warp}", name));
        } else {
            player.sendMessage("§cFailed to delete warp.");
        }

        return true;
    }
}
