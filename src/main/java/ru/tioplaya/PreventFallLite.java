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
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;
import static org.bukkit.entity.EntityType.*;

public final class PreventFallLite extends JavaPlugin implements Listener {
    private void log(ChatColor color, String str) {
        Bukkit.getConsoleSender().sendMessage(color + str);
    }
    ColorizeText text = new ColorizeText();
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

//                        .collect(Collectors.toList()); //на 1 блок больше в 1.20.4

    @EventHandler(priority = EventPriority.HIGH)
    public void fixFall(final EntityChangeBlockEvent event) {

            List<Entity> allEntitiesInRadius = Arrays.stream(event.getEntity().getChunk().getEntities())
                    .filter(entity -> entity.getType() == FALLING_BLOCK)
//                    .toList();
                    .collect(Collectors.toList()); //на 1 блок больше в 1.20.4
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
    private final String PREFIX = text.colorizeText("prefix");
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission("pflite.admin")) {
            sender.sendMessage(PREFIX + " " + text.colorizeText("not_permission"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(PREFIX + " " + text.colorizeText("help"));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("pflite.admin")) {
                    reloadConfig();
                    saveConfig();
                    onEnable();
                    sender.sendMessage(PREFIX + " " + text.colorizeText("config_reloaded"));
                }
            } else sender.sendMessage(PREFIX + " " + text.colorizeText("unknown_command"));
        } else sender.sendMessage(PREFIX + " " + text.colorizeText("too_many_args"));
        return false;
    }
    @Override
    public void onDisable() {
        log(RED,  "plugin shutting down");
    }
}
