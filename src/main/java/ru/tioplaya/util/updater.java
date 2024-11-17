package ru.tioplaya.util;

import org.bukkit.Bukkit ;
import org.bukkit.plugin.java.JavaPlugin ;

import java.io.IOException ;
import java.io.InputStream ;
import java.net.URL ;
import java.util.Scanner ;
import java.util.function.Consumer ; //

// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates public
public class updater {

    private final JavaPlugin plugin;
    private final int resourceId;

    public updater(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId + "/~").openStream(); Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}


   

           
               

               
           

           
       
   