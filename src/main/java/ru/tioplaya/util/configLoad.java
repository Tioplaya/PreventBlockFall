package ru.tioplaya.util;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.tioplaya.PreventBlockFall;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class configLoad {
    PreventBlockFall plugin;
    public configLoad(PreventBlockFall plugin) {
        this.plugin = plugin;
    }

    //проверка значений на существование и сохранение в кфг
    public void checkConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.config = YamlConfiguration.loadConfiguration(configFile);

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        } else {
            plugin.sectionSettings = plugin.config.getConfigurationSection("settings");
            plugin.sectionLang = plugin.config.getConfigurationSection("lang"); // TODO хз надо ли это вообще

            //комментарии в начале конфига
            plugin.config.options().header("Count - Maximum number of falling entities per chunk.\n" +
                    "DelayForFall - Time (ticks) before next blocks fall (after previous blocks drop items)\n" +
                    "AutoFall - If true, then automatically makes blocks fall that are not in the range (count*),\n" +
                    "when previous blocks disappear. If false, then the \"CB_highlight\" function will not work\n" +
                    "because it is useless in this case. \n" +
                    "Glow - Falling blocks will be highlighted\n" +
                    "CB_highlight - temporarily flying blocks will be replaced on glass for understand what is happening\n" +
                    "Don't delete version! If u delete this - future updates edit config incorrectly");
            //проверка наличия секций
            if (plugin.sectionSettings == null) {
                plugin.config.createSection("settings");
                plugin.sectionSettings = plugin.config.getConfigurationSection("settings");
                if (plugin.debug) {
                    plugin.log(ChatColor.RED, "settings");
                }
            }
            if (plugin.sectionLang == null) {
                plugin.config.createSection("lang");
                plugin.sectionLang = plugin.config.getConfigurationSection("lang");
                if (plugin.debug) {
                    plugin.log(ChatColor.RED, "lang");
                }
            }

            //Контроль версий
            int oldCount = 25;
            boolean oldDebug = false;
            String oldPrefix = "&8[&f&lPBF&8] ->", oldCfgReload = "&aConfig successfully reloaded!", oldNoPerm = "&cNo permission!";
            String version = plugin.getDescription().getVersion();
            Object[] Znachenie = new Object[0];
            if (!Objects.equals(plugin.config.getString("version"), version)) {
                //всё, что ниже 1.3
                if (plugin.config.getString("version") == null) {
                    int countValue = plugin.config.getInt("Count");
                    boolean debugValue = plugin.config.getBoolean("Debug");
                    String prefixValue = plugin.config.getString("prefix");
                    String cfgReloadValue = plugin.config.getString("config_reloaded");
                    String nPermValue = plugin.config.getString("not_permission");

                    String[] stroke = new String[]{"Count", "Debug", "prefix", "config_reloaded", "not_permission"}; //TODO сократить
                    Znachenie = new Object[] {oldCount, oldDebug, oldPrefix, oldCfgReload, oldNoPerm};
                    Object[] prisvaivaem = new Object[] {countValue, debugValue, prefixValue, cfgReloadValue, nPermValue};
                    for (int i = 0; i < stroke.length; i++) {
                        if (plugin.config.get(stroke[i]) != null) {
                            Znachenie[i] = prisvaivaem[i];
                        }
                        plugin.config.set(stroke[i], null);
                    }
                    plugin.config.set("too_many_args", null);
                }

                plugin.config.set("version", plugin.getDescription().getVersion());
                plugin.pluginUpdated = true;
                if (plugin.debug) {
                    plugin.log(ChatColor.RED, "version != plugin version");
                }
            } else {
                plugin.pluginUpdated = false;
            }

            //проверка наличия значений в секциях
            Object[] values;
            if (plugin.sectionSettings != null && plugin.sectionLang != null) {
                String[] stroki = new String[]{"Count", "DelayForFall", "AutoFall", "CB_highlight", "Glow", "CheckUpdates", "prefix", "config_reloaded", "no_permission"}; //TODO сократить (вообще эти два перебора очень похожи)
                if (!plugin.pluginUpdated) {
                    values = new Object[]{25, 60, true, false, false, true, "&8[&f&lPBF&8] ->", "&aConfig successfully reloaded!", "&cNo permission!"};
                } else {
                    values = new Object[]{Znachenie[0], 60, true, false, false, true, Znachenie[2], Znachenie[3], Znachenie[4]};
                }

                for (int i = 0; i < stroki.length; i++) {
                    plugin.isLang = stroki[i].contains("prefix") || stroki[i].contains("config_reloaded") || stroki[i].contains("no_permission");
                    ConfigurationSection section = (plugin.isLang ? plugin.sectionLang : plugin.sectionSettings);
                    if (section.get(stroki[i]) == null) {
                        section.set(stroki[i], values[i]);
                        if (plugin.debug) {
                            plugin.log(ChatColor.RED, stroki[i]);
                        }
                    }
                }
            }
            if (plugin.config.get("debug") == null) { //TODO сократить код
                if (!plugin.pluginUpdated) {
                    plugin.config.set("debug", false);
                } else {
                    plugin.config.set("debug", Znachenie[1]);
                }
                if (plugin.debug) {
                    plugin.log(ChatColor.RED, "debug");
                }
            }
            if (plugin.config.get("version") == null) {
                plugin.config.set("version", plugin.getDescription().getVersion());
                if (plugin.debug) {
                    plugin.log(ChatColor.RED, "version");
                }
            }

            //сохранение новых строк
            try {
                plugin.config.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //приведение значений к переменным для дальнейшего использования
        plugin.reloadConfig();
        try {
            plugin.config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        plugin.sectionSettings = plugin.config.getConfigurationSection("settings");
        plugin.sectionLang = plugin.config.getConfigurationSection("lang");
        if (plugin.sectionSettings != null && plugin.sectionLang != null) {
            plugin.count = plugin.sectionSettings.getInt("Count");
            plugin.delayForFall = plugin.sectionSettings.getLong("DelayForFall");
            plugin.AutoFall = plugin.sectionSettings.getBoolean("AutoFall");
            plugin.CB_highlight = plugin.sectionSettings.getBoolean("CB_highlight");
            plugin.UserGlow = plugin.sectionSettings.getBoolean("Glow");
            plugin.checkUpd = plugin.sectionSettings.getBoolean("CheckUpdates");
            plugin.prefix = plugin.sectionLang.getString("prefix");
            plugin.cfgReloaded = plugin.sectionLang.getString("config_reloaded"); // todo переделать если для получения языка будет использоваться другой файл
            plugin.noPerm = plugin.sectionLang.getString("no_permission");
            plugin.debug = plugin.config.getBoolean("debug");
        } else {
            plugin.getLogger().severe("If you see this in the console, tell the developer on Discord that he is an idiot and describe how you got this error. \nlink: https://discord.gg/C3kzKrxw34");
        }

        if (plugin.debug) {
            plugin.log(ChatColor.RED, "count: " + plugin.count + " delayForFall: " + plugin.delayForFall + " autoFall: " + plugin.AutoFall + " cb_highlight: " + plugin.CB_highlight +  " glow: " + plugin.UserGlow + " checkUpd: " + plugin.checkUpd + " debug: true");
        }
    }
}
