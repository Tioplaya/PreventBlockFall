package ru.tioplaya;

import org.bukkit.*;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import ru.tioplaya.commands.TabCommands;
import ru.tioplaya.commands.reload;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;
import static org.bukkit.entity.EntityType.*;

public final class PreventBlockFall extends JavaPlugin implements Listener {
    private void log(ChatColor color, String str) {
        Bukkit.getConsoleSender().sendMessage(color + str);
    }
    public boolean debug = false;

    @Override
    public void onEnable() {
        //регистрация листенеров и команд
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("PreventBlockFall")).setExecutor(new reload(this));
        Objects.requireNonNull(getCommand("PreventBlockFall")).setTabCompleter(new TabCommands());

        //конфиг
        reload reloadCommand = new reload(this);
        reloadCommand.onCommand(getServer().getConsoleSender(), Objects.requireNonNull(getCommand("PreventBlockFall")), "reload", new String[]{"reload"});
        log(GREEN, "PreventBlockFall enabled!");
        log(DARK_GREEN, "PreventBlockFall by Tioplaya");
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void fixFall(final EntityChangeBlockEvent event) {
        List<Entity> allEntitiesInRadius = Arrays.stream(event.getEntity().getChunk().getEntities())
                .filter(entity -> entity.getType() == FALLING_BLOCK)
                .collect(Collectors.toList());
        //фильтрация по версии для корректной работы
        String serverVersion = Bukkit.getBukkitVersion();
        String[] versionParts = serverVersion.split("\\.");

        int count = getConfig().getInt("Count");
        try {
            if (versionParts.length >= 2) {
                int majorVersion = Integer.parseInt(versionParts[0]);
                int minorVersion = Integer.parseInt(versionParts[1]);

                if (majorVersion > 1 || (majorVersion == 1 && minorVersion > 16)) {
                    count--;
                }
            }
        }catch (NumberFormatException e) {
            count--;
        }
        if (allEntitiesInRadius.size() > count) {
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
    //реализовать падение блоков приведённых в не активность
    @Override
    public void onDisable() {
        log(RED,  "plugin shutting down");
    }
}
