package dev.allenalt.wewarps.command;

import dev.allenalt.wewarps.manager.WarpManager;
import dev.allenalt.wewarps.WeWarps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class SetWarpCommand implements CommandExecutor {

    private final WarpManager manager;
    private final File messagesFile;

    public SetWarpCommand(WarpManager manager) {
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

        if (args.length < 1) {
            player.sendMessage(msg("messages.invalid-usage", "&cUsage: {usage}").replace("{usage}", "/setwarp <name>"));
            return true;
        }

        String name = args[0].toLowerCase();
        String description = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "";

        if (manager.getWarp(name).isPresent()) {
            player.sendMessage(msg("messages.warp-already-exists", "&cA warp named '{warp}' already exists.")
                    .replace("{warp}", name));
            return true;
        }

        boolean created = manager.createWarp(name, player.getLocation(), player.getName(), description);
        if (created) {
            player.sendMessage(msg("messages.warp-created", "&aWarp '{warp}' created.").replace("{warp}", name));
        } else {
            player.sendMessage("§cFailed to create warp.");
        }

        return true;
    }
}
