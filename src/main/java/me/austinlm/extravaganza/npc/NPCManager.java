package me.austinlm.extravaganza.npc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import me.austinlm.extravaganza.ExtravaganzaPlugin;
import me.austinlm.extravaganza.proto.Reflection;
import me.austinlm.extravaganza.proto.Reflection.FieldAccessor;
import me.austinlm.extravaganza.proto.TinyProtocol;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NPCManager implements Listener {

    private final Map<UUID, AtomicLong> lastUse = Maps.newHashMap();
    private final List<NPC> npcs = Lists.newArrayList();

    private final FieldAccessor<Integer> packetUsed = Reflection
        .getField("{nms}.network.protocol.game.PacketPlayInUseEntity", "a", int.class);

    public void init() {
        TinyProtocol protocol = new TinyProtocol(ExtravaganzaPlugin.getInstance()) {
            @Override
            public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
                // Click entity
                if (packet instanceof PacketPlayInUseEntity entityPacket) {
                    lastUse.putIfAbsent(sender.getUniqueId(), new AtomicLong());
                    if (System.currentTimeMillis() - lastUse.get(sender.getUniqueId()).get() < 1000) {
                        return packet;
                    }
                    int id = packetUsed.get(entityPacket);
                    NPC toUse = null;
                    for (NPC npc : npcs) {
                        // Intercept packet and pass it off to NPC
                        if (npc.getNpc().getId() == id) {
                            toUse = npc;
                        }
                    }
                    if (toUse != null) {
                        toUse.onClick(sender);
                        lastUse.get(sender.getUniqueId()).set(System.currentTimeMillis());
                        return null; // Packet not sent to server
                    }
                }
                return packet;
            }
        };
    }

    public void registerNPC(NPC npc) {
        this.npcs.add(npc);
    }

    public void unregisterNPC(NPC npc) {
        this.npcs.remove(npc);
    }

    /**
     * Spawn/despawn NPCs based on player world
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChange(PlayerChangedWorldEvent event) {
        this.npcs.stream().filter(n -> !n.getLocation().getWorld().equals(event.getPlayer().getWorld()))
            .forEach(n -> n.despawn(event.getPlayer()));
        Bukkit.getScheduler().runTaskLater(ExtravaganzaPlugin.getInstance(), () -> {
            this.npcs.stream().filter(n -> n.getLocation().getWorld().equals(event.getPlayer().getWorld()))
                .forEach(n -> n.spawn(event.getPlayer()));
        }, 40);
    }

    /**
     * Spawn NPCs on player join. Delayed so the client can load in world data
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(ExtravaganzaPlugin.getInstance(), () -> {
            if (event.getPlayer().isOnline()) {
                this.npcs.stream().filter(n -> n.getLocation().getWorld().equals(event.getPlayer().getWorld()))
                    .forEach(n -> n.spawn(event.getPlayer()));
            }
        }, 40);
    }

    @EventHandler
    public void clearCache(PlayerQuitEvent event) {
        this.lastUse.remove(event.getPlayer().getUniqueId());
    }
}
