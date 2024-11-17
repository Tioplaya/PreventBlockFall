package ru.tioplaya.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.tioplaya.PreventBlockFall;

import javax.annotation.Nonnull;
import java.util.List;

public class MainTC implements TabCompleter {
    PreventBlockFall plugin;
    public MainTC (PreventBlockFall plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 1) {
            return List.of(
                    "reload",
                    "debug"
            );
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug") && plugin.debug) {
            return List.of(
                    "all",
                    "enta",
                    "cba",
                    "mapsa",
                    "ent",
                    "maps"
            );
        }
        return null;
    }
}
