package me.austinlm.extravaganza.image;

import me.austinlm.extravaganza.ExtravaganzaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ImageManager implements Listener {

    private final ImageDisplay[] images;
    private boolean spawned = false;

    public ImageManager(ImageDisplay... images) {
        this.images = images;
    }

    public void despawn() {
        if (!spawned) return;
        spawned = false;
        for (ImageDisplay image : images) {
            image.despawn();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!spawned) {
            for (ImageDisplay image : images) {
                image.spawn();
            }
            spawned = true;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(ExtravaganzaPlugin.getInstance(), () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                despawn();
            }
        }, 30);
    }
}
