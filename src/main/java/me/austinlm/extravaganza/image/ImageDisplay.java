package me.austinlm.extravaganza.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import me.austinlm.extravaganza.ExtravaganzaPlugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

public class ImageDisplay {

    private final Runnable task;
    private int taskId = -1;

    public ImageDisplay(String url, Location where, double scale) throws IOException {
        File file = new File(ExtravaganzaPlugin.getInstance().getDataFolder(), "test.png");
        if (file.exists()) {
            file.delete();
        }

        FileUtils.copyURLToFile(new URL(url), file);

        BufferedImage image = ImageIO.read(file);

        ImageParticles particles = new ImageParticles(image, 2);
        particles.setDisplayRatio(scale);

        Map<Location, Color> particle = particles
            .getParticles(where, where.getPitch(), where.getYaw());

        this.task = () -> {
            for (Entry<Location, Color> entry : particle.entrySet()) {
                where.getWorld().spawnParticle(
                    Particle.REDSTONE, entry.getKey(), 1,
                    new DustOptions(entry.getValue(), 2.2f));
            }
        };
    }

    public void spawn() {
        if (this.taskId != -1) return;
        this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(ExtravaganzaPlugin.getInstance(), this.task, 1, 20).getTaskId();
    }

    public void despawn() {
        if (this.taskId == -1) return;
        Bukkit.getScheduler().cancelTask(this.taskId);
        this.taskId = -1;
    }
}
