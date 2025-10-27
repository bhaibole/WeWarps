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
import java.util.List;

public class WarpsListCommand implements CommandExecutor {

    private final WarpManager manager;
    private final File messagesFile;

    public WarpsListCommand(WarpManager manager) {
        this.manager = manager;
        this.messagesFile = new File(WeWarps.getInstance().getDataFolder(), "messages.yml");
    }

    private String msg(String path, String def) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(messagesFile);
        return cfg.getString(path, def).replace("&", "§");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("wewarps.use")) {
            sender.sendMessage(msg("messages.no-permission", "&cYou don't have permission to do that."));
            return true;
        }

        List<Warp> warps = manager.listWarps();
        sender.sendMessage(msg("messages.warp-list-title", "&6Available Warps (&e{count}&6):").replace("{count}", String.valueOf(warps.size())));

        if (warps.isEmpty()) {
            sender.sendMessage("§7- none -");
            return true;
        }

        for (Warp w : warps) {
            String line = msg("messages.warp-list-item", "&7- &e{warp} &f- &b{owner} &f- &d{description}")
                    .replace("{warp}", w.getName())
                    .replace("{owner}", w.getOwner())
                    .replace("{description}", w.getDescription() == null ? "" : w.getDescription());
            sender.sendMessage(line);
        }

        return true;
    }
}
