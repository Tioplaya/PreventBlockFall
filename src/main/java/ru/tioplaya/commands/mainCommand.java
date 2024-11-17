package ru.tioplaya.commands;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import ru.tioplaya.PreventBlockFall;
import ru.tioplaya.util.ColorizeText;
import ru.tioplaya.util.configLoad;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class mainCommand implements CommandExecutor {
    ColorizeText text = new ColorizeText();
    PreventBlockFall plugin;
    configLoad plug;
    public mainCommand(PreventBlockFall plugin, configLoad plug) {
        this.plugin = plugin;
        this.plug = plug;
    }
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {

        //если нет прав
        if (!sender.hasPermission("pbf.admin")) {
            sender.sendMessage(text.colorizeText(plugin.prefix + " " + plugin.noPerm));
        }

        //reload
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("pbf.admin")) {
                    plug.checkConfig();
                    plugin.debugEnt = false;
                    plugin.debugCB = false;
                    plugin.debugMaps = false;
                    plugin.debugMiniMaps = false;

                    //Отключаем слушатели за ненадобностью
                    if (!plugin.AutoFall) {
                        BlockBreakEvent.getHandlerList().unregister((Plugin) plugin);
                        EntityRemoveFromWorldEvent.getHandlerList().unregister((Plugin) plugin);
                    } else {
                        Bukkit.getPluginManager().registerEvents(plugin, plugin);
                    }

                    sender.sendMessage(text.colorizeText(plugin.prefix + " " + plugin.cfgReloaded));
                }
                return true;
            }
        }

        //debug
        if (args.length == 2) {
            if (plugin.debug) {
                if (args[0].equalsIgnoreCase("debug")) {
                    if (sender.hasPermission("pbf.admin")) {

                        String message = args[1].toLowerCase();
                        switch (message) {
                            case "all":
                                plugin.debugEnt = !plugin.debugEnt;
                                plugin.debugCB = !plugin.debugCB;
                                plugin.debugMiniMaps = !plugin.debugMiniMaps;
                                String allEnabled = (plugin.debugEnt && plugin.debugCB && plugin.debugMiniMaps) ? "enabled!" : "disabled!";
                                Bukkit.broadcastMessage("all mod's " + allEnabled);
                                break;

                            case "enta":
                                plugin.debugEnt = !plugin.debugEnt;
                                String entaEnabled = (plugin.debugEnt) ? "enabled!" : "disabled!";
                                Bukkit.broadcastMessage("\n\n\"enta\" " + entaEnabled);
                                break;

                            case "cba":
                                plugin.debugCB = !plugin.debugCB;
                                String cbaEnabled = (plugin.debugCB) ? "enabled!" : "disabled!";
                                Bukkit.broadcastMessage("\n\n\"cba\" " + cbaEnabled);
                                break;

                            case "mmapsa":
                                Bukkit.broadcastMessage("\n\n");
                                if (plugin.debugMaps) {
                                    plugin.debugMaps = false;
                                    Bukkit.broadcastMessage("\"mapsa\" disabled!");
                                }
                                plugin.debugMiniMaps = !plugin.debugMiniMaps;
                                String mmapsEnabled = (plugin.debugMiniMaps) ? "enabled!" : "disabled!";
                                Bukkit.broadcastMessage("\"mmapsa\" " + mmapsEnabled);
                                break;
                            case "mapsa":
                                Bukkit.broadcastMessage("\n\n");
                                if (plugin.debugMiniMaps) {
                                    plugin.debugMiniMaps = false;
                                    Bukkit.broadcastMessage("\"mmapsa\" disabled!");
                                }
                                plugin.debugMaps = !plugin.debugMaps;
                                String mapsEnabled = (plugin.debugMaps) ? "enabled!" : "disabled!";
                                Bukkit.broadcastMessage("\"mapsa\" " + mapsEnabled);
                                break;

                            case "ent":
                                Bukkit.broadcastMessage("\n\n");
                                if (!plugin.allEntitiesInRadius.keySet().isEmpty()) {
                                    for (Map.Entry<Chunk, Set<FallingBlock>> entry : plugin.allEntitiesInRadius.entrySet()) {
                                        Chunk chunk = entry.getKey();
                                        Set<FallingBlock> fallingBlock = entry.getValue();
                                        Bukkit.broadcastMessage("Chunk: " + chunk.getX() + " " + chunk.getZ() + " ent: " + fallingBlock.size());
                                    }
                                } else Bukkit.broadcastMessage("ent is empty");
                                break;

                            case "cb":
                                Bukkit.broadcastMessage("\n\n");
                                if (!plugin.cbBlock.keySet().isEmpty()) {
                                    for (Map.Entry<Chunk, Map<Location, Material>> entry : plugin.cbBlock.entrySet()) {
                                        Chunk chunk = entry.getKey();
                                        Bukkit.broadcastMessage("Chunk: " + chunk.getX() + " " + chunk.getZ() + " cb: " + entry.getValue().entrySet().size());
                                    }
                                } else Bukkit.broadcastMessage("ent is empty");
                                break;

                            case "maps":
                                Bukkit.broadcastMessage("\n\n");
                                if (!plugin.allEntitiesInRadius.isEmpty()) {
                                    Bukkit.broadcastMessage("aeir size: " + plugin.allEntitiesInRadius.size());
                                } else Bukkit.broadcastMessage("aeir is empty");

                                if (!plugin.cbBlock.isEmpty()) {
                                    Bukkit.broadcastMessage("cb size: " + plugin.cbBlock.size());
                                } else Bukkit.broadcastMessage("cb is empty");

                                if (plugin.fallingBlocks != null) {
                                    if (!plugin.fallingBlocks.isEmpty()) {
                                        Bukkit.broadcastMessage("fb size: " + plugin.fallingBlocks.size());
                                    } else Bukkit.broadcastMessage("fb is empty");
                                } else Bukkit.broadcastMessage("fb is null");

                                if (plugin.blockMap != null) {
                                    if (!plugin.blockMap.isEmpty()) {
                                        Bukkit.broadcastMessage("bm size: " + plugin.blockMap.size());
                                    } else Bukkit.broadcastMessage("bm is empty");
                                } else Bukkit.broadcastMessage("bm is null");

                                if (plugin.WaitblockMap != null) {
                                    if (!plugin.WaitblockMap.isEmpty()) {
                                        Bukkit.broadcastMessage("wbm size: " + plugin.WaitblockMap.size());
                                    } else Bukkit.broadcastMessage("wbm is empty");
                                } else Bukkit.broadcastMessage("wbm is null");
                                break;

                            default:
                                Bukkit.broadcastMessage("\n\nList of debug mods: all, enta, cba, mapsa, ent, maps");
                        }
                        return true;
                    }
                }
            } else {
                sender.sendMessage("Debug command is disabled. Enable in config");
                return true;
            }
        }
        return false;
    }
}
