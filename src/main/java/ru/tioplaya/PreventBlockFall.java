package ru.tioplaya;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import ru.tioplaya.commands.MainTC;
import ru.tioplaya.commands.mainCommand;
import ru.tioplaya.util.configLoad;
import ru.tioplaya.util.updater;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PreventBlockFall extends JavaPlugin implements Listener {
    public void log(ChatColor color, String str) {
        Bukkit.getConsoleSender().sendMessage(color + str);
    }

    public YamlConfiguration config;
    public ConfigurationSection sectionSettings, sectionLang;
    public boolean isLang = false, pluginUpdated = false, debug, checkUpd, UserGlow, CB_highlight, AutoFall;
    public long delayForFall;
    public int count;
    public String prefix, cfgReloaded, noPerm;

    @Override
    public void onEnable() {
        //подгружаем конфиг и делаем некоторые проверки
        configLoad plug = new configLoad(this);
        plug.checkConfig();

        //bstats
        new Metrics(this, 23315);

        //проверка обновлений
        if (checkUpd) {
            new updater(this, 116900).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    getLogger().info("You are using the latest version of the plugin");
                } else {
                    getLogger().warning("There is a new update available (" + version + ").");
                    getLogger().warning("https://www.spigotmc.org/resources/preventblockfall.116900/");
                }
            });
        }

        //регистрация листенеров и команд
        Bukkit.getPluginManager().registerEvents(this, this);
        configLoad cLoad = new configLoad(this);
        Objects.requireNonNull(getCommand("PreventBlockFall")).setTabCompleter(new MainTC(this));
        Objects.requireNonNull(getCommand("PreventBlockFall")).setExecutor(new mainCommand(this, cLoad));


        //сообщения о включении
        log(ChatColor.GREEN, "PreventBlockFall enabled!");
        log(ChatColor.DARK_GREEN, "PreventBlockFall by Tioplaya");
    }

    int prevSize;
    public boolean debugCB = false;
    public boolean debugEnt = false;
    public boolean debugMiniMaps = false;
    public boolean debugMaps = false;
    public final ConcurrentHashMap<Chunk, Set<FallingBlock>> allEntitiesInRadius = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Chunk, Map<Location, Material>> cbBlock = new ConcurrentHashMap<>();

    //Удаляем невалидные сущности
    public void updateEntities() {
        allEntitiesInRadius.forEach((chunk, entities) -> {
            entities.removeIf(fallingBlock -> !fallingBlock.isValid());

            if (entities.isEmpty()) {
                allEntitiesInRadius.remove(chunk);
            } else {
                if (debug && debugEnt) {
                    if (prevSize != entities.size()) {
                        Bukkit.broadcastMessage("Chunk: " + chunk.getX() + " " + chunk.getZ() + " has " + entities.size() + " entities.");
                    }
                    prevSize = entities.size();
                }
            }
        });
    }

    //Отмена физики, запись отменённых блоков в 2 списка для текущих операций, а также подсветка сущностей =3
    @Deprecated
    @EventHandler(priority = EventPriority.LOWEST)
    public void fixFall(EntityChangeBlockEvent event) {
        if (sectionSettings != null) {
            count = sectionSettings.getInt("Count");

            //фильтрация по версии для корректной работы (всё что ниже 1.16 меньше на 1)
            String serverVersion = Bukkit.getBukkitVersion();
            String[] versionParts = serverVersion.split("\\.");
            try {
                if (versionParts.length >= 2) {
                    int majorVersion = Integer.parseInt(versionParts[0]);
                    int minorVersion = Integer.parseInt(versionParts[1]);

                    if (majorVersion == 1 && minorVersion <= 16) {
                        count++;
                    }
                }
            } catch (NumberFormatException e) {
                if (serverVersion.equalsIgnoreCase("1.13-pre7-R0.1-SNAPSHOT")) {
                    count++;
                }
            }

            Entity entity = event.getEntity();
            Chunk chunk = entity.getChunk();

            //Добавляем энтити
            Set<FallingBlock> fallingBlocks = allEntitiesInRadius.computeIfAbsent(chunk, k -> new HashSet<>());
            for (Entity entity1 : chunk.getEntities()) {
                if (entity1 instanceof FallingBlock) {
                    fallingBlocks.add((FallingBlock) entity1);
                }
            }

            //Отменяем падение и сохраняем отменённые блоки
            updateEntities();
            if (fallingBlocks.size() >= count) {
                event.setCancelled(true);
                Block block = event.getBlock();
                if (AutoFall) {
                    Location loc = block.getLocation();
                    Material material = loc.getBlock().getType();
                    if (material != Material.GLASS && material != Material.LEGACY_STAINED_GLASS) {
                        cbBlock.computeIfAbsent(chunk, k -> new HashMap<>()).put(loc, material);

                        //Меняем отменённые блоки на стекло для индикации (с ними как-то покруче =3)
                        Map<Location, Material> blockMap = cbBlock.get(chunk);
                        if (blockMap != null && !blockMap.isEmpty()) {
                            int i = 0;
                            for (Map.Entry<Location, Material> entry : blockMap.entrySet()) {
                                i++;
                                Location location = entry.getKey();
                                if (CB_highlight || debug) {
                                    if (i <= count) {
                                        location.getBlock().setType(Material.GLASS); //TODO при установке игроком непонятно как ставит
                                    }
                                }
                                if (i > count) {
                                    if (CB_highlight || debug) {
                                        location.getBlock().setType(Material.LEGACY_STAINED_GLASS);
                                    }
                                    WaitblockMap.put(location, material);
                                }
                            }
                        }
                        //TODO Если в фулл чанке поставить блоки в "ожидание", то иногда приземлившись на землю они обратно возвращаются в это
                        // состояние и принимают новый тип блока(вероятнее всего воздух) PS: также касается обычных блоков, хорошо видно когда снизу вода а сверху цемент
                    }
                }
            }


            //Подсвечиваем энтити
            if (UserGlow || debug) {
                if (entity instanceof FallingBlock) {
                    entity.setGlowing(true);
                }
            }
        }
    }

    //Отмена ломания отменённых блоков, которые ожидают свою очередь
    @EventHandler
    public void CancelCBBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Chunk chunk = loc.getChunk();

        Map<Location, Material> blockMap = cbBlock.get(chunk);
        if (blockMap != null) {
            if (blockMap.containsKey(loc)) {
                event.setCancelled(true);
            }
        }
    }

    public Map<Location, Material> blockMap = new LinkedHashMap<>();
    public Map<Location, Material> WaitblockMap = new LinkedHashMap<>();
    public Set<FallingBlock> fallingBlocks;
    Chunk debugChunk;
    @Deprecated
    @EventHandler
    public void dropBlock(EntityRemoveFromWorldEvent event) {
        EntityType entityType = event.getEntity().getType();
        if (entityType == EntityType.FALLING_BLOCK) {
            updateEntities();
            //debug mmapsa
            if (debug && debugMiniMaps && fallingBlocks != null && blockMap != null && WaitblockMap != null) {
                LinkedList<Player> OnlinePlayers = new LinkedList<>(Bukkit.getOnlinePlayers());
                for (Player onlinePlayer : OnlinePlayers) {
                    onlinePlayer.sendActionBar("Chunk: " + debugChunk.getX() + " " + debugChunk.getZ() + " FB: " + fallingBlocks.size() + " BM: " + blockMap.size() + " WBM: " + WaitblockMap.size());
                }
            }
            //debug mapsa
            if (debug && debugMaps && fallingBlocks != null) {
                LinkedList<Player> OnlinePlayers = new LinkedList<>(Bukkit.getOnlinePlayers());
                for (Player onlinePlayer : OnlinePlayers) {
                    onlinePlayer.sendActionBar("Chunk: " + debugChunk.getX() + " " + debugChunk.getZ() + " AEIR: " + allEntitiesInRadius.size() + " CB: " + cbBlock.size());
                }
            }

            //Приведение в активность зависших в воздухе блоков (очень хрупкий код)
            if (sectionSettings != null) {
                delayForFall = sectionSettings.getLong("DelayForFall");
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    for (Chunk chunk : cbBlock.keySet()) {
                        blockMap = cbBlock.get(chunk);
                        if (WaitblockMap != null && !WaitblockMap.isEmpty()) {
                            Iterator<Map.Entry<Location, Material>> iterator0 = WaitblockMap.entrySet().iterator();
                            while (iterator0.hasNext()) {
                                Map.Entry<Location, Material> entry = iterator0.next();

                                if (blockMap.size() < count) {
                                    Location loc = entry.getKey();
                                    Material mat = entry.getValue();
                                    if (CB_highlight || debug) {
                                        loc.getBlock().setType(Material.GLASS);
                                    }

                                    blockMap.put(loc, mat);
                                    iterator0.remove();
                                }
                            }
                        }
                        fallingBlocks = allEntitiesInRadius.get(chunk);
                        if (debug) {
                            debugChunk = chunk;
                        }
                        Iterator<Map.Entry<Location, Material>> iterator;
                        if (blockMap != null && !blockMap.isEmpty() && (fallingBlocks == null || fallingBlocks.isEmpty())) {
                            if (WaitblockMap.isEmpty()) { //TODO ВСЕГДА ПУСТ
                                iterator = blockMap.entrySet().iterator();
                            } else {
                                iterator = WaitblockMap.entrySet().iterator();
                            }
                            int i = 0;
                            while (iterator.hasNext()) {
                                Map.Entry<Location, Material> entry = iterator.next();
                                Location targetBlock = entry.getKey();
                                Material oldMaterialGet = entry.getValue();
                                i++;
                                if (i <= count) {
                                    if (debug) {
                                        LinkedList<Player> OnlinePlayers = new LinkedList<>(Bukkit.getOnlinePlayers());
                                        Chunk chunk1 = targetBlock.getChunk();
                                        for (Player onlinePlayer : OnlinePlayers) {
                                            onlinePlayer.sendTitle(Title.builder().title("Fall").subtitle("chunk: " + chunk1.getX() + " " + chunk1.getZ()).fadeIn(10).stay(20).fadeOut(15).build());
                                        }
                                    }

                                    targetBlock.getBlock().setType(Material.AIR);
                                    targetBlock.getBlock().setType(oldMaterialGet); //TODO oldmaterialget неправильно ставит нужно определять его в fixfall и при waitblock

                                    if (debug && debugCB) {
                                        Bukkit.broadcastMessage("cb fall = " + blockMap.size() + " type: " + oldMaterialGet.name());
                                    }
                                }
                                if (i > count) {
                                    if (CB_highlight || debug) {
                                        targetBlock.getBlock().setType(Material.LEGACY_STAINED_GLASS);
                                    }
                                    WaitblockMap.put(targetBlock, oldMaterialGet);
                                }
                                iterator.remove();

                                if (blockMap.isEmpty()) {
                                    i = 0;
                                    cbBlock.remove(chunk);
                                }
                            }
                        }
                    }
                }, delayForFall);
            }
        }
    }

    //Цели
    //TODO сделать рефакторинг кода, перенести всё что после onEnable куда-нибудь
    //TODO загрузка/выгрузка "падающих блоков" из бд и приведение в действие
    //TODO добавить поддержку разных языков посредством добавления конфигов с разными языками, в самом конфиге соответствующая настройка
    // (либо сделать файлы из которых это будет добавляться при включении/перезагрузке плагина; либо вообще сделать отдельный файл с lang). УДОБНЕЕ ВСЕГО ОТДЕЛЬНЫМ ФАЙЛОМ
    //TODO добавить поддержку ргб и minimessage в colorizeText или вовсе переделать его

    //КРИТИЧЕСКИЕ Баги:
    //TODO Если в фулл чанке игрок поставит блоки сверх лимита, то иногда при падении oldGetMaterial определяется как место на котором упал блок + заменяется стеклом на время т.е. в мапе
    //TODO если игрок ставит сверх лимита на чанке, то выборка обычного стекла и белого перемещается на новые блоки, заместо того чтобы просто переместить всё на белое (аналогично с мапами полагаю)

    //Опционально включаемые настройки в кфг:
    //TODO добавить сценарий действий в кфг при разрушении отменённого блока (например: если игрок разрушит блок , то отменённым будет считаться блок выше, а тот выпадет игроку || блок не разрушится и не приведёт в действие следующий)

    @Override
    public void onDisable() {
        log(ChatColor.RED,"plugin shutting down");
    }
}
//Проверить:
//todo чек что будет если уйти с чанка где падают блоки
//todo добавить новый readme на github и другие
//todo сделать проверку с помощью spark'а по разнице до и после включения плагина
//todo проверить всё в конфиге после команды /reload (на разных версиях)
//todo чек чё будет если значения строк конфига будут пусты
//todo тщательно поюзать плагин перед выпуском обновы
//todo чек как падают блоки с учётом того что падающие блоки ещё ставит игрок
//todo чек загрузку/выгрузку ненужных эвентов в reload
//todo чек переход с версии 1.2.1 и 1.2.2 на 1.3
//todo чек работу без дебага/с ним/с подсветкой/с индикацией блоков


// Изменено в этой версии: //todo всё это чек на остальных версиях
// - добавлено автоматическое падение отменённых от этого блоков спустя время
// - добавлен очиститель для уже ненужных сущностей и блоков
// - добавлена проверка обновлений плагина
// - добавлен контроль версий (с версии 1.2.1 данные переносятся автоматически), некоторые улучшения и нововведения конфига.
// - добавлен bstats (мне стало интересно: использует ли вообще кто-то этот ресурс?
// - Теперь репозиторий лицензирован MIT, просто потому что могу и хочу.
// - другие мелкие изменения и улучшения.
//
//В будущем плагин будет сохранять сущности/блоки по выгрузке чанка или выключению
//сервера в БД и воспроизводить падение заново при загрузке чанка с процессом.
//На данный момент отсутствие этой функции не критично.