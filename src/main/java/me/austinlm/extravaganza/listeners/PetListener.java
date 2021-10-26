package me.austinlm.extravaganza.listeners;

import me.austinlm.extravaganza.ExtravaganzaPlugin;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.level.World;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PetListener implements Listener {

    private final EntityType type;
    private final String name;
    private Entity entity;
    private int taskId = -1;

    public PetListener(EntityType type, String name) {
        this.type = type;
        this.name = name;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!ExtravaganzaPlugin.isBirthday(event.getPlayer())) {
            return;
        }

        if (entity == null) {
            entity = event.getPlayer().getWorld().spawnEntity(event.getPlayer().getLocation(), this.type);
            entity.setCustomNameVisible(true);
            entity.setCustomName(ChatColor.GOLD + this.name);
            entity.setInvulnerable(true);
        }
        if (entity instanceof Tameable) {
            ((Tameable) entity).setTamed(true);
            ((Tameable) entity).setOwner(event.getPlayer());
        } else {
            EntityLiving entityLiving = ((CraftLivingEntity)entity).getHandle();
            if (entityLiving instanceof EntityInsentient entityInsentient) {
                World world = entityInsentient.getWorld();
                PathfinderGoalSelector goalSelector = new PathfinderGoalSelector(world.getMethodProfilerSupplier());
                goalSelector.a(0, new PathfinderGoal() {
                    public boolean a() {
                        return true;
                    }

                    public void e() {
                        entityInsentient.getControllerLook().a(event.getPlayer().getLocation().getX(), event.getPlayer().getEyeLocation().getY(), event.getPlayer().getLocation().getZ(), (float)entityInsentient.fb(), (float)entityInsentient.eZ());
                        if (event.getPlayer().getLocation().distanceSquared(new Location(event.getPlayer().getWorld(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ())) <= ((10 * 10) + 0.1f))
                            return;
                        entityInsentient.getNavigation().a(event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(), event.getPlayer().getLocation().getZ(), 1D);
                    }
                });

                entityInsentient.bP = goalSelector;
            } else {
                throw new IllegalArgumentException(entityLiving.getClass().getSimpleName() + " is not an instance of an EntityInsentient.");
            }
        }
        if (taskId == -1) {
            this.taskId = Bukkit.getScheduler().runTaskTimer(ExtravaganzaPlugin.getInstance(), () -> {
                if (entity.getLocation().distanceSquared(event.getPlayer().getLocation()) > 10 * 10)
                    entity.teleport(event.getPlayer().getLocation());
                Location loc = entity.getLocation();
                float yaw = ((CraftLivingEntity) entity).getHandle().getYRot();
                loc.setYaw(yaw);
                entity.teleport(loc);
            }, 50, 50).getTaskId();
        }
    }

    public void disable() {
        if (entity != null) {
            entity.remove();
            entity = null;
        }
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (ExtravaganzaPlugin.isBirthday(event.getPlayer())) {
            disable();
        }
    }
}
