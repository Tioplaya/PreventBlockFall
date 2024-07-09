package ru.tioplaya.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.tioplaya.ColorizeText;
import ru.tioplaya.PreventFallLite;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;


public class reload implements CommandExecutor {
    ColorizeText text = new ColorizeText();
    PreventFallLite plugin;
    public reload(PreventFallLite plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        final String PREFIX = text.colorizeText(config.getString("prefix"));

        if (args.length > 1) {
            sender.sendMessage(PREFIX + " " + text.colorizeText(config.getString("too_many_args")));
            return true;
        }

        if (!sender.hasPermission("pfl.admin")) {
            sender.sendMessage(PREFIX + " " + text.colorizeText(config.getString("not_permission")));
        }

        try {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("pfl.admin")) {
                    Bukkit.getScheduler().cancelTasks(plugin);
                    File CreateconfigName = new File(plugin.getDataFolder(), "config.yml");
                    File CreateFolderEvent = plugin.getDataFolder();

                    if (!CreateFolderEvent.exists()) {
                        boolean folderCreated = CreateFolderEvent.mkdirs();
                        if (!folderCreated) {
                            plugin.getLogger().warning("Failed to create plugin folder, report it to developer");
                        }
                    }

                    if (!CreateconfigName.exists() || CreateconfigName.length() == 0) {
                        InputStream CreateInputStream = plugin.getResource("config.yml");
                        if (CreateInputStream != null) {
                            try {
                                plugin.getLogger().info("Config" + " " + "config.yml" + " " + "successfully created");
                                Files.copy(CreateInputStream, CreateconfigName.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            plugin.getLogger().severe("Failed to create config, report it to developer");
                        }
                    }
                    if (CreateconfigName.exists() && CreateFolderEvent.exists() && !config.getKeys(false).isEmpty()) {
                        plugin.reloadConfig();
                        plugin.getConfig().options().copyDefaults(true);
                        plugin.saveConfig();

                        if (plugin.getConfig().getBoolean("Debug")) {
                            plugin.debug = true;
                            plugin.getLogger().log(Level.WARNING, "Debug enabled. Disable in config if you can't wanna see it");
                        } else plugin.debug = false;
                        if (config.contains("config_reloaded")) {
                            sender.sendMessage(PREFIX + " " + text.colorizeText(config.getString("config_reloaded")));
                          }
                    }
                    return true;
                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }
}
