package ru.tioplaya.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.tioplaya.ColorizeText;
import ru.tioplaya.PreventBlockFall;

import javax.annotation.Nonnull;

public class reload implements CommandExecutor {
    ColorizeText text = new ColorizeText();
    PreventBlockFall plugin;
    public reload(PreventBlockFall plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        String PREFIX = plugin.getConfig().getString("prefix");
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("pbf.admin")) {
                    if (!plugin.ConfigCheck.exists()) {
                        plugin.saveResource("config.yml", false);
                    }
                    plugin.reloadConfig();
                    sender.sendMessage(text.colorizeText(PREFIX + " " + plugin.getConfig().getString("config_reloaded")));
                } else sender.sendMessage(text.colorizeText(PREFIX + " " + plugin.getConfig().getString("not_permission")));
                return true;
            }
        } else return false;
        return false;
    }
}
