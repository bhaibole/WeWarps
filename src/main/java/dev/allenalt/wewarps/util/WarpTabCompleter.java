package dev.allenalt.wewarps.util;

import dev.allenalt.wewarps.manager.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion for warp commands:
 * - /warp <partial> suggests available warps
 * - /delwarp <partial> suggests warps (admin)
 * - /setwarp suggests nothing (or existing names)
 */
public class WarpTabCompleter implements TabCompleter {

    private final WarpManager manager;

    public WarpTabCompleter(WarpManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        String cmd = command.getName().toLowerCase();
        if (args.length == 0) return suggestions;

        // When completing the first arg (name)
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            suggestions.addAll(
                    manager.getWarpNames().stream()
                            .filter(n -> n.startsWith(partial))
                            .sorted()
                            .collect(Collectors.toList())
            );
        }

        return suggestions;
    }
}
