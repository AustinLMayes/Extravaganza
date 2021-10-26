package me.austinlm.extravaganza.npc;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.net.ssl.HttpsURLConnection;
import me.austinlm.extravaganza.ExtravaganzaPlugin;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NPC {

    private final Map<UUID, Integer> ids = Maps.newHashMap();
    private final Location location;
    private final BiConsumer<Player, NPC> onClick;
    private final EntityPlayer npc;

    public NPC(Location location, UUID skinUuid, String name, BiConsumer<Player, NPC> onClick) {
        this.location = location;
        this.onClick = onClick;
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "§b" + name);
        setSkin(profile, skinUuid);
        this.npc = new EntityPlayer(server, world, profile);
        DataWatcher watcher = npc.getDataWatcher();
        watcher.set(new DataWatcherObject<>(17, DataWatcherRegistry.a), (byte) 0xFF);
        npc.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0, 0);
    }

    public EntityPlayer getNpc() {
        return npc;
    }

    public void onClick(Player player) {
        this.onClick.accept(player, this);
    }

    public Location getLocation() {
        return location;
    }

    public void despawn(Player player) {
        ((CraftPlayer) player).getHandle().b
            .sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    public void spawn(Player player) {
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, npc));
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), this.npc.getDataWatcher(), true));
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onLeave(PlayerQuitEvent event) {
                if (event.getPlayer().getUniqueId().equals(player.getUniqueId()))
                    HandlerList.unregisterAll(this);
            }

            @EventHandler
            public void onMove(PlayerMoveEvent event) {
                if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutPlayerInfo(
                        EnumPlayerInfoAction.e, npc));
                    HandlerList.unregisterAll(this);
                }
            }
        }, ExtravaganzaPlugin.getInstance());
    }

    private static void setSkin(GameProfile profile, UUID uuid) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(
                String.format("https://api.ashcon.app/mojang/v2/user/%s", UUIDTypeAdapter
                    .fromUUID(uuid))).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                JsonObject data = new Gson()
                    .fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
                JsonObject rawTex = data.get("textures").getAsJsonObject().get("raw")
                    .getAsJsonObject();
                String skin = rawTex.get("value").getAsString();
                String signature = rawTex.get("signature").getAsString();
                profile.getProperties().put("textures", new Property("textures", skin, signature));
            } else {
                System.out.println(
                    "Connection could not be opened (Response code " + connection.getResponseCode()
                        + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
