package ru.tioplaya;

import org.bukkit.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.*;
import static org.bukkit.entity.EntityType.*;

public final class PreventFallLite extends JavaPlugin implements Listener {
    private void log(ChatColor color, String str) {
        Bukkit.getConsoleSender().sendMessage(color + str);
    }

    boolean debug;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        log(GREEN, "PreventFallLite enabled!");
        log(DARK_GREEN, "PreventFallLite by Tioplaya");
        if (getConfig().getBoolean("Debug")) {
            debug = true;
            getLogger().log(Level.WARNING, "Debug enabled. Disable in config if you can't wanna see it");
        } else debug = false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void fixFall(final EntityChangeBlockEvent event) {
        List<Entity> allEntitiesInRadius = Arrays.stream(event.getEntity().getChunk().getEntities())
                .filter(entity -> entity.getType() == FALLING_BLOCK)
                .toList();
        if (allEntitiesInRadius.size() > getConfig().getInt("Count")) {
            if (event.getEntity() instanceof FallingBlock) {
                event.setCancelled(true);
                allEntitiesInRadius.forEach(entity -> {
                    if (entity.hasGravity()) {
                        event.setCancelled(true);
                    }
                    if (debug) {
                        entity.setGlowing(true);
                    }
                });
            }
        }
    }

//реализовать дроп от паутины
    //реализовать падение блоков приведённых в неактивность

    public String colorizeText(String text) {
        String configValue = (String) this.getConfig().get(text);
        if (configValue == null) {
            return text;
        }
        text = configValue.replaceAll("&", "§");
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String hexCode = text.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("&").append(c);
            text = text.replace(hexCode, builder.toString());
            matcher = pattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    private final String PREFIX = colorizeText("prefix");
    private final String NOT_PERMISSION = colorizeText("not_permission");
    private final String CONFIG_RELOADED = colorizeText("config_reloaded");
    private final String HELP = colorizeText("help");
    private final String UNKNOWN_COMMAND = colorizeText("unknown_command");
    private final String too_many_args = colorizeText("too_many_args");
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("pflite.admin")) {
            sender.sendMessage(PREFIX + " " + NOT_PERMISSION);
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(PREFIX + " " + HELP);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("pflite.admin")) {
                    reloadConfig();
                    saveConfig();
                    onEnable();
                    sender.sendMessage(PREFIX + " " + CONFIG_RELOADED);
                }
            } else sender.sendMessage(PREFIX + " " + UNKNOWN_COMMAND);
        } else sender.sendMessage(PREFIX + " " + too_many_args);
        return false;
    }
    @Override
    public void onDisable() {
        log(RED,  "plugin shutting down");
    }
}
