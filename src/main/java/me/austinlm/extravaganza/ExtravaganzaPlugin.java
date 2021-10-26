package me.austinlm.extravaganza;

import java.io.IOException;
import java.util.UUID;
import me.austinlm.extravaganza.image.ImageDisplay;
import me.austinlm.extravaganza.image.ImageManager;
import me.austinlm.extravaganza.listeners.PetListener;
import me.austinlm.extravaganza.npc.NPC;
import me.austinlm.extravaganza.npc.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtravaganzaPlugin extends JavaPlugin {

    private static ExtravaganzaPlugin instance;
    private NPCManager npcManager;
    private ImageManager imageManager;
    private PetListener petListener;

    public static ExtravaganzaPlugin getInstance() {
        return instance;
    }

    private static final UUID BIRTHDAY_UUID = UUIDCache.ALM;
    public static boolean isBirthday(Entity entity) {
        return entity instanceof Player && entity.getUniqueId().equals(BIRTHDAY_UUID);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.npcManager = new NPCManager();
        this.npcManager.init();
        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 0, 100 ,0), UUID.fromString("08cc0b7a-2367-49d9-b206-9eb391da91b2"), "Redned", (p, n) -> {
            p.sendMessage("Sup!");
        }));

        try {
            this.imageManager = new ImageManager(
                new ImageDisplay("https://i.imgur.com/bZAjPcc.png", new Location(Bukkit.getWorlds().get(0), 30, 100 ,30), 0.05)
            );
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getServer().shutdown();
            return;
        }

        Bukkit.getPluginManager().registerEvents(this.npcManager, this);
        Bukkit.getPluginManager().registerEvents(this.imageManager, this);
        Bukkit.getPluginManager().registerEvents(this.petListener = new PetListener(EntityType.OCELOT, "Skye"), this);
    }

    @Override
    public void onDisable() {
        if (this.petListener != null) this.petListener.disable();
        if (this.imageManager != null) this.imageManager.despawn();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPerms(sender)) {
            return false;
        }
        return command.getName().equalsIgnoreCase("bday");
    }

    private boolean checkPerms(CommandSender sender) {
        if (sender instanceof BlockCommandSender) {
            return false;
        }
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        if (sender instanceof Player) {
            return sender.isOp();
        }
        return false;
    }
}
